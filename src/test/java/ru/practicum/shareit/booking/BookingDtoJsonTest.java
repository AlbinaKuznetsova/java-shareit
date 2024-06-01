package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserMapperImpl;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

@JsonTest
public class BookingDtoJsonTest {
    @Autowired
    private JacksonTester<BookingDtoOut> json;

    @Test
    void testBookingDto() throws Exception {
        BookingMapper bookingMapper = new BookingMapperImpl();
        UserMapper userMapper = new UserMapperImpl();
        UserDto user = new UserDto();
        user.setId(1);
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");

        Item item = new Item(1, "вещь", "описание", true, userMapper.toUser(user), null);

        BookingDtoIn bookingDtoIn = new BookingDtoIn();
        bookingDtoIn.setId(1);
        bookingDtoIn.setStart(LocalDateTime.of(2024, 6, 20, 10, 10, 10));
        bookingDtoIn.setEnd(LocalDateTime.of(2024, 6, 21, 10, 10, 10));
        bookingDtoIn.setStatus(Status.WAITING);

        Booking booking = bookingMapper.toBooking(bookingDtoIn, user, item);
        BookingDtoOut bookingDtoOut = bookingMapper.toBookingDtoOut(booking);

        JsonContent<BookingDtoOut> result = json.write(bookingDtoOut);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(bookingDtoOut.getStart().toString());
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(bookingDtoOut.getEnd().toString());
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(bookingDtoOut.getStatus().toString());
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(bookingDtoOut.getItem().getId());
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(bookingDtoOut.getBooker().getId());
    }
}
