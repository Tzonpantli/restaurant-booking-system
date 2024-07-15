package com.example.restaurantBooking.exception;

import com.example.restaurantBooking.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Factory for creating error responses.
 */
public class ErrorFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates an error response.
     *
     * @param statusCode        the HTTP status code
     * @param errorMessage      the error message
     * @param details           the error details
     * @param availableBookings the available booking slots in case of conflict
     * @return the error response in JSON format
     */
    public static String createErrorResponse(int statusCode, String errorMessage, String details, List<String> availableBookings) {
        try {
            ErrorResponse errorResponse = new ErrorResponse(statusCode, errorMessage, details, availableBookings);
            return objectMapper.writeValueAsString(errorResponse);
        } catch (Exception e) {
            return "{\"statusCode\":500,\"message\":\"Unexpected error\",\"details\":\"" + e.getMessage() + "\"}";
        }
    }
}