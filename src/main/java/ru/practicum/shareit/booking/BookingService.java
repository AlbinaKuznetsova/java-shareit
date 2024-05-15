package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.Collection;

public interface BookingService {

    Booking createBooking(BookingDto bookingDto, int userId);

    Booking changeStatus(int userId, Integer bookingId, boolean approved);

    Booking getBookingById(int userId, Integer bookingId);

    Collection<Booking> getAllForBooker(int userId, String state);

    Collection<Booking> getAllForOwner(int userId, String state);

}
