package com.example.restaurantBooking.controller;

import com.example.restaurantBooking.model.Booking;
import com.example.restaurantBooking.repository.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RequestParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingControllerTest {
/*
    private MuServer server;
    private BookingRepository bookingRepository;
    private ObjectMapper objectMapper;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        bookingRepository = new BookingRepository();
        BookingController bookingController = new BookingController(bookingRepository);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        client = HttpClient.newHttpClient();

        server = MuServerBuilder.muServer()
                .withHttpPort(8081)
                .addHandler(Method.POST, "/bookings/request-a-booking", bookingController::handleCreateBooking)
                .addHandler(Method.GET, "/bookings/request-a-booking/all-bookings-at-date", bookingController::handleGetBookings)
                .start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    private HttpResponse<String> sendHttpRequest(String method, String urlString, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(new URI(urlString))
                .header("Content-Type", "application/json");

        if (method.equalsIgnoreCase("POST")) {
            builder = builder.POST(HttpRequest.BodyPublishers.ofString(body));
        } else if (method.equalsIgnoreCase("GET")) {
            builder = builder.GET();
        }

        HttpRequest request = builder.build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void testCreateBooking() throws Exception {
        Booking booking = new Booking("John Doe", 4, LocalDate.of(2024, 7, 12), LocalTime.of(18, 0));
        String bookingJson = objectMapper.writeValueAsString(booking);

        HttpResponse<String> response = sendHttpRequest("POST", "http://localhost:8081/bookings/request-a-booking", bookingJson);
        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Booking created"));
    }

    @Test
    void testCreateBookingWithConflict() throws Exception {
        Booking booking = new Booking("John Doe", 4, LocalDate.of(2024, 7, 12), LocalTime.of(18, 0));
        bookingRepository.addBooking(booking);
        String bookingJson = objectMapper.writeValueAsString(booking);

        HttpResponse<String> response = sendHttpRequest("POST", "http://localhost:8081/bookings/request-a-booking", bookingJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Booking conflict"));
        assertTrue(response.body().contains("availableBookings"));
    }

    @Test
    void testGetBookings() throws Exception {
        Booking booking = new Booking("John Doe", 4, LocalDate.of(2024, 7, 12), LocalTime.of(18, 0));
        bookingRepository.addBooking(booking);

        HttpResponse<String> response = sendHttpRequest("GET", "http://localhost:8081/bookings/request-a-booking/all-bookings-at-date?date=2024-07-12", null);
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("John Doe"));
    }*/

    private BookingRepository bookingRepository;
    private BookingController bookingController;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        bookingController = new BookingController(bookingRepository);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void handleCreateBooking_Success() throws Exception {
        MuRequest request = mock(MuRequest.class);
        MuResponse response = mock(MuResponse.class);

        Booking booking = new Booking("John Doe", 4, LocalDate.now(), LocalTime.of(18, 0));
        String requestBody = objectMapper.writeValueAsString(booking);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));

        when(request.inputStream()).thenReturn(Optional.of(inputStream));
        when(bookingRepository.isWithinOperatingHours(any(LocalTime.class))).thenReturn(true);
        when(bookingRepository.isBookingOverlapping(any(LocalDate.class), any(LocalTime.class))).thenReturn(false);

        bookingController.handleCreateBooking(request, response, Map.of());

        verify(response).status(201);
        verify(response).contentType("application/json");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).write(responseCaptor.capture());

        String jsonResponse = responseCaptor.getValue();
        assertTrue(jsonResponse.contains("Booking created"));
    }

    @Test
    void handleCreateBooking_InvalidTime() throws Exception {
        MuRequest request = mock(MuRequest.class);
        MuResponse response = mock(MuResponse.class);

        Booking booking = new Booking("John Doe", 4, LocalDate.now(), LocalTime.of(6, 0)); // Invalid time
        String requestBody = objectMapper.writeValueAsString(booking);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));

        when(request.inputStream()).thenReturn(Optional.of(inputStream));
        when(bookingRepository.isWithinOperatingHours(any(LocalTime.class))).thenReturn(false);

        bookingController.handleCreateBooking(request, response, Map.of());

        verify(response).status(400);
        verify(response).contentType("application/json");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).write(responseCaptor.capture());

        String jsonResponse = responseCaptor.getValue();
        assertTrue(jsonResponse.contains("Invalid booking time"));
    }

    @Test
    void handleCreateBooking_BookingConflict() throws Exception {
        MuRequest request = mock(MuRequest.class);
        MuResponse response = mock(MuResponse.class);

        Booking booking = new Booking("John Doe", 4, LocalDate.now(), LocalTime.of(18, 0));
        String requestBody = objectMapper.writeValueAsString(booking);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));

        when(request.inputStream()).thenReturn(Optional.of(inputStream));
        when(bookingRepository.isWithinOperatingHours(any(LocalTime.class))).thenReturn(true);
        when(bookingRepository.isBookingOverlapping(any(LocalDate.class), any(LocalTime.class))).thenReturn(true);
        when(bookingRepository.getAvailableSlots(any(LocalDate.class))).thenReturn(List.of(LocalTime.of(8, 0), LocalTime.of(10, 0)));

        bookingController.handleCreateBooking(request, response, Map.of());

        verify(response).status(400);
        verify(response).contentType("application/json");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).write(responseCaptor.capture());

        String jsonResponse = responseCaptor.getValue();
        assertTrue(jsonResponse.contains("Booking conflict"));
        assertTrue(jsonResponse.contains("08:00"));
        assertTrue(jsonResponse.contains("10:00"));
    }

    @Test
    void handleGetBookings_Success() throws Exception {
        MuRequest request = mock(MuRequest.class);
        MuResponse response = mock(MuResponse.class);

        RequestParameters queryParams = mock(RequestParameters.class);
        when(request.query()).thenReturn(queryParams);
        when(queryParams.get("date")).thenReturn("2024-07-12");

        LocalDate date = LocalDate.of(2024, 7, 12);
        Booking booking = new Booking("John Doe", 4, date, LocalTime.of(18, 0));
        when(bookingRepository.getBookingsForDay(date)).thenReturn(List.of(booking));

        bookingController.handleGetBookings(request, response, Map.of());

        verify(response).contentType("application/json");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).write(responseCaptor.capture());

        String jsonResponse = responseCaptor.getValue();
        assertTrue(jsonResponse.contains("John Doe"));
    }

    @Test
    void handleGetBookings_InvalidDate() throws Exception {
        MuRequest request = mock(MuRequest.class);
        MuResponse response = mock(MuResponse.class);

        RequestParameters queryParams = mock(RequestParameters.class);
        when(request.query()).thenReturn(queryParams);
        when(queryParams.get("date")).thenReturn("invalid-date");

        bookingController.handleGetBookings(request, response, Map.of());

        verify(response).status(400);
        verify(response).contentType("application/json");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).write(responseCaptor.capture());

        String jsonResponse = responseCaptor.getValue();
        assertTrue(jsonResponse.contains("Invalid Date Format"));
    }

    @Test
    void handleGetBookings_MissingDate() throws Exception {
        MuRequest request = mock(MuRequest.class);
        MuResponse response = mock(MuResponse.class);

        RequestParameters queryParams = mock(RequestParameters.class);
        when(request.query()).thenReturn(queryParams);
        when(queryParams.get("date")).thenReturn(null);

        bookingController.handleGetBookings(request, response, Map.of());

        verify(response).status(400);
        verify(response).contentType("application/json");

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).write(responseCaptor.capture());

        String jsonResponse = responseCaptor.getValue();
        assertTrue(jsonResponse.contains("Date parameter is missing"));
    }
}
