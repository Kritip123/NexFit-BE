package org.example.nexfit.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.entity.*;
import org.example.nexfit.entity.enums.MediaType;
import org.example.nexfit.entity.enums.TrainerStatus;
import org.example.nexfit.repository.*;
import org.example.nexfit.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final TrainingCategoryRepository categoryRepository;
    private final TrainingSubcategoryRepository subcategoryRepository;
    private final TrainerMediaRepository mediaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Value("${aws.region:ap-southeast-2}")
    private String awsRegion;

    @Value("${aws.access-key:}")
    private String awsAccessKey;

    @Value("${aws.secret-key:}")
    private String awsSecretKey;

    @Value("${aws.s3.bucket:nexfit-media-prod}")
    private String s3Bucket;

    private final Random random = new Random();

    // Fallback demo videos (used when S3 is disabled or empty)
    private static final List<String> FALLBACK_VIDEO_URLS = List.of(
            "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
    );

    /**
     * Fetches all video URLs from S3 bucket's videos/ folder
     */
    private List<String> fetchS3VideoUrls() {
        if (!s3Enabled || awsAccessKey.isEmpty() || awsSecretKey.isEmpty()) {
            log.info("S3 disabled, using fallback demo videos");
            return FALLBACK_VIDEO_URLS;
        }

        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(s3Bucket)
                    .prefix("videos/")
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            List<String> videoUrls = response.contents().stream()
                    .map(S3Object::key)
                    .filter(key -> key.endsWith(".mp4") || key.endsWith(".mov") || key.endsWith(".webm"))
                    .map(key -> String.format("https://%s.s3.%s.amazonaws.com/%s", s3Bucket, awsRegion, key))
                    .collect(Collectors.toList());

            if (videoUrls.isEmpty()) {
                log.warn("No videos found in S3 bucket, using fallback demo videos");
                return FALLBACK_VIDEO_URLS;
            }

            log.info("Found {} videos in S3 bucket", videoUrls.size());
            return videoUrls;

        } catch (Exception e) {
            log.error("Failed to fetch videos from S3, using fallback", e);
            return FALLBACK_VIDEO_URLS;
        }
    }

    // Demo thumbnail URLs (Unsplash fitness images)
    private static final List<String> DEMO_THUMBNAIL_URLS = List.of(
            "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800",
            "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800",
            "https://images.unsplash.com/photo-1549576490-b0b4831ef60a?w=800",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=800",
            "https://images.unsplash.com/photo-1574680096145-d05b474e2155?w=800",
            "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800"
    );

    @Override
    public void run(String... args) {
        // Seed categories if they don't exist
        if (categoryRepository.count() == 0) {
            seedTrainingCategories();
        }

        // Seed other data if empty
        if (trainerRepository.count() > 0 || userRepository.count() > 0 || reviewRepository.count() > 0) {
            // Seed media for existing trainers if media is empty
            if (mediaRepository.count() == 0) {
                log.info("Seeding media for existing trainers...");
                List<Trainer> existingTrainers = trainerRepository.findAll();
                seedTrainerMedia(existingTrainers);
                log.info("Seeded {} media items", mediaRepository.count());
            }
            log.info("Seeder skipped: user/trainer data already exists.");
            return;
        }

        List<User> users = seedUsers();
        List<Trainer> trainers = seedTrainers();
        seedReviews(users, trainers);
        seedTrainerMedia(trainers);

        log.info("Seeder completed: {} users, {} trainers, {} reviews, {} categories, {} media items",
                users.size(), trainers.size(), reviewRepository.count(),
                categoryRepository.count(), mediaRepository.count());
    }

    private void seedTrainingCategories() {
        log.info("Seeding training categories and subcategories...");

        Map<String, CategoryData> categories = new LinkedHashMap<>();

        categories.put("strength", new CategoryData(
                "Strength & Muscle Building", "dumbbell",
                "Build muscle mass and increase strength with resistance training",
                "https://images.pexels.com/photos/1552242/pexels-photo-1552242.jpeg?auto=compress&cs=tinysrgb&w=800",
                List.of("Powerlifting", "Bodybuilding", "Olympic Weightlifting", "Calisthenics", "Functional Strength")
        ));

        categories.put("weight_loss", new CategoryData(
                "Weight Loss & Fat Burn", "fire",
                "Achieve your ideal weight with effective fat-burning workouts",
                "https://images.pexels.com/photos/3768916/pexels-photo-3768916.jpeg?auto=compress&cs=tinysrgb&w=800",
                List.of("HIIT", "Cardio Training", "Circuit Training", "Metabolic Conditioning", "Body Transformation")
        ));

        categories.put("sports", new CategoryData(
                "Sports Performance", "trophy",
                "Enhance athletic performance for competitive sports",
                "https://images.pexels.com/photos/2294361/pexels-photo-2294361.jpeg?auto=compress&cs=tinysrgb&w=800",
                List.of("Speed & Agility", "Sports-Specific Training", "Endurance", "Athletic Conditioning", "Performance Coaching")
        ));

        categories.put("yoga", new CategoryData(
                "Yoga & Flexibility", "leaf",
                "Improve flexibility, balance, and mindfulness through yoga",
                "https://images.pexels.com/photos/3822906/pexels-photo-3822906.jpeg?auto=compress&cs=tinysrgb&w=800",
                List.of("Vinyasa Yoga", "Hatha Yoga", "Power Yoga", "Yin Yoga", "Prenatal Yoga", "Flexibility Training")
        ));

        categories.put("hiit", new CategoryData(
                "HIIT & Cardio", "zap",
                "High-intensity workouts for maximum calorie burn",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=800",
                List.of("Interval Training", "Tabata", "Boot Camp", "CrossFit Style", "Boxing Cardio")
        ));

        categories.put("martial_arts", new CategoryData(
                "Martial Arts & Combat", "shield",
                "Learn self-defense and combat sports techniques",
                "https://images.pexels.com/photos/4754146/pexels-photo-4754146.jpeg?auto=compress&cs=tinysrgb&w=800",
                List.of("Boxing", "Kickboxing", "MMA", "Brazilian Jiu-Jitsu", "Muay Thai", "Self-Defense")
        ));

        categories.put("rehab", new CategoryData(
                "Rehabilitation & Mobility", "heart",
                "Recover from injuries and improve movement quality",
                "https://images.pexels.com/photos/5473182/pexels-photo-5473182.jpeg?auto=compress&cs=tinysrgb&w=800",
                List.of("Injury Rehabilitation", "Corrective Exercise", "Mobility Training", "Chronic Pain Management", "Post-Surgery Recovery")
        ));

        categories.put("nutrition", new CategoryData(
                "Nutrition & Lifestyle", "utensils",
                "Optimize your diet and lifestyle for better results",
                "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg?auto=compress&cs=tinysrgb&w=800",
                List.of("Nutrition Coaching", "Meal Planning", "Weight Management", "Sports Nutrition", "Lifestyle Coaching")
        ));

        int order = 0;
        for (var entry : categories.entrySet()) {
            CategoryData data = entry.getValue();

            TrainingCategory category = TrainingCategory.builder()
                    .name(data.name)
                    .icon(data.icon)
                    .description(data.description)
                    .imageUrl(data.imageUrl)
                    .displayOrder(order++)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            category = categoryRepository.save(category);

            // Seed subcategories
            int subOrder = 0;
            for (String subName : data.subcategories) {
                TrainingSubcategory subcategory = TrainingSubcategory.builder()
                        .categoryId(category.getId())
                        .name(subName)
                        .description("Specialized " + subName.toLowerCase() + " training")
                        .displayOrder(subOrder++)
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                subcategoryRepository.save(subcategory);
            }
        }

        log.info("Seeded {} categories with subcategories", categories.size());
    }

    private void seedTrainerMedia(List<Trainer> trainers) {
        log.info("Seeding media for trainers...");

        // Fetch videos from S3 (or fallback to demo)
        List<String> videoUrls = fetchS3VideoUrls();
        log.info("Using {} videos for seeding", videoUrls.size());

        List<String> videoTitles = List.of(
                "Morning HIIT Workout", "Strength Training Basics", "Full Body Burn",
                "Core Crusher", "Leg Day Special", "Upper Body Power"
        );

        List<String> transformationTitles = List.of(
                "12-Week Transformation", "Body Recomposition Journey", "Weight Loss Success"
        );

        for (int t = 0; t < trainers.size(); t++) {
            Trainer trainer = trainers.get(t);
            List<TrainerMedia> mediaList = new ArrayList<>();

            // Add 2-3 videos (first one marked as featured)
            int videoCount = 2 + random.nextInt(2);
            for (int v = 0; v < videoCount; v++) {
                // Distribute videos evenly - each trainer gets different videos
                int videoIndex = (t + v) % videoUrls.size();
                String videoUrl = videoUrls.get(videoIndex);
                boolean isS3Video = videoUrl.contains("s3.") && videoUrl.contains("amazonaws.com");

                mediaList.add(TrainerMedia.builder()
                        .trainerId(trainer.getId())
                        .type(MediaType.VIDEO)
                        .mediaUrl(videoUrl)
                        .s3Key(isS3Video ? videoUrl.substring(videoUrl.indexOf(".com/") + 5) : null)
                        .thumbnailUrl(DEMO_THUMBNAIL_URLS.get(v % DEMO_THUMBNAIL_URLS.size()))
                        .title(videoTitles.get(Math.abs((trainer.hashCode() + v) % videoTitles.size())))
                        .description("Professional training session demonstrating proper form and technique")
                        .durationSeconds(10 + random.nextInt(6))
                        .displayOrder(v)
                        .likes(10 + random.nextInt(200))
                        .isDemo(!isS3Video)
                        .isFeatured(v == 0) // First video is featured
                        .createdAt(LocalDateTime.now().minusDays(random.nextInt(30)))
                        .build());
            }

            // Add 3-5 demo images
            int imageCount = 3 + random.nextInt(3);
            for (int i = 0; i < imageCount; i++) {
                int seed = trainer.hashCode() + i * 100;
                mediaList.add(TrainerMedia.builder()
                        .trainerId(trainer.getId())
                        .type(MediaType.IMAGE)
                        .mediaUrl("https://images.pexels.com/photos/" + getRandomFitnessPhotoId(seed) + "/pexels-photo-" + getRandomFitnessPhotoId(seed) + ".jpeg?auto=compress&cs=tinysrgb&w=800")
                        .thumbnailUrl("https://images.pexels.com/photos/" + getRandomFitnessPhotoId(seed) + "/pexels-photo-" + getRandomFitnessPhotoId(seed) + ".jpeg?auto=compress&cs=tinysrgb&w=400")
                        .title("Training Session " + (i + 1))
                        .description("Snapshot from a recent training session")
                        .displayOrder(videoCount + i)
                        .likes(5 + random.nextInt(100))
                        .isDemo(true)
                        .createdAt(LocalDateTime.now().minusDays(random.nextInt(60)))
                        .build());
            }

            // Add 1-2 transformation posts
            int transformationCount = 1 + random.nextInt(2);
            for (int tr = 0; tr < transformationCount; tr++) {
                int beforeSeed = trainer.hashCode() + tr * 200;
                int afterSeed = trainer.hashCode() + tr * 300;
                mediaList.add(TrainerMedia.builder()
                        .trainerId(trainer.getId())
                        .type(MediaType.TRANSFORMATION)
                        .mediaUrl("https://images.pexels.com/photos/" + getRandomFitnessPhotoId(afterSeed) + "/pexels-photo-" + getRandomFitnessPhotoId(afterSeed) + ".jpeg?auto=compress&cs=tinysrgb&w=800")
                        .thumbnailUrl("https://images.pexels.com/photos/" + getRandomFitnessPhotoId(afterSeed) + "/pexels-photo-" + getRandomFitnessPhotoId(afterSeed) + ".jpeg?auto=compress&cs=tinysrgb&w=400")
                        .beforeImageUrl("https://picsum.photos/seed/before-" + beforeSeed + "/400/600")
                        .afterImageUrl("https://picsum.photos/seed/after-" + afterSeed + "/400/600")
                        .title(transformationTitles.get(tr % transformationTitles.size()))
                        .description("Amazing transformation achieved through dedicated training and nutrition")
                        .displayOrder(videoCount + imageCount + tr)
                        .likes(50 + random.nextInt(300))
                        .isDemo(true)
                        .createdAt(LocalDateTime.now().minusDays(random.nextInt(90)))
                        .build());
            }

            mediaRepository.saveAll(mediaList);
        }

        log.info("Seeded demo media for {} trainers", trainers.size());
    }

    private String getRandomFitnessPhotoId(int seed) {
        // Real Pexels fitness photo IDs
        List<String> photoIds = List.of(
                "1552242", "2294361", "3253501", "3822906", "4162451",
                "3768916", "4754146", "5473182", "841130", "1954524",
                "2827392", "3076509", "3775566", "4498362", "6455927"
        );
        return photoIds.get(Math.abs(seed) % photoIds.size());
    }

    private List<User> seedUsers() {
        List<User> users = new ArrayList<>();
        String[] firstNames = {"Ava", "Liam", "Noah", "Mia", "Ella", "James", "Zoe", "Ethan", "Chloe", "Lucas"};
        String[] lastNames = {"Brown", "Smith", "Taylor", "Wilson", "Clark", "Martin", "Lee", "Walker", "Hall", "Young"};
        List<String> goals = List.of("weight_loss", "muscle_gain", "strength", "endurance", "mobility");
        List<String> activities = List.of("boxing", "yoga", "hiit", "pilates", "crossfit", "strength", "cardio");

        // Get category IDs for preferences
        List<String> categoryIds = categoryRepository.findAll().stream()
                .map(TrainingCategory::getId)
                .toList();
        List<String> subcategoryIds = subcategoryRepository.findAll().stream()
                .map(TrainingSubcategory::getId)
                .toList();

        for (int i = 0; i < 12; i++) {
            String first = firstNames[i % firstNames.length];
            String last = lastNames[i % lastNames.length];
            String email = ("user" + (i + 1) + "@nexfit.com").toLowerCase();

            User user = User.builder()
                    .name(first + " " + last)
                    .email(email)
                    .password(passwordEncoder.encode("Password@123"))
                    .phone("04" + (10000000 + random.nextInt(89999999)))
                    .role(User.UserRole.USER)
                    .isActive(true)
                    .emailVerified(true)
                    .dateOfBirth(LocalDate.now().minusYears(18 + random.nextInt(15)).minusDays(random.nextInt(365)))
                    .gender(randomEnum(User.Gender.class))
                    .trainerGenderPreference(randomEnum(User.TrainerGenderPreference.class))
                    .fitnessGoals(pickRandomList(goals, 2, 3))
                    .preferredActivities(pickRandomList(activities, 2, 4))
                    .experienceLevel(randomEnum(User.ExperienceLevel.class))
                    .selectedCategories(pickRandomList(categoryIds, 2, 4))
                    .selectedSubcategories(pickRandomList(subcategoryIds, 3, 6))
                    .build();

            users.add(user);
        }

        return userRepository.saveAll(users);
    }

    private List<Trainer> seedTrainers() {
        List<Trainer> trainers = new ArrayList<>();
        String[] firstNames = {"Olivia", "Mason", "Isla", "Jack", "Amelia", "Leo", "Ruby", "Henry", "Grace", "Owen"};
        String[] lastNames = {"Nguyen", "Harris", "Roberts", "Adams", "Baker", "Evans", "Carter", "King", "Parker", "Turner"};
        List<String> specializations = List.of(
                "Boxing", "Yoga", "HIIT", "Pilates", "CrossFit", "Strength Training", "Weight Loss",
                "Cardio Training", "Functional Training", "Rehabilitation", "Nutrition Coaching", "Mobility Training"
        );
        List<String> certifications = List.of("Cert III Fitness", "Cert IV Fitness", "ASCA Level 1", "First Aid", "Yoga Alliance 200");
        List<String> languages = List.of("English", "Mandarin", "Spanish", "Arabic", "Vietnamese", "Italian");
        List<LocationSeed> locations = List.of(
                new LocationSeed("Sydney", "NSW", -33.8688, 151.2093),
                new LocationSeed("Melbourne", "VIC", -37.8136, 144.9631),
                new LocationSeed("Brisbane", "QLD", -27.4698, 153.0251),
                new LocationSeed("Perth", "WA", -31.9505, 115.8605),
                new LocationSeed("Adelaide", "SA", -34.9285, 138.6007),
                new LocationSeed("Canberra", "ACT", -35.2809, 149.1300)
        );
        List<String> achievements = List.of(
                "National Fitness Award", "Top Trainer of the Year", "Marathon Finish", "Bodybuilding Finalist"
        );
        List<String> gymAffiliations = List.of("Anytime Fitness", "Fitness First", "F45 Training", "Jetts", "Snap Fitness");
        List<String> bios = List.of(
                "Passionate trainer focused on sustainable fitness and personalized coaching.",
                "Dedicated to helping clients achieve their fitness goals through evidence-based training.",
                "Specializing in body transformations with a focus on strength and nutrition.",
                "Former athlete turned coach, bringing competitive experience to every session.",
                "Holistic approach to fitness combining physical training with lifestyle optimization."
        );

        for (int i = 0; i < 30; i++) {
            String first = firstNames[i % firstNames.length];
            String last = lastNames[i % lastNames.length];
            LocationSeed loc = locations.get(random.nextInt(locations.size()));
            int experience = 1 + random.nextInt(12);

            List<Trainer.TrainerImage> gallery = new ArrayList<>();
            for (int g = 0; g < 5; g++) {
                gallery.add(Trainer.TrainerImage.builder()
                        .url("https://images.pexels.com/photos/" + getRandomFitnessPhotoId(i * 10 + g) + "/pexels-photo-" + getRandomFitnessPhotoId(i * 10 + g) + ".jpeg?auto=compress&cs=tinysrgb&w=600")
                        .likes(5 + random.nextInt(500))
                        .build());
            }

            List<Trainer.Achievement> achievementList = new ArrayList<>();
            for (int a = 0; a < 2; a++) {
                achievementList.add(Trainer.Achievement.builder()
                        .title(achievements.get(random.nextInt(achievements.size())))
                        .description("Recognized for exceptional coaching outcomes.")
                        .year(2015 + random.nextInt(10))
                        .build());
            }

            List<Trainer.TrainingLocation> trainingLocations = List.of(
                    Trainer.TrainingLocation.builder()
                            .name("Outdoor Sessions")
                            .address("Main Park")
                            .city(loc.city)
                            .state(loc.state)
                            .country("Australia")
                            .latitude(loc.lat + randomOffset())
                            .longitude(loc.lng + randomOffset())
                            .build()
            );

            String phone = "04" + (10000000 + random.nextInt(89999999));
            String instagramId = "trainer_" + UUID.randomUUID().toString().substring(0, 8);
            String whatsapp = "+61" + phone.substring(1);
            String website = "https://" + first.toLowerCase() + last.toLowerCase() + "fitness.com.au";
            String email = ("trainer" + (i + 1) + "@nexfit.com").toLowerCase();

            List<Trainer.ContactMethod> contactMethods = new ArrayList<>();
            contactMethods.add(Trainer.ContactMethod.builder()
                    .type("whatsapp")
                    .value(whatsapp)
                    .label("WhatsApp")
                    .isPrimary(true)
                    .build());
            contactMethods.add(Trainer.ContactMethod.builder()
                    .type("website")
                    .value(website)
                    .label("Book Online")
                    .isPrimary(false)
                    .build());
            contactMethods.add(Trainer.ContactMethod.builder()
                    .type("instagram")
                    .value(instagramId)
                    .label("Instagram")
                    .isPrimary(false)
                    .build());
            contactMethods.add(Trainer.ContactMethod.builder()
                    .type("email")
                    .value(email)
                    .label("Email")
                    .isPrimary(false)
                    .build());

            Trainer trainer = Trainer.builder()
                    .name(first + " " + last)
                    .email(email)
                    .password(passwordEncoder.encode("Password@123"))
                    .phone(phone)
                    .gender(randomEnum(User.Gender.class))
                    .profileImage("https://images.pexels.com/photos/" + getRandomFitnessPhotoId(i + 50) + "/pexels-photo-" + getRandomFitnessPhotoId(i + 50) + ".jpeg?auto=compress&cs=tinysrgb&w=400")
                    .coverImage("https://images.pexels.com/photos/" + getRandomFitnessPhotoId(i + 100) + "/pexels-photo-" + getRandomFitnessPhotoId(i + 100) + ".jpeg?auto=compress&cs=tinysrgb&w=1000")
                    .headline("Certified Fitness Coach")
                    .specializations(Set.copyOf(pickRandomList(specializations, 2, 4)))
                    .experience(experience)
                    .rating(BigDecimal.valueOf(3.5 + random.nextDouble() * 1.5).setScale(2, RoundingMode.HALF_UP))
                    .reviewCount(0)
                    .hourlyRate(BigDecimal.valueOf(60 + random.nextInt(90)))
                    .pricingMonthlySubscriptionUsd(BigDecimal.ONE)
                    .bio(bios.get(i % bios.size()))
                    .certifications(pickRandomList(certifications, 2, 3))
                    .instagramId(instagramId)
                    .latitude(loc.lat + randomOffset())
                    .longitude(loc.lng + randomOffset())
                    .address("Central " + loc.city)
                    .city(loc.city)
                    .state(loc.state)
                    .country("Australia")
                    .zipCode("2000")
                    .languages(Set.copyOf(pickRandomList(languages, 1, 3)))
                    .gymAffiliation(gymAffiliations.get(random.nextInt(gymAffiliations.size())))
                    .totalClients(50 + random.nextInt(200))
                    .transformations(10 + random.nextInt(60))
                    .sessionsCompleted(200 + random.nextInt(2000))
                    .yearsActive(experience)
                    .gallery(gallery)
                    .achievements(achievementList)
                    .trainingLocations(trainingLocations)
                    .whatsapp(whatsapp)
                    .website(website)
                    .contactMethods(contactMethods)
                    .isActive(true)
                    .isVerified(random.nextBoolean())
                    .status(TrainerStatus.APPROVED)
                    .hasDiscoverVideo(true)
                    .profileInitialized(true)
                    .submittedAt(LocalDateTime.now().minusDays(random.nextInt(10)))
                    .approvedAt(LocalDateTime.now().minusDays(random.nextInt(5)))
                    .build();

            trainers.add(trainer);
        }

        return trainerRepository.saveAll(trainers);
    }

    private void seedReviews(List<User> users, List<Trainer> trainers) {
        List<Review> reviews = new ArrayList<>();
        List<String> reviewComments = List.of(
                "Great session! Learned a lot and felt motivated.",
                "Amazing trainer, very professional and knowledgeable.",
                "Really helped me improve my form and technique.",
                "Best workout I've had in years!",
                "Very patient and encouraging. Highly recommend!",
                "Pushed me beyond my limits in the best way possible.",
                "Excellent communication and always on time.",
                "Tailored the workout perfectly to my goals."
        );

        for (Trainer trainer : trainers) {
            int reviewCount = 3 + random.nextInt(6);
            for (int i = 0; i < reviewCount; i++) {
                User user = users.get(random.nextInt(users.size()));
                reviews.add(Review.builder()
                        .trainerId(trainer.getId())
                        .userId(user.getId())
                        .rating(3 + random.nextInt(3))
                        .comment(reviewComments.get(random.nextInt(reviewComments.size())))
                        .build());
            }
        }
        reviewRepository.saveAll(reviews);

        for (Trainer trainer : trainers) {
            List<Review> trainerReviews = reviewRepository.findByTrainerId(trainer.getId());
            double average = trainerReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            trainer.setRating(BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
            trainer.setReviewCount(trainerReviews.size());
            trainerRepository.save(trainer);
        }
    }

    private <T extends Enum<T>> T randomEnum(Class<T> enumClass) {
        T[] values = enumClass.getEnumConstants();
        return values[random.nextInt(values.length)];
    }

    private <T> List<T> pickRandomList(List<T> source, int min, int max) {
        int count = min + random.nextInt(max - min + 1);
        List<T> copy = new ArrayList<>(source);
        List<T> picked = new ArrayList<>();
        for (int i = 0; i < count && !copy.isEmpty(); i++) {
            picked.add(copy.remove(random.nextInt(copy.size())));
        }
        return picked;
    }

    private double randomOffset() {
        return (random.nextDouble() - 0.5) * 0.02;
    }

    private record LocationSeed(String city, String state, double lat, double lng) {}

    private record CategoryData(String name, String icon, String description, String imageUrl, List<String> subcategories) {}
}
