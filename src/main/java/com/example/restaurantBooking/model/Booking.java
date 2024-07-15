package com.example.restaurantBooking.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Model representing a booking, using lombok.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    private String customerName;

    private int tableSize;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime time;
}
