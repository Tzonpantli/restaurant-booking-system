# restaurant-booking-system

The **Restaurant Booking System** is a simple application to manage restaurant bookings. It allows customers to request bookings and restaurant owners to view all bookings for a specific day. The application is built using Java and MuServer for handling HTTP requests, Jackson for JSON serialization and deserialization, and JUnit and Mockito for unit testing.

## Features

- **Request Bookings**: Customers can request a booking by providing their name, table size, date, and time.
- **View Bookings**: Restaurant owners can view all bookings for a specific day.
- **Validation and Error Handling**: The application validates booking requests to ensure they fall within operating hours and do not overlap with existing bookings. It also provides available time slots in case of conflicts.
