package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.entity.Trainer;
import org.example.nexfit.entity.TrainerAvailability;
import org.example.nexfit.exception.ResourceNotFoundException;
import org.example.nexfit.model.dto.ReviewDTO;
import org.example.nexfit.model.dto.TrainerDTO;
import org.example.nexfit.model.request.TrainerSearchRequest;
import org.example.nexfit.model.request.TrainerMatchRequest;
import org.example.nexfit.model.response.MatchedTrainerResponse;
import org.example.nexfit.model.response.PageResponse;
import org.example.nexfit.model.response.TrainerPortfolioResponse;
import org.example.nexfit.repository.ReviewRepository;
import org.example.nexfit.repository.UserRepository;
import org.example.nexfit.repository.TrainerAvailabilityRepository;
import org.example.nexfit.repository.TrainerRepository;
import org.example.nexfit.service.TrainerService;
import org.example.nexfit.service.TrainerVisibilityService;
import org.example.nexfit.util.DistanceCalculator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerServiceImpl implements TrainerService {
    
    private final TrainerRepository trainerRepository;
    private final TrainerAvailabilityRepository availabilityRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final TrainerVisibilityService visibilityService;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");
    
    @Override
    public PageResponse<TrainerDTO> searchTrainers(TrainerSearchRequest request, Pageable pageable) {
        List<Trainer> trainers = trainerRepository.findAll().stream()
                .filter(visibilityService::isVisibleToUsers)
                .toList();

        trainers = applySearchFilters(trainers, request);

        List<TrainerDTO> trainerDTOs = trainers.stream()
                .map(trainer -> convertToDTO(trainer, request.getLatitude(), request.getLongitude()))
                .collect(Collectors.toList());

        if (request.getSortBy() != null) {
            trainerDTOs = sortTrainers(trainerDTOs, request.getSortBy());
        }

        return buildPageResponse(trainerDTOs, pageable);
    }
    
    @Override
    public TrainerDTO getTrainerById(String trainerId) {
        Trainer trainer = trainerRepository.findById(trainerId)
            .orElseThrow(() -> new ResourceNotFoundException("Trainer", trainerId));
        
        if (!visibilityService.isVisibleToUsers(trainer)) {
            throw new ResourceNotFoundException("Trainer not found or inactive");
        }
        
        return convertToDTO(trainer, null, null);
    }
    
    @Override
    public Map<String, List<String>> getTrainerAvailability(String trainerId, LocalDate date) {
        // Get trainer availability
        TrainerAvailability availability = availabilityRepository.findByTrainerId(trainerId)
            .orElse(createDefaultAvailability(trainerId));
        
        Map<String, List<String>> weekAvailability = new HashMap<>();
        
        // Get availability for the week containing the given date
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startOfWeek.plusDays(i);
            List<String> slots = getAvailableTimeSlots(trainerId, currentDate);
            weekAvailability.put(currentDate.getDayOfWeek().toString().toLowerCase(), slots);
        }
        
        return weekAvailability;
    }
    
    @Override
    public List<String> getAvailableTimeSlots(String trainerId, LocalDate date) {
        // Get trainer availability
        TrainerAvailability availability = availabilityRepository.findByTrainerId(trainerId)
            .orElse(createDefaultAvailability(trainerId));
        
        // Get available slots for the date
        List<TrainerAvailability.TimeSlot> availableSlots = availability.getAvailableSlotsForDate(date);
        
        // Generate available time slots (hourly from 6 AM to 9 PM)
        List<String> timeSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(6, 0);
        LocalTime endTime = LocalTime.of(21, 0);
        
        while (startTime.isBefore(endTime)) {
            // Check if this time is within trainer's available hours
            LocalTime finalStartTime = startTime;
            boolean isAvailable = availableSlots.stream()
                .anyMatch(slot -> !finalStartTime.isBefore(slot.getStartTime()) &&
                                 finalStartTime.isBefore(slot.getEndTime()));
            
            if (isAvailable || availableSlots.isEmpty()) {
                timeSlots.add(startTime.format(TIME_FORMATTER));
            }
            startTime = startTime.plusHours(1);
        }
        
        return timeSlots;
    }

    @Override
    public PageResponse<MatchedTrainerResponse> getMatchedTrainers(TrainerMatchRequest request, Pageable pageable) {
        List<Trainer> trainers = trainerRepository.findAll().stream()
                .filter(visibilityService::isVisibleToUsers)
                .toList();

        List<MatchedTrainerResponse> matches = trainers.stream()
                .map(trainer -> buildMatch(trainer, request))
                .sorted(Comparator.comparing(MatchedTrainerResponse::getMatchPercentage).reversed())
                .toList();

        return buildPageResponse(matches, pageable);
    }

    @Override
    public TrainerPortfolioResponse getTrainerPortfolio(String trainerId) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", trainerId));
        if (!visibilityService.isVisibleToUsers(trainer)) {
            throw new ResourceNotFoundException("Trainer not found or inactive");
        }

        var reviews = reviewRepository.findByTrainerId(trainerId).stream()
                .map(review -> {
                    var user = userRepository.findById(review.getUserId()).orElse(null);
                    return ReviewDTO.builder()
                            .id(review.getId())
                            .trainerId(review.getTrainerId())
                            .rating(review.getRating())
                            .comment(review.getComment())
                            .createdAt(review.getCreatedAt())
                            .user(ReviewDTO.UserInfo.builder()
                                    .id(review.getUserId())
                                    .name(user != null ? user.getName() : "Unknown")
                                    .avatar(user != null ? user.getAvatar() : null)
                                    .build())
                            .build();
                })
                .toList();

        return TrainerPortfolioResponse.builder()
                .id(trainer.getId())
                .name(trainer.getName())
                .email(trainer.getEmail())
                .phone(trainer.getPhone())
                .profileImage(trainer.getProfileImage())
                .coverImage(trainer.getCoverImage())
                .hourlyRate(trainer.getHourlyRate())
                .rating(trainer.getRating())
                .reviewCount(trainer.getReviewCount())
                .experience(trainer.getExperience())
                .totalClients(trainer.getTotalClients())
                .transformations(trainer.getTransformations())
                .sessionsCompleted(trainer.getSessionsCompleted())
                .yearsActive(trainer.getYearsActive())
                .bio(trainer.getBio())
                .gymAffiliation(trainer.getGymAffiliation())
                .specializations(trainer.getSpecializations())
                .certifications(trainer.getCertifications())
                .languages(trainer.getLanguages())
                .location(TrainerPortfolioResponse.TrainerLocation.builder()
                        .latitude(trainer.getLatitude())
                        .longitude(trainer.getLongitude())
                        .address(trainer.getAddress())
                        .city(trainer.getCity())
                        .state(trainer.getState())
                        .country(trainer.getCountry())
                        .zipCode(trainer.getZipCode())
                        .build())
                .gallery(trainer.getGallery())
                .achievements(trainer.getAchievements())
                .trainingLocations(trainer.getTrainingLocations())
                .reviews(reviews)
                .build();
    }
    
    private TrainerDTO convertToDTO(Trainer trainer, Double userLat, Double userLng) {
        TrainerDTO.TrainerDTOBuilder dtoBuilder = TrainerDTO.builder()
            .id(trainer.getId())
            .name(trainer.getName())
            .email(trainer.getEmail())
            .phone(trainer.getPhone())
            .profileImage(trainer.getProfileImage())
            .coverImage(trainer.getCoverImage())
            .specializations(trainer.getSpecializations())
            .experience(trainer.getExperience())
            .rating(trainer.getRating())
            .reviewCount(trainer.getReviewCount())
            .hourlyRate(trainer.getHourlyRate())
            .bio(trainer.getBio())
            .certifications(trainer.getCertifications())
            .instagramId(trainer.getInstagramId())
            .languages(trainer.getLanguages())
            .gymAffiliation(trainer.getGymAffiliation())
            .gallery(trainer.getGallery() != null
                    ? trainer.getGallery().stream().map(Trainer.TrainerImage::getUrl).toList()
                    : List.of())
            .location(TrainerDTO.LocationDTO.builder()
                .latitude(trainer.getLatitude())
                .longitude(trainer.getLongitude())
                .address(trainer.getAddress())
                .city(trainer.getCity())
                .state(trainer.getState())
                .country(trainer.getCountry())
                .zipCode(trainer.getZipCode())
                .build())
            .stats(TrainerDTO.StatsDTO.builder()
                .totalClients(trainer.getTotalClients())
                .transformations(trainer.getTransformations())
                .sessionsCompleted(trainer.getSessionsCompleted())
                .yearsActive(trainer.getYearsActive())
                .build())
            .whatsapp(trainer.getWhatsapp())
            .website(trainer.getWebsite())
            .contactMethods(trainer.getContactMethods() != null
                    ? trainer.getContactMethods().stream()
                        .map(cm -> TrainerDTO.ContactMethodDTO.builder()
                            .type(cm.getType())
                            .value(cm.getValue())
                            .label(cm.getLabel())
                            .isPrimary(cm.getIsPrimary())
                            .build())
                        .toList()
                    : List.of());

        // Calculate distance if user location is provided
        if (userLat != null && userLng != null && trainer.getLatitude() != null && trainer.getLongitude() != null) {
            double distance = DistanceCalculator.calculateDistance(
                userLat, userLng,
                trainer.getLatitude(), trainer.getLongitude()
            );
            dtoBuilder.distance(distance);
        }
        
        return dtoBuilder.build();
    }

    private List<Trainer> applySearchFilters(List<Trainer> trainers, TrainerSearchRequest request) {
        if (request.getSearch() != null && !request.getSearch().isBlank()) {
            String searchLower = request.getSearch().toLowerCase();
            trainers = trainers.stream()
                    .filter(trainer -> (trainer.getName() != null && trainer.getName().toLowerCase().contains(searchLower))
                            || (trainer.getCity() != null && trainer.getCity().toLowerCase().contains(searchLower))
                            || trainer.getSpecializations().stream().anyMatch(spec -> spec.toLowerCase().contains(searchLower)))
                    .toList();
        }

        if (request.getSpecializations() != null && !request.getSpecializations().isEmpty()) {
            Set<String> requested = request.getSpecializations().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            trainers = trainers.stream()
                    .filter(trainer -> trainer.getSpecializations().stream()
                            .map(String::toLowerCase)
                            .anyMatch(requested::contains))
                    .toList();
        }

        if (request.getMinRating() != null) {
            trainers = trainers.stream()
                    .filter(trainer -> trainer.getRating() != null && trainer.getRating().compareTo(request.getMinRating()) >= 0)
                    .toList();
        }

        if (request.getMaxPrice() != null) {
            trainers = trainers.stream()
                    .filter(trainer -> trainer.getHourlyRate() != null && trainer.getHourlyRate().compareTo(request.getMaxPrice()) <= 0)
                    .toList();
        }

        if (request.getLatitude() != null && request.getLongitude() != null && request.getMaxDistance() != null) {
            trainers = trainers.stream()
                    .filter(trainer -> trainer.getLatitude() != null && trainer.getLongitude() != null)
                    .filter(trainer -> DistanceCalculator.calculateDistance(
                            request.getLatitude(),
                            request.getLongitude(),
                            trainer.getLatitude(),
                            trainer.getLongitude()) <= request.getMaxDistance())
                    .toList();
        }

        return trainers;
    }

    private MatchedTrainerResponse buildMatch(Trainer trainer, TrainerMatchRequest request) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // Activities match
        if (request.getActivities() != null && !request.getActivities().isEmpty()) {
            Set<String> activitySet = request.getActivities().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            List<String> matched = trainer.getSpecializations().stream()
                    .filter(spec -> activitySet.contains(spec.toLowerCase()))
                    .toList();
            if (!matched.isEmpty()) {
                score += 35;
                reasons.add("Specializes in " + String.join(", ", matched));
            }
        }

        // Goals match (substring match against specializations)
        if (request.getGoals() != null && !request.getGoals().isEmpty()) {
            List<String> matchedGoals = request.getGoals().stream()
                    .filter(goal -> trainer.getSpecializations().stream()
                            .anyMatch(spec -> spec.toLowerCase().contains(goal.toLowerCase().replace("_", " "))))
                    .toList();
            if (!matchedGoals.isEmpty()) {
                score += 25;
                reasons.add("Great for " + String.join(", ", matchedGoals));
            }
        }

        // Experience level
        if (request.getExperienceLevel() != null) {
            int years = trainer.getExperience() != null ? trainer.getExperience() : 0;
            boolean match = switch (request.getExperienceLevel()) {
                case BEGINNER -> true;
                case INTERMEDIATE -> years >= 2;
                case ADVANCED -> years >= 4;
                case PROFESSIONAL -> years >= 6;
            };
            if (match) {
                score += 15;
            }
        }

        // Trainer gender preference
        if (request.getTrainerGender() != null) {
            if (request.getTrainerGender() == org.example.nexfit.entity.User.TrainerGenderPreference.NO_PREFERENCE) {
                score += 10;
            } else if (trainer.getGender() != null) {
                boolean matches = (request.getTrainerGender() == org.example.nexfit.entity.User.TrainerGenderPreference.MALE
                        && trainer.getGender() == org.example.nexfit.entity.User.Gender.MALE)
                        || (request.getTrainerGender() == org.example.nexfit.entity.User.TrainerGenderPreference.FEMALE
                        && trainer.getGender() == org.example.nexfit.entity.User.Gender.FEMALE);
                if (matches) {
                    score += 10;
                }
            }
        }

        // Distance
        if (request.getLatitude() != null && request.getLongitude() != null
                && trainer.getLatitude() != null && trainer.getLongitude() != null) {
            double distance = DistanceCalculator.calculateDistance(
                    request.getLatitude(),
                    request.getLongitude(),
                    trainer.getLatitude(),
                    trainer.getLongitude()
            );
            if (distance <= 5) {
                score += 15;
                reasons.add(String.format("Only %.1f km away", distance));
            } else if (distance <= 10) {
                score += 10;
                reasons.add(String.format("Only %.1f km away", distance));
            } else if (distance <= 25) {
                score += 5;
                reasons.add(String.format("Only %.1f km away", distance));
            }
        }

        // Rating
        if (trainer.getRating() != null && trainer.getRating().doubleValue() >= 4.5) {
            score += 5;
            reasons.add("Top rated (" + trainer.getRating() + " stars)");
        }

        TrainerDTO dto = convertToDTO(trainer, request.getLatitude(), request.getLongitude());
        return MatchedTrainerResponse.builder()
                .trainer(dto)
                .matchPercentage(Math.min(score, 100))
                .matchReasons(reasons)
                .build();
    }

    private <T> PageResponse<T> buildPageResponse(List<T> items, Pageable pageable) {
        int page = pageable.getPageNumber();
        int limit = pageable.getPageSize();
        int start = Math.min(page * limit, items.size());
        int end = Math.min(start + limit, items.size());
        List<T> pageData = items.subList(start, end);
        long total = items.size();
        int totalPages = limit == 0 ? 1 : (int) Math.ceil((double) total / limit);

        return PageResponse.<T>builder()
                .data(pageData)
                .pagination(PageResponse.PaginationInfo.builder()
                        .page(page)
                        .limit(limit)
                        .total(total)
                        .totalPages(totalPages)
                        .hasNext(page + 1 < totalPages)
                        .hasPrevious(page > 0)
                        .build())
                .build();
    }
    
    private List<TrainerDTO> sortTrainers(List<TrainerDTO> trainers, String sortBy) {
        switch (sortBy) {
            case "distance":
                return trainers.stream()
                    .sorted(Comparator.comparing(t -> t.getDistance() != null ? t.getDistance() : Double.MAX_VALUE))
                    .collect(Collectors.toList());
            case "rating":
                return trainers.stream()
                    .sorted((a, b) -> b.getRating().compareTo(a.getRating()))
                    .collect(Collectors.toList());
            case "price_low":
                return trainers.stream()
                    .sorted(Comparator.comparing(TrainerDTO::getHourlyRate))
                    .collect(Collectors.toList());
            case "price_high":
                return trainers.stream()
                    .sorted((a, b) -> b.getHourlyRate().compareTo(a.getHourlyRate()))
                    .collect(Collectors.toList());
            case "experience":
                return trainers.stream()
                    .sorted((a, b) -> b.getExperience().compareTo(a.getExperience()))
                    .collect(Collectors.toList());
            default:
                return trainers;
        }
    }
    
    private TrainerAvailability createDefaultAvailability(String trainerId) {
        // Create default availability (Mon-Fri 6AM-9PM, Sat-Sun 7AM-7PM)
        Map<DayOfWeek, List<TrainerAvailability.TimeSlot>> weeklySchedule = new HashMap<>();
        
        // Weekdays
        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                                     DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            weeklySchedule.put(day, List.of(
                TrainerAvailability.TimeSlot.builder()
                    .startTime(LocalTime.of(6, 0))
                    .endTime(LocalTime.of(21, 0))
                    .available(true)
                    .build()
            ));
        }
        
        // Weekends
        for (DayOfWeek day : List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
            weeklySchedule.put(day, List.of(
                TrainerAvailability.TimeSlot.builder()
                    .startTime(LocalTime.of(7, 0))
                    .endTime(LocalTime.of(19, 0))
                    .available(true)
                    .build()
            ));
        }
        
        return TrainerAvailability.builder()
            .trainerId(trainerId)
            .weeklySchedule(weeklySchedule)
            .build();
    }
}
