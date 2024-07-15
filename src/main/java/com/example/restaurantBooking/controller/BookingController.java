package com.example.restaurantBooking.controller;

import com.example.restaurantBooking.exception.ErrorFactory;
import com.example.restaurantBooking.model.Booking;
import com.example.restaurantBooking.repository.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller to handle restaurant bookings.
 */
public class BookingController {
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for BookingController.
     *
     * @param repository the booking repository
     */
    public BookingController(BookingRepository repository) {
        this.bookingRepository = repository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Starts the MuServer.
     *
     * @param port the port on which the server will listen
     */
    public void startServer(int port) {
        MuServer server = MuServerBuilder.httpServer()
                .withHttpPort(port)
                .addHandler(Method.POST, "/bookings/request-a-booking", this::handleCreateBooking)
                .addHandler(Method.GET, "/bookings/all-bookings-at-date", this::handleGetBookings)
                .start();

        logger.info("Server started at " + server.uri());

        /**
         * Registers a shutdown hook to stop the server  is shutting down.
         */
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

    /**
     * Handles the creation of a booking.
     *
     * @param request    the request
     * @param response   the response
     * @param pathParams the path parameters
     */
    public void handleCreateBooking(MuRequest request, MuResponse response, Map<String, String> pathParams) {
        try (var inputStream = request.inputStream().orElseThrow()) {
            String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            logger.info("Received request body: " + requestBody);

            if (requestBody.isEmpty()) {
                throw new IOException("Request body is empty");
            }

            Booking booking = objectMapper.readValue(requestBody, Booking.class);
            validateAndProcessBooking(booking, response);
        } catch (Exception e) {
            handleException(e, response,"Invalid request body");
        }
    }

    /**
     * Validates and processes a booking, corresponds to user story 1.
     *
     * @param booking  the booking to validate and process
     * @param response the HTTP response
     * @throws Exception if an error occurs while processing the booking
     */
    private void validateAndProcessBooking(Booking booking, MuResponse response) throws Exception {
        if (!bookingRepository.isWithinOperatingHours(booking.getTime())) {
            sendErrorResponse(response,
                    "Invalid booking time",
                    "The booking time is outside the operating hours (08:00 to 22:00)",
                    null);
            return;
        }

        if (bookingRepository.isBookingOverlapping(booking.getDate(), booking.getTime())) {
            List<String> availableSlots = bookingRepository.getAvailableSlots(booking.getDate())
                    .stream()
                    .map(LocalTime::toString)
                    .collect(Collectors.toList());
            sendErrorResponse(response,
                    "Booking conflict",
                    "The requested time slot overlaps with an existing booking.",
                    availableSlots);
            return;
        }

        try {
            bookingRepository.addBooking(booking);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        response.status(201);
        response.contentType("application/json");
        response.write(objectMapper.writeValueAsString(Map.of("message", "Booking created")));
    }

    /**
     * Handles retrieving bookings for a specific date, corresponds to user story 2.
     *
     * @param request    the HTTP request
     * @param response   the HTTP response
     * @param pathParams the path parameters
     */
    public void handleGetBookings(MuRequest request, MuResponse response, Map<String, String> pathParams) {
        try {
            String date = request.query().get("date");
            logger.info("Received date parameter: " + date);

            if (date == null || date.isEmpty()) {
                throw new IllegalArgumentException("Date parameter is missing");
            }

            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            List<Booking> bookings = bookingRepository.getBookingsForDay(localDate);
            response.contentType("application/json");
            response.write(objectMapper.writeValueAsString(bookings));
        } catch (Exception e) {
            handleException(e, response, "Invalid Date Format");
        }
    }

    /**
     * Handles exceptions thrown during request processing.
     *
     * @param e               the exception thrown
     * @param response        the HTTP response
     * @param defaultMessage  the default message
     */
    private void handleException(Exception e, MuResponse response, String defaultMessage) {
        logger.error("Error processing request", e);
        try {
            sendErrorResponse(response, defaultMessage, e.getMessage(), null);
        } catch (IOException ioException) {
            logger.error("Error writing error response", ioException);
        }
    }

    /**
     * Sends an error response.
     *
     * @param response        the HTTP response
     * @param error           the error message
     * @param message         the detailed message
     * @param availableSlots  the available slots in case of booking conflict
     * @throws IOException if an error occurs while writing the response
     */
    private void sendErrorResponse(MuResponse response, String error, String message, List<String> availableSlots) throws IOException {
        response.status(400);
        response.contentType("application/json");
        response.write(ErrorFactory.createErrorResponse(
                400,
                error,
                message,
                availableSlots));
    }
}