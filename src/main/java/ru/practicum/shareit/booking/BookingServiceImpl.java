package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final ItemService itemService;

    public BookingServiceImpl(BookingRepository bookingRepository, BookingMapper bookingMapper, UserService userService, @Lazy ItemService itemService) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.userService = userService;
        this.itemService = itemService;
    }


    @Override
    public BookingDtoOut createBooking(BookingDtoIn bookingDtoIn, int userId) {
        validateBooking(bookingDtoIn);
        Item item = itemService.getItemForBooking(bookingDtoIn.getItemId()); // Проверяем, что вещь существует
        Booking booking = bookingMapper.toBooking(bookingDtoIn, userService.getUserById(userId), item);
        if (item.getAvailable() == false) {
            log.info("Вещь недоступна");
            throw new ValidationException("Вещь недоступна");
        }
        if (userId == item.getOwner().getId()) {
            log.info("Владелец не может забронировать свою вещь");
            throw new ObjectNotFoundException("Владелец не может забронировать свою вещь");
        }
        booking.setStatus(Status.WAITING);
        return bookingMapper.toBookingDtoOut(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoOut changeStatus(int userId, Integer bookingId, boolean approved) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        Booking booking = null;
        if (bookingOpt.isPresent()) {
            booking = bookingOpt.get();
        } else {
            log.info("Бронирование с id {} не найдено.", bookingId);
            throw new ObjectNotFoundException("Бронирование не найдено");
        }
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.info("Попытка изменить статус бронирования " + bookingId + " не владельцем вещи " + userId);
            throw new ObjectNotFoundException("Бронирование может подтвердить или отменить только владелец вещи");
        }
        if (!booking.getStatus().equals(Status.WAITING)) {
            log.info("Некорректный статус");
            throw new ValidationException("Некорректный статус");
        } else {
            if (approved == true) {
                booking.setStatus(Status.APPROVED);
            } else {
                booking.setStatus(Status.REJECTED);
            }
            return bookingMapper.toBookingDtoOut(bookingRepository.save(booking));
        }
    }

    @Override
    public BookingDtoOut getBookingById(int userId, Integer bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        Booking booking = null;
        if (bookingOpt.isPresent()) {
            booking = bookingOpt.get();
        } else {
            log.info("Бронирование с id {} не найдено.", bookingId);
            throw new ObjectNotFoundException("Бронирование не найдено");
        }
        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return bookingMapper.toBookingDtoOut(booking);
        } else {
            throw new ObjectNotFoundException("Бронирование может просмотреть только создатель или владелец вещи");
        }
    }

    @Override
    public Collection<BookingDtoOut> getAllForBooker(int userId, String state) {
        if (userService.getUserById(userId) == null) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        LocalDateTime now = LocalDateTime.now();
        if (state.equals(State.ALL.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByBookerIdOrderByEndDesc(userId));
        } else if (state.equals(State.CURRENT.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByEndDesc(
                    userId, now, now));
        } else if (state.equals(State.PAST.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByEndDesc(userId, now));
        } else if (state.equals(State.FUTURE.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByBookerIdAndStartIsAfterOrderByEndDesc(userId, now));
        } else if (state.equals(State.REJECTED.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByBookerIdAndStatusOrderByEndDesc(userId, Status.REJECTED));
        } else if (state.equals(State.WAITING.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByBookerIdAndStatusOrderByEndDesc(userId, Status.WAITING));
        } else {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public Collection<BookingDtoOut> getAllForOwner(int userId, String state) {
        LocalDateTime now = LocalDateTime.now();
        if (userService.getUserById(userId) == null) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        if (state.equals(State.ALL.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByItemOwnerIdOrderByEndDesc(userId));
        } else if (state.equals(State.CURRENT.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByEndDesc(
                    userId, now, now));
        } else if (state.equals(State.PAST.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByItemOwnerIdAndEndIsBeforeOrderByEndDesc(userId, now));
        } else if (state.equals(State.FUTURE.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByItemOwnerIdAndStartIsAfterOrderByEndDesc(userId, now));
        } else if (state.equals(State.REJECTED.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByItemOwnerIdAndStatusOrderByEndDesc(userId, Status.REJECTED));
        } else if (state.equals(State.WAITING.toString())) {
            return bookingMapper.toBookingDtoOut(bookingRepository.findAllByItemOwnerIdAndStatusOrderByEndDesc(userId, Status.WAITING));
        } else {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public BookingDtoForItem getNextBooking(Integer itemId, LocalDateTime now, Status status) {
        return bookingRepository.findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(itemId, now, status);
    }

    @Override
    public BookingDtoForItem getLastBooking(Integer itemId, LocalDateTime now, Status status) {
        return bookingRepository.findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(itemId, now, status);
    }

    @Override
    public Booking getBookingForComment(Integer userId, Integer itemId) {
        return bookingRepository.findFirst1ByBookerIdAndItemIdOrderByEndAsc(userId, itemId);
    }

    private void validateBooking(BookingDtoIn bookingDtoIn) {
        if (bookingDtoIn.getItemId() == null) {
            log.info("Вещь не может быть пустой");
            throw new ValidationException("Вещь не может быть пустой");
        }
        if (bookingDtoIn.getStart() == null) {
            log.info("Дата начала не может быть пустой");
            throw new ValidationException("Дата начала не может быть пустой");
        }
        if (bookingDtoIn.getEnd() == null) {
            log.info("Дата окончания не может быть пустой");
            throw new ValidationException("Дата окончания не может быть пустой");
        }
        if (bookingDtoIn.getEnd().isBefore(bookingDtoIn.getStart()) ||
                bookingDtoIn.getStart().isBefore(LocalDateTime.now()) ||
                bookingDtoIn.getEnd().isBefore(LocalDateTime.now()) ||
                bookingDtoIn.getStart().isEqual(bookingDtoIn.getEnd())
        ) {
            log.info("Даты бронирования некорректные: " + bookingDtoIn.getStart() + ", " + bookingDtoIn.getEnd());
            throw new ValidationException("Даты бронирования некорректные");
        }
    }
}
