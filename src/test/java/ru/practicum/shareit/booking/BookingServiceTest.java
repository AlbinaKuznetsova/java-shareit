package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserMapperImpl;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    BookingRepository bookingRepository;
    BookingMapper bookingMapper;
    @Mock
    ItemService itemService;
    @Mock
    UserService userService;
    BookingService bookingService;

    UserDto user;
    UserDto user2;
    Item item;
    BookingDtoIn bookingDtoIn;
    Booking booking;

    @BeforeEach
    void beforeEach() {
        bookingMapper = new BookingMapperImpl();
        bookingService = new BookingServiceImpl(bookingRepository, bookingMapper, userService, itemService);

        user = new UserDto();
        user.setId(1);
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        user2 = new UserDto();
        user2.setId(2);
        user2.setName("www");
        user2.setEmail("www@yandex.ru");
        UserMapper userMapper = new UserMapperImpl();
        item = new Item(1, "вещь", "описание", true, userMapper.toUser(user), null);

        bookingDtoIn = new BookingDtoIn(1,
                LocalDateTime.of(2024, 6, 20, 0, 0),
                LocalDateTime.of(2024, 6, 21, 0, 0),
                1,
                null);
        booking = bookingMapper.toBooking(bookingDtoIn, user2, item);
        booking.setStatus(Status.WAITING);
    }

    @Test
    void testCreateBooking() {

        Mockito.when(userService.getUserById(anyInt())).thenReturn(user2);
        Mockito.when(itemService.getItemForBooking(anyInt())).thenReturn(item);
        Mockito.when(bookingRepository.save(any())).thenReturn(booking);

        BookingDtoOut bookingDtoOut = bookingService.createBooking(bookingDtoIn, user2.getId());

        Mockito.verify(userService, Mockito.times(1)).getUserById(user2.getId());
        Mockito.verify(itemService, Mockito.times(1)).getItemForBooking(item.getId());
        Assertions.assertEquals(bookingMapper.toBookingDtoOut(booking), bookingDtoOut);

        // Владелец вещи пытается забронировать свою вещь
        Assertions.assertThrows(ObjectNotFoundException.class, () -> bookingService.createBooking(bookingDtoIn, user.getId()));

        // Пробуем забронировать недоступную вещь
        item.setAvailable(false);
        Assertions.assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDtoIn, user2.getId()));
    }

    @Test
    void testChangeStatus() {
        Mockito.when(bookingRepository.save(any())).thenReturn(booking);
        Mockito.when(bookingRepository.findById(anyInt())).thenAnswer(invocationOnMock -> {
            int bookingId = invocationOnMock.getArgument(0, Integer.class);
            if (bookingId == booking.getId()) {
                return Optional.of(booking);
            } else {
                return Optional.empty();
            }
        });

        BookingDtoOut bookingDtoOut = bookingService.changeStatus(user.getId(), booking.getId(), false);
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).save(booking);

        Assertions.assertEquals(Status.REJECTED, bookingDtoOut.getStatus());

        // Пробуем изменить статус бронирования не владельцем вещи
        booking.setStatus(Status.WAITING);
        Assertions.assertThrows(ObjectNotFoundException.class, () -> bookingService.changeStatus(user2.getId(), booking.getId(), false));

        // Пробуем изменить некорректное бронирование
        booking.setStatus(Status.WAITING);
        Assertions.assertThrows(ObjectNotFoundException.class, () -> bookingService.changeStatus(user.getId(), booking.getId() + 1000, false));

        // Пробуем изменить бронирование с неподходящим статусом
        booking.setStatus(Status.APPROVED);
        Assertions.assertThrows(ValidationException.class, () -> bookingService.changeStatus(user.getId(), booking.getId(), false));
    }

    @Test
    void testGetBookingById() {
        Mockito.when(bookingRepository.findById(anyInt())).thenAnswer(invocationOnMock -> {
            int bookingId = invocationOnMock.getArgument(0, Integer.class);
            if (bookingId == booking.getId()) {
                return Optional.of(booking);
            } else {
                return Optional.empty();
            }
        });

        BookingDtoOut bookingDtoOut = bookingService.getBookingById(user.getId(), booking.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(booking.getId());
        Assertions.assertEquals(bookingMapper.toBookingDtoOut(booking), bookingDtoOut);

        bookingDtoOut = bookingService.getBookingById(user2.getId(), booking.getId());

        // Пробуем просмотреть бронирование некорректным пользователем
        Assertions.assertThrows(ObjectNotFoundException.class, () -> bookingService.getBookingById(user2.getId() + 1000, booking.getId()));

        // Пробуем просмотреть несуществующее бронирование
        Assertions.assertThrows(ObjectNotFoundException.class, () -> bookingService.getBookingById(user2.getId(), booking.getId() + 1000));
    }

    @Test
    void testGetAllForBooker() {
        Mockito.when(bookingRepository.findAllByBookerIdOrderByEndDesc(anyInt())).thenReturn(List.of(booking));
        Mockito.when(userService.getUserById(anyInt())).thenReturn(user2);
        Collection<BookingDtoOut> bookings = bookingService.getAllForBooker(user2.getId(), "ALL", null, null);
        Assertions.assertTrue(bookings.contains(bookingMapper.toBookingDtoOut(booking)));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.getAllForBooker(user2.getId(), "TEST", null, null));

        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByBookerIdOrderByEndDesc(user2.getId());

        Mockito.when(bookingRepository.findAllByBookerIdAndStatus(anyInt(), any(), any())).thenReturn(Page.empty());
        bookings = bookingService.getAllForBooker(user2.getId(), "REJECTED", 0, 10);
        Assertions.assertTrue(bookings.isEmpty());

        Assertions.assertThrows(ValidationException.class, () -> bookingService.getAllForBooker(user2.getId(), "REJECTED", -5, 10));
        Assertions.assertThrows(ValidationException.class, () -> bookingService.getAllForBooker(user2.getId(), "REJECTED", 5, -5));

    }

    @Test
    void testGetAllForOwner() {
        Mockito.when(bookingRepository.findAllByItemOwnerIdOrderByEndDesc(anyInt())).thenReturn(List.of(booking));
        Mockito.when(userService.getUserById(anyInt())).thenReturn(user2);
        Collection<BookingDtoOut> bookings = bookingService.getAllForOwner(user2.getId(), "ALL", null, null);
        Assertions.assertTrue(bookings.contains(bookingMapper.toBookingDtoOut(booking)));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.getAllForOwner(user2.getId(), "TEST", null, null));

        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemOwnerIdOrderByEndDesc(user2.getId());

        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStatus(anyInt(), any(), any())).thenReturn(Page.empty());
        bookings = bookingService.getAllForOwner(user2.getId(), "REJECTED", 0, 10);
        Assertions.assertTrue(bookings.isEmpty());

        Assertions.assertThrows(ValidationException.class, () -> bookingService.getAllForOwner(user2.getId(), "REJECTED", -5, 10));
        Assertions.assertThrows(ValidationException.class, () -> bookingService.getAllForOwner(user2.getId(), "REJECTED", 5, -5));

    }
}
