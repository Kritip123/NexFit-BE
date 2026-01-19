package org.example.trainerhub.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trainerhub.entity.Review;
import org.example.trainerhub.entity.Trainer;
import org.example.trainerhub.entity.User;
import org.example.trainerhub.repository.ReviewRepository;
import org.example.trainerhub.repository.TrainerRepository;
import org.example.trainerhub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final Random random = new Random();
    
    @Override
    public void run(String... args) {
        if (trainerRepository.count() > 0 || userRepository.count() > 0 || reviewRepository.count() > 0) {
            log.info("Seeder skipped: data already exists.");
            return;
        }
        
        List<User> users = seedUsers();
        List<Trainer> trainers = seedTrainers();
        seedReviews(users, trainers);
        
        log.info("Seeder completed: {} users, {} trainers, {} reviews",
                users.size(), trainers.size(), reviewRepository.count());
    }
    
    private List<User> seedUsers() {
        List<User> users = new ArrayList<>();
        String[] firstNames = {"Ava", "Liam", "Noah", "Mia", "Ella", "James", "Zoe", "Ethan", "Chloe", "Lucas"};
        String[] lastNames = {"Brown", "Smith", "Taylor", "Wilson", "Clark", "Martin", "Lee", "Walker", "Hall", "Young"};
        List<String> goals = List.of("weight_loss", "muscle_gain", "strength", "endurance", "mobility");
        List<String> activities = List.of("boxing", "yoga", "hiit", "pilates", "crossfit", "strength", "cardio");
        
        for (int i = 0; i < 12; i++) {
            String first = firstNames[i % firstNames.length];
            String last = lastNames[i % lastNames.length];
            String email = ("user" + (i + 1) + "@trainerhub.com").toLowerCase();
            
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
        
        for (int i = 0; i < 30; i++) {
            String first = firstNames[i % firstNames.length];
            String last = lastNames[i % lastNames.length];
            LocationSeed loc = locations.get(random.nextInt(locations.size()));
            int experience = 1 + random.nextInt(12);
            
            List<Trainer.TrainerImage> gallery = new ArrayList<>();
            for (int g = 0; g < 5; g++) {
                gallery.add(Trainer.TrainerImage.builder()
                        .url("https://picsum.photos/seed/trainer-" + i + "-" + g + "/600/800")
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

            // Generate contact methods
            String phone = "04" + (10000000 + random.nextInt(89999999));
            String instagramId = "trainer_" + UUID.randomUUID().toString().substring(0, 8);
            String whatsapp = "+61" + phone.substring(1);
            String website = "https://" + first.toLowerCase() + last.toLowerCase() + "fitness.com.au";
            String email = ("trainer" + (i + 1) + "@trainerhub.com").toLowerCase();

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
                    .phone(phone)
                    .gender(randomEnum(User.Gender.class))
                    .profileImage("https://picsum.photos/seed/trainer-profile-" + i + "/400/400")
                    .coverImage("https://picsum.photos/seed/trainer-cover-" + i + "/1000/600")
                    .specializations(Set.copyOf(pickRandomList(specializations, 2, 4)))
                    .experience(experience)
                    .rating(BigDecimal.valueOf(3.5 + random.nextDouble() * 1.5).setScale(2, RoundingMode.HALF_UP))
                    .reviewCount(0)
                    .hourlyRate(BigDecimal.valueOf(60 + random.nextInt(90)))
                    .bio("Passionate trainer focused on sustainable fitness and personalized coaching.")
                    .certifications(pickRandomList(certifications, 2, 3))
                    .instagramId(instagramId)
                    .latitude(loc.lat)
                    .longitude(loc.lng)
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
                    .build();
            
            trainers.add(trainer);
        }
        
        return trainerRepository.saveAll(trainers);
    }
    
    private void seedReviews(List<User> users, List<Trainer> trainers) {
        List<Review> reviews = new ArrayList<>();
        for (Trainer trainer : trainers) {
            int reviewCount = 3 + random.nextInt(6);
            for (int i = 0; i < reviewCount; i++) {
                User user = users.get(random.nextInt(users.size()));
                reviews.add(Review.builder()
                        .trainerId(trainer.getId())
                        .userId(user.getId())
                        .rating(3 + random.nextInt(3))
                        .comment("Great session! Learned a lot and felt motivated.")
                        .build());
            }
        }
        reviewRepository.saveAll(reviews);
        
        // Update trainer ratings after seeding reviews
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
    
    private record LocationSeed(String city, String state, double lat, double lng) {
    }
}
