package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserMapperImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingServiceControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;
    @Autowired
    private MockMvc mvc;
    private UserDto user;
    private UserDto user2;
    private BookingMapper bookingMapper;
    Item item;
    Booking booking;
    BookingDtoIn bookingDtoIn;
    BookingDtoOut bookingDtoOut;

    @BeforeEach
    void beforeEach() {
        bookingMapper = new BookingMapperImpl();
        UserMapper userMapper = new UserMapperImpl();

        user = new UserDto();
        user.setId(1);
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");

        user2 = new UserDto();
        user2.setId(2);
        user2.setName("www");
        user2.setEmail("www@yandex.ru");

        item = new Item();
        item.setId(1);
        item.setName("стол");
        item.setDescription("описание");
        item.setAvailable(true);
        item.setOwner(userMapper.toUser(user));

        bookingDtoIn = new BookingDtoIn(1,
                LocalDateTime.of(2024, 6, 20, 10, 10, 10),
                LocalDateTime.of(2024, 6, 21, 10, 10, 10),
                1,
                null);
        booking = bookingMapper.toBooking(bookingDtoIn, user2, item);
        booking.setStatus(Status.WAITING);
        bookingDtoOut = bookingMapper.toBookingDtoOut(booking);

    }

    @Test
    void testCreateBooking() throws Exception {
        when(bookingService.createBooking(any(), anyInt())).thenReturn(bookingDtoOut);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoOut.getId())))
                .andExpect(jsonPath("$.start", is(bookingDtoOut.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoOut.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingDtoOut.getStatus().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingDtoOut.getItem().getId())))
                .andExpect(jsonPath("$.booker.id", is(bookingDtoOut.getBooker().getId())));

    }

    @Test
    void testChangeStatus() throws Exception {
        bookingDtoOut.setStatus(Status.APPROVED);
        when(bookingService.changeStatus(anyInt(), anyInt(), anyBoolean())).thenReturn(bookingDtoOut);

        mvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoOut.getId())))
                .andExpect(jsonPath("$.start", is(bookingDtoOut.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoOut.getEnd().toString())))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.item.id", is(bookingDtoOut.getItem().getId())))
                .andExpect(jsonPath("$.booker.id", is(bookingDtoOut.getBooker().getId())));
    }

    @Test
    void testGetBookingById() throws Exception {
        when(bookingService.getBookingById(anyInt(), anyInt())).thenReturn(bookingDtoOut);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoOut.getId())))
                .andExpect(jsonPath("$.start", is(bookingDtoOut.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoOut.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingDtoOut.getStatus().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingDtoOut.getItem().getId())))
                .andExpect(jsonPath("$.booker.id", is(bookingDtoOut.getBooker().getId())));


    }

    @Test
    void testGetAllForBooker() throws Exception {
        when(bookingService.getAllForBooker(anyInt(), anyString(), any(), any())).thenReturn(List.of(bookingDtoOut));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDtoOut.getId())))
                .andExpect(jsonPath("$[0].start", is(bookingDtoOut.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingDtoOut.getEnd().toString())))
                .andExpect(jsonPath("$[0].status", is(bookingDtoOut.getStatus().toString())))
                .andExpect(jsonPath("$[0].item.id", is(bookingDtoOut.getItem().getId())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDtoOut.getBooker().getId())));
    }

    @Test
    void testGetAllForOwner() throws Exception {
        when(bookingService.getAllForOwner(anyInt(), anyString(), any(), any())).thenReturn(List.of(bookingDtoOut));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDtoOut.getId())))
                .andExpect(jsonPath("$[0].start", is(bookingDtoOut.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingDtoOut.getEnd().toString())))
                .andExpect(jsonPath("$[0].status", is(bookingDtoOut.getStatus().toString())))
                .andExpect(jsonPath("$[0].item.id", is(bookingDtoOut.getItem().getId())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDtoOut.getBooker().getId())));
    }
}
