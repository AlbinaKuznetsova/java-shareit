package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingService {

    BookingDtoOut createBooking(BookingDtoIn bookingDtoIn, int userId);

    BookingDtoOut changeStatus(int userId, Integer bookingId, boolean approved);

    BookingDtoOut getBookingById(int userId, Integer bookingId);

    Collection<BookingDtoOut> getAllForBooker(int userId, String state);

    Collection<BookingDtoOut> getAllForOwner(int userId, String state);

    BookingDtoForItem getNextBooking(Integer itemId, LocalDateTime now, Status status);

    BookingDtoForItem getLastBooking(Integer itemId, LocalDateTime now, Status status);

    Booking getBookingForComment(Integer userId, Integer itemId);

}
