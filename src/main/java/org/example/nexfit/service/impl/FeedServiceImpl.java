package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.entity.Trainer;
import org.example.nexfit.entity.TrainerInteraction;
import org.example.nexfit.entity.TrainerMedia;
import org.example.nexfit.entity.User;
import org.example.nexfit.entity.enums.InteractionType;
import org.example.nexfit.entity.enums.MediaType;
import org.example.nexfit.model.request.FeedRequest;
import org.example.nexfit.model.response.FeedResponse;
import org.example.nexfit.model.response.TrainerFeedCard;
import org.example.nexfit.repository.TrainerMediaRepository;
import org.example.nexfit.repository.TrainerRepository;
import org.example.nexfit.repository.UserRepository;
import org.example.nexfit.service.FeedService;
import org.example.nexfit.service.TrainerInteractionService;
import org.example.nexfit.service.TrainerVisibilityService;
import org.example.nexfit.util.DistanceCalculator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedServiceImpl implements FeedService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final TrainerMediaRepository mediaRepository;
    private final TrainerInteractionService interactionService;
    private final TrainerVisibilityService visibilityService;

    private static final int RECENCY_FILTER_HOURS = 24;
    private static final double EXPLORATION_MIX_RATIO = 0.1; // 10% exploration

    @Override
    public FeedResponse getFeed(String userId, FeedRequest request) {
        log.debug("Generating feed for user: {}", userId);

        // Determine seed for reproducible randomization (for pagination consistency)
        Long seed = request.getSeed() != null ? request.getSeed() : System.currentTimeMillis();
        Random seededRandom = new Random(seed);

        // Get user preferences (null for anonymous users)
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        // Get user location (from request or user profile)
        Double userLat = request.getLatitude();
        Double userLng = request.getLongitude();
        if (userLat == null && user != null) {
            userLat = user.getLatitude();
            userLng = user.getLongitude();
        }

        // Get recently interacted trainer IDs (to filter out)
        Set<String> recentlySaved = interactionService.getRecentInteractions(userId, InteractionType.SAVED, RECENCY_FILTER_HOURS)
                .stream().map(TrainerInteraction::getTrainerId).collect(Collectors.toSet());
        Set<String> recentlySkipped = interactionService.getRecentInteractions(userId, InteractionType.SKIPPED, RECENCY_FILTER_HOURS)
                .stream().map(TrainerInteraction::getTrainerId).collect(Collectors.toSet());

        Set<String> excludedTrainerIds = new HashSet<>();
        excludedTrainerIds.addAll(recentlySaved);
        excludedTrainerIds.addAll(recentlySkipped);

        // Get all active trainers
        List<Trainer> allTrainers = trainerRepository.findAll().stream()
                .filter(visibilityService::isVisibleToUsers)
                .filter(t -> !excludedTrainerIds.contains(t.getId()))
                .toList();

        // Calculate match scores and sort
        List<ScoredTrainer> scoredTrainers = new ArrayList<>();
        for (Trainer trainer : allTrainers) {
            int score = calculateMatchScore(trainer, user, userLat, userLng);
            scoredTrainers.add(new ScoredTrainer(trainer, score));
        }

        // Sort by score descending
        scoredTrainers.sort((a, b) -> Integer.compare(b.score, a.score));

        // Add exploration mix and apply seeded shuffle for variety
        int explorationCount = (int) (request.getSize() * EXPLORATION_MIX_RATIO);
        List<ScoredTrainer> finalList = addExplorationMix(scoredTrainers, explorationCount, seededRandom);

        // Pagination
        int start = request.getPage() * request.getSize();
        int end = Math.min(start + request.getSize(), finalList.size());

        int totalPages = (int) Math.ceil((double) finalList.size() / request.getSize());

        if (start >= finalList.size()) {
            return FeedResponse.builder()
                    .trainers(Collections.emptyList())
                    .sessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString())
                    .seed(seed)
                    .page(request.getPage())
                    .limit(request.getSize())
                    .hasMore(false)
                    .totalCount(finalList.size())
                    .totalPages(totalPages)
                    .build();
        }

        List<ScoredTrainer> pageTrainers = finalList.subList(start, end);

        // Get media for trainers
        List<String> trainerIds = pageTrainers.stream().map(st -> st.trainer.getId()).toList();
        Map<String, List<TrainerMedia>> mediaMap = mediaRepository.findByTrainerIdInOrderByDisplayOrderAsc(trainerIds)
                .stream().collect(Collectors.groupingBy(TrainerMedia::getTrainerId));

        // Convert to feed cards
        Double finalUserLat = userLat;
        Double finalUserLng = userLng;
        List<TrainerFeedCard> feedCards = pageTrainers.stream()
                .map(st -> convertToFeedCard(st.trainer, st.score, mediaMap.get(st.trainer.getId()), finalUserLat, finalUserLng))
                .toList();

        // Log first trainer for debugging
        if (!feedCards.isEmpty()) {
            TrainerFeedCard first = feedCards.get(0);
            log.info("=== FEED TRAINER DEBUG ===");
            log.info("Trainer: id={}, name={}", first.getId(), first.getName());
            log.info("FeaturedVideo: {}", first.getFeaturedVideo() != null
                ? String.format("type=%s, mediaUrl=%s, thumbnailUrl=%s",
                    first.getFeaturedVideo().getType(),
                    first.getFeaturedVideo().getMediaUrl(),
                    first.getFeaturedVideo().getThumbnailUrl())
                : "null");
            log.info("Media count: {}", first.getMedia() != null ? first.getMedia().size() : 0);
            if (first.getMedia() != null && !first.getMedia().isEmpty()) {
                first.getMedia().forEach(m ->
                    log.info("  Media: type={}, url={}", m.getType(), m.getMediaUrl()));
            }
            log.info("=== END DEBUG ===");
        }

        return FeedResponse.builder()
                .trainers(feedCards)
                .sessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString())
                .seed(seed)
                .page(request.getPage())
                .limit(request.getSize())
                .hasMore(end < finalList.size())
                .totalCount(finalList.size())
                .totalPages(totalPages)
                .build();
    }

    private int calculateMatchScore(Trainer trainer, User user, Double userLat, Double userLng) {
        int score = 0;

        // Preference match (40%)
        if (user != null) {
            // Activities/specializations match
            Set<String> userActivities = new HashSet<>(user.getPreferredActivities() != null ? user.getPreferredActivities() : List.of());
            Set<String> userCategories = new HashSet<>(user.getSelectedCategories() != null ? user.getSelectedCategories() : List.of());
            Set<String> userSubcategories = new HashSet<>(user.getSelectedSubcategories() != null ? user.getSelectedSubcategories() : List.of());

            Set<String> trainerSpecs = trainer.getSpecializations().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            int prefMatches = 0;
            for (String activity : userActivities) {
                if (trainerSpecs.stream().anyMatch(s -> s.contains(activity.toLowerCase()))) {
                    prefMatches++;
                }
            }
            // Add category and subcategory matching
            for (String cat : userCategories) {
                if (trainerSpecs.stream().anyMatch(s -> s.toLowerCase().contains(cat.toLowerCase().replace("_", " ")))) {
                    prefMatches++;
                }
            }

            score += Math.min(40, prefMatches * 10);

            // Gender preference
            if (user.getTrainerGenderPreference() != null && trainer.getGender() != null) {
                if (user.getTrainerGenderPreference() == User.TrainerGenderPreference.NO_PREFERENCE) {
                    score += 5;
                } else if ((user.getTrainerGenderPreference() == User.TrainerGenderPreference.MALE && trainer.getGender() == User.Gender.MALE) ||
                           (user.getTrainerGenderPreference() == User.TrainerGenderPreference.FEMALE && trainer.getGender() == User.Gender.FEMALE)) {
                    score += 10;
                }
            }
        }

        // Distance proximity (25%)
        if (userLat != null && userLng != null && trainer.getLatitude() != null && trainer.getLongitude() != null) {
            double distance = DistanceCalculator.calculateDistance(userLat, userLng, trainer.getLatitude(), trainer.getLongitude());
            if (distance <= 5) {
                score += 25;
            } else if (distance <= 10) {
                score += 20;
            } else if (distance <= 20) {
                score += 15;
            } else if (distance <= 50) {
                score += 10;
            } else {
                score += 5;
            }
        }

        // Rating (20%)
        if (trainer.getRating() != null) {
            double rating = trainer.getRating().doubleValue();
            if (rating >= 4.8) {
                score += 20;
            } else if (rating >= 4.5) {
                score += 16;
            } else if (rating >= 4.0) {
                score += 12;
            } else if (rating >= 3.5) {
                score += 8;
            } else {
                score += 4;
            }
        }

        // Activity/recency (15%) - based on profile completeness
        int activityScore = 0;
        if (trainer.getBio() != null && !trainer.getBio().isEmpty()) activityScore += 3;
        if (trainer.getGallery() != null && !trainer.getGallery().isEmpty()) activityScore += 3;
        if (trainer.getCertifications() != null && !trainer.getCertifications().isEmpty()) activityScore += 3;
        if (trainer.getContactMethods() != null && !trainer.getContactMethods().isEmpty()) activityScore += 3;
        if (trainer.getReviewCount() != null && trainer.getReviewCount() > 5) activityScore += 3;
        score += Math.min(15, activityScore);

        return Math.min(100, score);
    }

    private List<ScoredTrainer> addExplorationMix(List<ScoredTrainer> sorted, int explorationCount, Random random) {
        if (sorted.size() <= explorationCount) {
            return sorted;
        }

        List<ScoredTrainer> result = new ArrayList<>(sorted);

        // Take some random trainers from lower ranks and move them up
        int lowRankStart = sorted.size() / 2;

        for (int i = 0; i < explorationCount && lowRankStart + i < sorted.size(); i++) {
            int randomIdx = lowRankStart + random.nextInt(sorted.size() - lowRankStart);
            ScoredTrainer explorer = result.remove(randomIdx);
            // Insert at a random position in the top half
            int insertIdx = random.nextInt(Math.min(10, result.size()));
            result.add(insertIdx, explorer);
        }

        return result;
    }

    private TrainerFeedCard convertToFeedCard(Trainer trainer, int matchScore, List<TrainerMedia> media,
                                              Double userLat, Double userLng) {
        // Get primary contact
        TrainerFeedCard.ContactInfo primaryContact = null;
        if (trainer.getContactMethods() != null) {
            var primary = trainer.getContactMethods().stream()
                    .filter(cm -> Boolean.TRUE.equals(cm.getIsPrimary()))
                    .findFirst()
                    .orElse(trainer.getContactMethods().isEmpty() ? null : trainer.getContactMethods().get(0));

            if (primary != null) {
                primaryContact = TrainerFeedCard.ContactInfo.builder()
                        .type(primary.getType())
                        .value(primary.getValue())
                        .label(primary.getLabel())
                        .build();
            }
        }

        // Convert media
        List<TrainerFeedCard.MediaInfo> mediaInfos = media != null ? media.stream()
                .limit(5) // Max 5 media items per trainer in feed
                .map(m -> TrainerFeedCard.MediaInfo.builder()
                        .id(m.getId())
                        .type(m.getType().name().toLowerCase())
                        .mediaUrl(m.getMediaUrl())
                        .thumbnailUrl(m.getThumbnailUrl())
                        .title(m.getTitle())
                        .durationSeconds(m.getDurationSeconds())
                        .build())
                .toList() : Collections.emptyList();

        // Select featured video: prefer isFeatured=true, then first VIDEO by displayOrder
        TrainerFeedCard.MediaInfo featuredVideo = null;
        if (media != null) {
            // First try to find a video marked as featured
            TrainerMedia featured = media.stream()
                    .filter(m -> m.getType() == MediaType.VIDEO)
                    .filter(m -> Boolean.TRUE.equals(m.getIsFeatured()))
                    .findFirst()
                    .orElse(null);

            // If no featured video, get the first video by displayOrder
            if (featured == null) {
                featured = media.stream()
                        .filter(m -> m.getType() == MediaType.VIDEO)
                        .findFirst()
                        .orElse(null);
            }

            if (featured != null) {
                featuredVideo = TrainerFeedCard.MediaInfo.builder()
                        .id(featured.getId())
                        .type(featured.getType().name().toLowerCase())
                        .mediaUrl(featured.getMediaUrl())
                        .thumbnailUrl(featured.getThumbnailUrl())
                        .title(featured.getTitle())
                        .durationSeconds(featured.getDurationSeconds())
                        .build();
            }
        }

        // Calculate distance
        Double distance = null;
        if (userLat != null && userLng != null && trainer.getLatitude() != null && trainer.getLongitude() != null) {
            distance = DistanceCalculator.calculateDistance(userLat, userLng, trainer.getLatitude(), trainer.getLongitude());
        }

        return TrainerFeedCard.builder()
                .id(trainer.getId())
                .name(trainer.getName())
                .profileImage(trainer.getProfileImage())
                .bio(trainer.getBio())
                .featuredVideo(featuredVideo)
                .media(mediaInfos)
                .specializations(trainer.getSpecializations())
                .rating(trainer.getRating())
                .reviewCount(trainer.getReviewCount())
                .hourlyRate(trainer.getHourlyRate())
                .distance(distance)
                .matchScore(matchScore)
                .experience(trainer.getExperience())
                .city(trainer.getCity())
                .primaryContact(primaryContact)
                .build();
    }

    private record ScoredTrainer(Trainer trainer, int score) {}
}
