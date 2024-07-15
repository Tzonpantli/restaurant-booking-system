package com.example.restaurantBooking.repository;

import com.example.restaurantBooking.model.Booking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class handles storing and managing restaurant bookings.
 *
 * Note: This implementation uses an in-memory list to store bookings
 * instead of a real database to simplify the exercise.
 */
public class BookingRepository {
    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);
    private final List<Booking> bookings = new ArrayList<>();

    /**
     * Adds a new booking to the list.
     *
     * @param booking the booking to add
     */
    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    /**
     * Gets all bookings for a specific date.
     *
     * @param date the date to get bookings for
     * @return a list of bookings for the specified date
     */
    public List<Booking> getBookingsForDay(LocalDate date) {
        return bookings.stream()
                .filter(booking -> booking.getDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a new booking overlaps with any existing bookings.
     *
     * @param date the date of the new booking
     * @param time the time of the new booking
     * @return true if the new booking overlaps with an existing one, false otherwise
     */
    public boolean isBookingOverlapping(LocalDate date, LocalTime time) {
        LocalDateTime newBookingDateTime = LocalDateTime.of(date, time);
        LocalDateTime newBookingEndTime = newBookingDateTime.plusHours(2);
        return bookings.stream().anyMatch(existingBooking -> {
            LocalDateTime existingBookingDateTime = LocalDateTime.of(existingBooking.getDate(), existingBooking.getTime());
            LocalDateTime existingBookingEndTime = existingBookingDateTime.plusHours(2);
            return (newBookingDateTime.isBefore(existingBookingEndTime) && newBookingEndTime.isAfter(existingBookingDateTime));
        });
    }

    /**
     * Checks if a given time is within the restaurant's operating hours.
     *
     * @param time the time to check
     * @return true if the time is within operating hours, false otherwise
     */
    public boolean isWithinOperatingHours(LocalTime time) {
        return !time.isBefore(OPENING_TIME) && !time.isAfter(CLOSING_TIME.minusHours(2));
    }

    /**
     * Gets the available booking slots for a specific date.
     *
     * @param date the date to check for available slots
     * @return a list of available booking slots
     */
    public List<LocalTime> getAvailableSlots(LocalDate date) {
        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime currentTime = OPENING_TIME;
        while (!currentTime.isAfter(CLOSING_TIME.minusHours(2))) {
            if (!isBookingOverlapping(date, currentTime)) {
                availableSlots.add(currentTime);
            }
            currentTime = currentTime.plusHours(2);
        }
        return availableSlots;
    }
}