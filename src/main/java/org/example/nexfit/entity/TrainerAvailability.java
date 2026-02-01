package org.example.nexfit.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Document(collection = "trainer_availability")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerAvailability {
    
    @Id
    private String id;
    
    @Indexed
    private String trainerId;
    
    // Regular weekly schedule
    @Builder.Default
    private Map<DayOfWeek, List<TimeSlot>> weeklySchedule = new HashMap<>();
    
    // Specific date overrides (for holidays, special availability)
    @Builder.Default
    private Map<LocalDate, List<TimeSlot>> dateOverrides = new HashMap<>();
    
    // Blocked dates (vacation, unavailable)
    @Builder.Default
    private Set<LocalDate> blockedDates = new HashSet<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private LocalTime startTime;
        private LocalTime endTime;
        private Boolean available;
        private Integer maxCapacity; // For group sessions
        private Integer currentBookings;
        
        @Builder.Default
        private Boolean isRecurring = true;
        
        public boolean isAvailable() {
            return available && (maxCapacity == null || currentBookings < maxCapacity);
        }
    }
    
    // Helper method to get available slots for a specific date
    public List<TimeSlot> getAvailableSlotsForDate(LocalDate date) {
        // Check if date is blocked
        if (blockedDates.contains(date)) {
            return Collections.emptyList();
        }
        
        // Check for date-specific overrides first
        if (dateOverrides.containsKey(date)) {
            return dateOverrides.get(date).stream()
                .filter(TimeSlot::isAvailable)
                .toList();
        }
        
        // Fall back to weekly schedule
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return weeklySchedule.getOrDefault(dayOfWeek, Collections.emptyList())
            .stream()
            .filter(TimeSlot::isAvailable)
            .toList();
    }
}
