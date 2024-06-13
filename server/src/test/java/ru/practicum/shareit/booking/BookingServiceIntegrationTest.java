package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserMapperImpl;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceIntegrationTest {
    private final EntityManager em;
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;
    private final ItemMapper itemMapper;
    UserDto user;
    UserDto user2;
    Item item;
    BookingDtoIn bookingDtoIn;
    ItemDto itemDto;
    UserMapper userMapper;

    @BeforeEach
    void beforeEach() {
        userMapper = new UserMapperImpl();
        user = new UserDto();
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        user2 = new UserDto();
        user2.setName("www");
        user2.setEmail("www@yandex.ru");
        item = new Item();
        item.setName("вещь");
        item.setDescription("описание");
        item.setAvailable(true);
        user = userService.createUser(user);
        user2 = userService.createUser(user2);
        itemDto = itemService.createItem(itemMapper.toItemDto(item), user.getId());
        item.setId(itemDto.getId());

        bookingDtoIn = new BookingDtoIn();
        bookingDtoIn.setStart(LocalDateTime.of(2025, 6, 20, 0, 0));
        bookingDtoIn.setEnd(LocalDateTime.of(2025, 6, 21, 0, 0));
        bookingDtoIn.setItemId(itemDto.getId());
    }

    @Test
    void testCreateBooking() {
        BookingDtoOut bookingDtoOut = bookingService.createBooking(bookingDtoIn, user2.getId());

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :id", Booking.class);
        Booking bookingFromDB = query.setParameter("id", bookingDtoOut.getId()).getSingleResult();

        assertThat(bookingFromDB.getId(), equalTo(bookingDtoOut.getId()));
        assertThat(bookingFromDB.getItem().getId(), equalTo(itemDto.getId()));
        assertThat(bookingFromDB.getBooker(), equalTo(userMapper.toUser(user2)));
        assertThat(bookingFromDB.getStart(), equalTo(LocalDateTime.of(2025, 6, 20, 0, 0)));
        assertThat(bookingFromDB.getEnd(), equalTo(LocalDateTime.of(2025, 6, 21, 0, 0)));
        assertThat(bookingFromDB.getStatus(), equalTo(Status.WAITING));

    }

    @Test
    void testChangeStatus() {
        BookingDtoOut bookingDtoOut = bookingService.createBooking(bookingDtoIn, user2.getId());
        bookingService.changeStatus(user.getId(), bookingDtoOut.getId(), true);


        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :id", Booking.class);
        Booking bookingFromDB = query.setParameter("id", bookingDtoOut.getId()).getSingleResult();

        assertThat(bookingFromDB.getId(), equalTo(bookingDtoOut.getId()));
        assertThat(bookingFromDB.getBooker(), equalTo(userMapper.toUser(user2)));
        assertThat(bookingFromDB.getStart(), equalTo(LocalDateTime.of(2025, 6, 20, 0, 0)));
        assertThat(bookingFromDB.getEnd(), equalTo(LocalDateTime.of(2025, 6, 21, 0, 0)));
        assertThat(bookingFromDB.getStatus(), equalTo(Status.APPROVED));

    }

    @Test
    void testGetBookingById() {
        BookingDtoOut bookingDtoOut = bookingService.createBooking(bookingDtoIn, user2.getId());
        BookingDtoOut bookingDtoOut2 = bookingService.getBookingById(user2.getId(), bookingDtoOut.getId());
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :id", Booking.class);
        Booking bookingFromDB = query.setParameter("id", bookingDtoOut.getId()).getSingleResult();

        assertThat(bookingFromDB.getId(), equalTo(bookingDtoOut2.getId()));
        assertThat(bookingFromDB.getBooker(), equalTo(userMapper.toUser(bookingDtoOut2.getBooker())));
        assertThat(bookingFromDB.getStart(), equalTo(bookingDtoOut2.getStart()));
        assertThat(bookingFromDB.getEnd(), equalTo(bookingDtoOut2.getEnd()));
        assertThat(bookingFromDB.getStatus(), equalTo(bookingDtoOut2.getStatus()));
    }

    @Test
    void testGetAllForBooker() {
        BookingDtoIn bookingDtoIn2 = new BookingDtoIn();
        bookingDtoIn2.setStart(LocalDateTime.of(2025, 6, 10, 0, 0));
        bookingDtoIn2.setEnd(LocalDateTime.of(2025, 6, 11, 0, 0));
        bookingDtoIn2.setItemId(itemDto.getId());

        BookingDtoOut bookingDtoOut1 = bookingService.createBooking(bookingDtoIn, user2.getId());
        bookingDtoOut1 = bookingService.changeStatus(user.getId(), bookingDtoOut1.getId(), false);
        BookingDtoOut bookingDtoOut2 = bookingService.createBooking(bookingDtoIn2, user2.getId());

        Collection<BookingDtoOut> bookings = bookingService.getAllForBooker(user2.getId(), "ALL", null, null);
        assertThat(bookings.size(), equalTo(2));
        Assertions.assertTrue(bookings.contains(bookingDtoOut1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut2));

        bookings = bookingService.getAllForBooker(user2.getId(), "PAST", null, null);
        assertThat(bookings.size(), equalTo(0));

        bookings = bookingService.getAllForBooker(user2.getId(), "CURRENT", null, null);
        assertThat(bookings.size(), equalTo(0));

        bookings = bookingService.getAllForBooker(user2.getId(), "FUTURE", null, null);
        assertThat(bookings.size(), equalTo(2));
        Assertions.assertTrue(bookings.contains(bookingDtoOut1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut2));

        bookings = bookingService.getAllForBooker(user2.getId(), "REJECTED", null, null);
        assertThat(bookings.size(), equalTo(1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut1));

        bookings = bookingService.getAllForBooker(user2.getId(), "WAITING", null, null);
        assertThat(bookings.size(), equalTo(1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut2));

        bookings = bookingService.getAllForBooker(user2.getId(), "ALL", 0, 1);
        assertThat(bookings.size(), equalTo(1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut1));

        bookings = bookingService.getAllForBooker(user2.getId(), "CURRENT", 0, 1);
        assertThat(bookings.size(), equalTo(0));

        bookings = bookingService.getAllForBooker(user2.getId(), "PAST", 0, 1);
        assertThat(bookings.size(), equalTo(0));

        bookings = bookingService.getAllForBooker(user2.getId(), "FUTURE", 0, 1);
        assertThat(bookings.size(), equalTo(1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut1));

        bookings = bookingService.getAllForBooker(user2.getId(), "WAITING", 0, 1);
        assertThat(bookings.size(), equalTo(1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut2));

        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.getAllForBooker(user2.getId(), "NO_STATE", 0, 1));

        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getAllForBooker(user.getId() + 1000, "ALL", 0, 1));
    }

    @Test
    void testGetAllForOwner() {
        BookingDtoIn bookingDtoIn2 = new BookingDtoIn();
        bookingDtoIn2.setStart(LocalDateTime.of(2025, 6, 10, 0, 0));
        bookingDtoIn2.setEnd(LocalDateTime.of(2025, 6, 11, 0, 0));
        bookingDtoIn2.setItemId(itemDto.getId());

        BookingDtoOut bookingDtoOut1 = bookingService.createBooking(bookingDtoIn, user2.getId());
        bookingDtoOut1 = bookingService.changeStatus(user.getId(), bookingDtoOut1.getId(), false);
        BookingDtoOut bookingDtoOut2 = bookingService.createBooking(bookingDtoIn2, user2.getId());

        Collection<BookingDtoOut> bookings = bookingService.getAllForOwner(user.getId(), "ALL", null, null);
        assertThat(bookings.size(), equalTo(2));
        Assertions.assertTrue(bookings.contains(bookingDtoOut1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut2));

        bookings = bookingService.getAllForOwner(user.getId(), "PAST", null, null);
        assertThat(bookings.size(), equalTo(0));

        bookings = bookingService.getAllForOwner(user.getId(), "CURRENT", null, null);
        assertThat(bookings.size(), equalTo(0));

        bookings = bookingService.getAllForOwner(user.getId(), "FUTURE", null, null);
        assertThat(bookings.size(), equalTo(2));
        Assertions.assertTrue(bookings.contains(bookingDtoOut1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut2));

        bookings = bookingService.getAllForOwner(user.getId(), "REJECTED", null, null);
        assertThat(bookings.size(), equalTo(1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut1));

        bookings = bookingService.getAllForOwner(user.getId(), "WAITING", null, null);
        assertThat(bookings.size(), equalTo(1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut2));

        bookings = bookingService.getAllForOwner(user.getId(), "ALL", 0, 1);
        assertThat(bookings.size(), equalTo(1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut1));

        bookings = bookingService.getAllForOwner(user.getId(), "CURRENT", 0, 1);
        assertThat(bookings.size(), equalTo(0));

        bookings = bookingService.getAllForOwner(user.getId(), "PAST", 0, 1);
        assertThat(bookings.size(), equalTo(0));

        bookings = bookingService.getAllForOwner(user.getId(), "FUTURE", 0, 1);
        assertThat(bookings.size(), equalTo(1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut1));

        bookings = bookingService.getAllForOwner(user.getId(), "WAITING", 0, 1);
        assertThat(bookings.size(), equalTo(1));
        Assertions.assertTrue(bookings.contains(bookingDtoOut2));

        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.getAllForOwner(user.getId(), "NO_STATE", 0, 1));

        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getAllForOwner(user.getId() + 1000, "ALL", 0, 1));

    }

    @Test
    void testGetNextBooking() {
        BookingDtoIn bookingDtoIn2 = new BookingDtoIn();
        bookingDtoIn2.setStart(LocalDateTime.of(2025, 6, 10, 0, 0));
        bookingDtoIn2.setEnd(LocalDateTime.of(2025, 6, 11, 0, 0));
        bookingDtoIn2.setItemId(itemDto.getId());

        BookingDtoOut bookingDtoOut1 = bookingService.createBooking(bookingDtoIn, user2.getId());
        bookingDtoOut1 = bookingService.changeStatus(user.getId(), bookingDtoOut1.getId(), false);
        BookingDtoOut bookingDtoOut2 = bookingService.createBooking(bookingDtoIn2, user2.getId());

        BookingDtoForItem bookingDtoForItem = bookingService.getNextBooking(item.getId(), LocalDateTime.now(), Status.REJECTED);
        assertThat(bookingDtoForItem.getId(), equalTo(bookingDtoOut1.getId()));
        assertThat(bookingDtoForItem.getBookerId(), equalTo(bookingDtoOut1.getBooker().getId()));

        bookingDtoForItem = bookingService.getNextBooking(item.getId(), LocalDateTime.now(), Status.WAITING);
        assertThat(bookingDtoForItem.getId(), equalTo(bookingDtoOut2.getId()));
        assertThat(bookingDtoForItem.getBookerId(), equalTo(bookingDtoOut2.getBooker().getId()));
    }

    @Test
    void testGetLastBooking() {
        BookingDtoIn bookingDtoIn2 = new BookingDtoIn();
        bookingDtoIn2.setStart(LocalDateTime.of(2025, 6, 10, 0, 0));
        bookingDtoIn2.setEnd(LocalDateTime.of(2025, 6, 11, 0, 0));
        bookingDtoIn2.setItemId(itemDto.getId());

        BookingDtoOut bookingDtoOut1 = bookingService.createBooking(bookingDtoIn, user2.getId());
        bookingDtoOut1 = bookingService.changeStatus(user.getId(), bookingDtoOut1.getId(), false);
        BookingDtoOut bookingDtoOut2 = bookingService.createBooking(bookingDtoIn2, user2.getId());

        BookingDtoForItem bookingDtoForItem = bookingService.getLastBooking(item.getId(),
                LocalDateTime.of(2025, 6, 29, 0, 0),
                Status.REJECTED);
        assertThat(bookingDtoForItem.getId(), equalTo(bookingDtoOut1.getId()));
        assertThat(bookingDtoForItem.getBookerId(), equalTo(bookingDtoOut1.getBooker().getId()));

        bookingDtoForItem = bookingService.getLastBooking(item.getId(),
                LocalDateTime.of(2025, 6, 29, 0, 0),
                Status.WAITING);
        assertThat(bookingDtoForItem.getId(), equalTo(bookingDtoOut2.getId()));
        assertThat(bookingDtoForItem.getBookerId(), equalTo(bookingDtoOut2.getBooker().getId()));
    }

    @Test
    void testGetBookingForComment() {
        BookingDtoOut bookingDtoOut = bookingService.createBooking(bookingDtoIn, user2.getId());
        Booking booking = bookingService.getBookingForComment(user2.getId(), item.getId());

        assertThat(booking.getId(), equalTo(bookingDtoOut.getId()));
        assertThat(booking.getStart(), equalTo(bookingDtoOut.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingDtoOut.getEnd()));
        assertThat(booking.getItem().getId(), equalTo(bookingDtoOut.getItem().getId()));
        assertThat(booking.getBooker(), equalTo(userMapper.toUser(bookingDtoOut.getBooker())));

    }

}
