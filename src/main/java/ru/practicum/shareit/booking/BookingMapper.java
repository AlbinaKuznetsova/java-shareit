package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingDto toBookingDto(Booking booking);

    List<BookingDto> toBookingDto(List<Booking> bookings);

    @Mapping(target = "id", source = "bookingDto.id")
    Booking toBooking(BookingDto bookingDto, User booker, Item item);
}
