package com.example.restaurantBooking;

import com.example.restaurantBooking.controller.BookingController;
import com.example.restaurantBooking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RestaurantBookingApp {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantBookingApp.class);

    public static void main(String[] args) {

        BookingRepository repository = new BookingRepository();

        BookingController bookingController = new BookingController(repository);

        //init app in port 8080
        bookingController.startServer(8080);

        // Add a shutdown hook to log a message when the server is shutting down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.info("Server shutting down")));
    }
}
