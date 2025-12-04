package org.example.eliteevents.services;

import org.example.eliteevents.models.Booking;
import org.example.eliteevents.models.Venue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConflictDetectionService {
    private DatabaseService databaseService;

    public ConflictDetectionService() {
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * Check for booking conflicts for a specific venue and time period
     */
    public ConflictCheckResult checkBookingConflict(int venueId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeBookingId) {
        List<Booking> conflictingBookings = new ArrayList<>();
        List<String> conflictReasons = new ArrayList<>();

        try {
            List<Booking> allBookings = databaseService.getAllBookings();

            for (Booking existingBooking : allBookings) {
                // Skip the booking we're updating (if provided)
                if (excludeBookingId != null && existingBooking.getId() == excludeBookingId) {
                    continue;
                }

                // Check if it's the same venue
                if (existingBooking.getVenue().getId() == venueId) {
                    // Check for time overlap
                    if (hasTimeOverlap(existingBooking.getStartDateTime(), existingBooking.getEndDateTime(), startTime, endTime)) {
                        conflictingBookings.add(existingBooking);

                        String conflictReason = String.format(
                                "Venue already booked for %s from %s to %s",
                                existingBooking.getEventType(),
                                existingBooking.getStartDateTime().format(java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a")),
                                existingBooking.getEndDateTime().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))
                        );
                        conflictReasons.add(conflictReason);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error checking booking conflicts: " + e.getMessage());
            e.printStackTrace();
        }

        return new ConflictCheckResult(conflictingBookings, conflictReasons);
    }

    /**
     * Check if two time periods overlap
     */
    private boolean hasTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }

    /**
     * Get available time slots for a venue on a specific date
     */
    public List<TimeSlot> getAvailableTimeSlots(int venueId, java.time.LocalDate date) {
        List<TimeSlot> availableSlots = new ArrayList<>();

        try {
            List<Booking> venueBookings = getBookingsForVenueOnDate(venueId, date);

            // Define business hours (9 AM to 9 PM)
            LocalDateTime dayStart = date.atTime(9, 0);
            LocalDateTime dayEnd = date.atTime(21, 0);

            // Start with the entire day as available
            availableSlots.add(new TimeSlot(dayStart, dayEnd));

            // Remove booked slots
            for (Booking booking : venueBookings) {
                availableSlots = removeBookedTimeFromSlots(availableSlots, booking.getStartDateTime(), booking.getEndDateTime());
            }

        } catch (Exception e) {
            System.err.println("Error getting available time slots: " + e.getMessage());
            e.printStackTrace();
        }

        return availableSlots;
    }

    private List<Booking> getBookingsForVenueOnDate(int venueId, java.time.LocalDate date) {
        List<Booking> result = new ArrayList<>();
        try {
            List<Booking> allBookings = databaseService.getAllBookings();
            for (Booking booking : allBookings) {
                if (booking.getVenue().getId() == venueId &&
                        booking.getStartDateTime().toLocalDate().equals(date) &&
                        "CONFIRMED".equals(booking.getStatus())) {
                    result.add(booking);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<TimeSlot> removeBookedTimeFromSlots(List<TimeSlot> availableSlots, LocalDateTime bookedStart, LocalDateTime bookedEnd) {
        List<TimeSlot> updatedSlots = new ArrayList<>();

        for (TimeSlot slot : availableSlots) {
            if (slot.getEnd().isBefore(bookedStart) || slot.getStart().isAfter(bookedEnd)) {
                // No overlap, keep the slot
                updatedSlots.add(slot);
            } else {
                // There's overlap, split the slot
                if (slot.getStart().isBefore(bookedStart)) {
                    updatedSlots.add(new TimeSlot(slot.getStart(), bookedStart));
                }
                if (slot.getEnd().isAfter(bookedEnd)) {
                    updatedSlots.add(new TimeSlot(bookedEnd, slot.getEnd()));
                }
            }
        }

        return updatedSlots;
    }

    /**
     * Check if a venue is available for the given time period
     */
    public boolean isVenueAvailable(int venueId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeBookingId) {
        ConflictCheckResult result = checkBookingConflict(venueId, startTime, endTime, excludeBookingId);
        return result.getConflictingBookings().isEmpty();
    }

    // Data classes for conflict detection results
    public static class ConflictCheckResult {
        private final List<Booking> conflictingBookings;
        private final List<String> conflictReasons;

        public ConflictCheckResult(List<Booking> conflictingBookings, List<String> conflictReasons) {
            this.conflictingBookings = conflictingBookings;
            this.conflictReasons = conflictReasons;
        }

        public List<Booking> getConflictingBookings() { return conflictingBookings; }
        public List<String> getConflictReasons() { return conflictReasons; }
        public boolean hasConflicts() { return !conflictingBookings.isEmpty(); }
    }

    public static class TimeSlot {
        private final LocalDateTime start;
        private final LocalDateTime end;

        public TimeSlot(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }

        public String getFormattedTime() {
            return start.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a")) + " - " +
                    end.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
        }

        @Override
        public String toString() {
            return getFormattedTime();
        }
    }
}