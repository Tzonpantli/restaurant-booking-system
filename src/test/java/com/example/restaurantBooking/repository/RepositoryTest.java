package com.example.restaurantBooking.repository;

import com.example.restaurantBooking.model.Booking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository = new BookingRepository();
    }

    @Test
    void testAddBooking() {
        Booking booking = new Booking("John Doe", 4, LocalDate.of(2024, 7, 12), LocalTime.of(18, 0));
        bookingRepository.addBooking(booking);
        assertEquals(1, bookingRepository.getBookingsForDay(LocalDate.of(2024, 7, 12)).size());
    }

    @Test
    void testGetBookingsForDay() {
        Booking booking1 = new Booking("John Doe", 4, LocalDate.of(2024, 7, 12), LocalTime.of(18, 0));
        Booking booking2 = new Booking("Jane Doe", 2, LocalDate.of(2024, 7, 12), LocalTime.of(20, 0));
        bookingRepository.addBooking(booking1);
        bookingRepository.addBooking(booking2);
        List<Booking> bookings = bookingRepository.getBookingsForDay(LocalDate.of(2024, 7, 12));
        assertEquals(2, bookings.size());
    }

    @Test
    void testIsBookingOverlapping() {
        Booking booking1 = new Booking("John Doe", 4, LocalDate.of(2024, 7, 12), LocalTime.of(18, 0));
        bookingRepository.addBooking(booking1);
        boolean isOverlapping = bookingRepository.isBookingOverlapping(LocalDate.of(2024, 7, 12), LocalTime.of(18, 0));
        assertTrue(isOverlapping);
    }

    @Test
    void testIsWithinOperatingHours() {
        assertTrue(bookingRepository.isWithinOperatingHours(LocalTime.of(8, 0)));
        assertTrue(bookingRepository.isWithinOperatingHours(LocalTime.of(20, 0)));
        assertFalse(bookingRepository.isWithinOperatingHours(LocalTime.of(22, 0)));
    }

    @Test
    void testGetAvailableSlots() {
        Booking booking1 = new Booking("John Doe", 4, LocalDate.of(2024, 7, 12), LocalTime.of(18, 0));
        bookingRepository.addBooking(booking1);
        List<LocalTime> availableSlots = bookingRepository.getAvailableSlots(LocalDate.of(2024, 7, 12));
        assertFalse(availableSlots.contains(LocalTime.of(18, 0)));
        assertTrue(availableSlots.contains(LocalTime.of(8, 0)));
    }
}
