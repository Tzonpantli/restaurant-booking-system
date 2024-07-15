package com.example.restaurantBooking.exception;

import com.example.restaurantBooking.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testCreateErrorResponse() throws Exception {
        String errorResponse = ErrorFactory.createErrorResponse(400, "Error occurred", "Details about the error", null);
        ErrorResponse response = objectMapper.readValue(errorResponse, ErrorResponse.class);
        assertEquals(400, response.getStatusCode());
        assertEquals("Error occurred", response.getMessage());
        assertEquals("Details about the error", response.getDetails());
        assertTrue(response.getAvailableBookings() == null || response.getAvailableBookings().isEmpty());

        assertTrue(!errorResponse.contains("availableBookings"));
    }

    @Test
    void testCreateErrorResponseWithAvailableBookings() throws Exception {
        List<String> availableBookings = Arrays.asList("08:00", "10:00");
        String errorResponse = ErrorFactory.createErrorResponse(400, "Booking conflict", "The requested time slot overlaps with an existing booking.", availableBookings);
        ErrorResponse response = objectMapper.readValue(errorResponse, ErrorResponse.class);
        assertEquals(400, response.getStatusCode());
        assertEquals("Booking conflict", response.getMessage());
        assertEquals("The requested time slot overlaps with an existing booking.", response.getDetails());
        assertEquals(availableBookings, response.getAvailableBookings());

        assertTrue(errorResponse.contains("availableBookings"));
    }
}