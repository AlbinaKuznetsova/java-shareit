package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingDtoOut toBookingDtoOut(Booking booking);

    List<BookingDtoOut> toBookingDtoOut(List<Booking> bookings);

    @Mapping(target = "id", source = "bookingDtoIn.id")
    Booking toBooking(BookingDtoIn bookingDtoIn, User booker, Item item);
}
