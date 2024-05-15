package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
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
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public Booking createBooking(BookingDto bookingDto, int userId) {
        validateBooking(bookingDto);
        Item item = itemService.getItemForBooking(bookingDto.getItemId()); // Проверяем, что вещь существует
        Booking booking = bookingMapper.toBooking(bookingDto, userService.getUserById(userId), item);
        if (item.getAvailable() == false) {
            log.info("Вещь недоступна");
            throw new ValidationException("Вещь недоступна");
        }
        if (userId == item.getOwner().getId()) {
            log.info("Владелец не может забронировать свою вещь");
            throw new ObjectNotFoundException("Владелец не может забронировать свою вещь");
        }
        booking.setStatus(Status.WAITING);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking changeStatus(int userId, Integer bookingId, boolean approved) {
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
            return bookingRepository.save(booking);
        }
    }

    @Override
    public Booking getBookingById(int userId, Integer bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        Booking booking = null;
        if (bookingOpt.isPresent()) {
            booking = bookingOpt.get();
        } else {
            log.info("Бронирование с id {} не найдено.", bookingId);
            throw new ObjectNotFoundException("Бронирование не найдено");
        }
        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return booking;
        } else {
            throw new ObjectNotFoundException("Бронирование может просмотреть только создатель или владелец вещи");
        }
    }

    @Override
    public Collection<Booking> getAllForBooker(int userId, String state) {
        if (userService.getUserById(userId) == null) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        LocalDateTime now = LocalDateTime.now();
        if (state.equals(State.ALL.toString())) {
            return bookingRepository.findAllByBookerIdOrderByEndDesc(userId);
        } else if (state.equals(State.CURRENT.toString())) {
            return bookingRepository.findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByEndDesc(
                    userId, now, now);
        } else if (state.equals(State.PAST.toString())) {
            return bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByEndDesc(userId, now);
        } else if (state.equals(State.FUTURE.toString())) {
            return bookingRepository.findAllByBookerIdAndStartIsAfterOrderByEndDesc(userId, now);
        } else if (state.equals(State.REJECTED.toString())) {
            return bookingRepository.findAllByBookerIdAndStatusOrderByEndDesc(userId, Status.REJECTED);
        } else if (state.equals(State.WAITING.toString())) {
            return bookingRepository.findAllByBookerIdAndStatusOrderByEndDesc(userId, Status.WAITING);
        } else {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public Collection<Booking> getAllForOwner(int userId, String state) {
        LocalDateTime now = LocalDateTime.now();
        if (userService.getUserById(userId) == null) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        if (state.equals(State.ALL.toString())) {
            return bookingRepository.findAllByItemOwnerIdOrderByEndDesc(userId);
        } else if (state.equals(State.CURRENT.toString())) {
            return bookingRepository.findAllByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByEndDesc(
                    userId, now, now);
        } else if (state.equals(State.PAST.toString())) {
            return bookingRepository.findAllByItemOwnerIdAndEndIsBeforeOrderByEndDesc(userId, now);
        } else if (state.equals(State.FUTURE.toString())) {
            return bookingRepository.findAllByItemOwnerIdAndStartIsAfterOrderByEndDesc(userId, now);
        } else if (state.equals(State.REJECTED.toString())) {
            return bookingRepository.findAllByItemOwnerIdAndStatusOrderByEndDesc(userId, Status.REJECTED);
        } else if (state.equals(State.WAITING.toString())) {
            return bookingRepository.findAllByItemOwnerIdAndStatusOrderByEndDesc(userId, Status.WAITING);
        } else {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private void validateBooking(BookingDto bookingDto) {
        if (bookingDto.getItemId() == null) {
            log.info("Вещь не может быть пустой");
            throw new ValidationException("Вещь не может быть пустой");
        }
        if (bookingDto.getStart() == null) {
            log.info("Дата начала не может быть пустой");
            throw new ValidationException("Дата начала не может быть пустой");
        }
        if (bookingDto.getEnd() == null) {
            log.info("Дата окончания не может быть пустой");
            throw new ValidationException("Дата окончания не может быть пустой");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getStart().isBefore(LocalDateTime.now()) ||
                bookingDto.getEnd().isBefore(LocalDateTime.now()) ||
                bookingDto.getStart().isEqual(bookingDto.getEnd())
        ) {
            log.info("Даты бронирования некорректные: " + bookingDto.getStart() + ", " + bookingDto.getEnd());
            throw new ValidationException("Даты бронирования некорректные");
        }
    }
}
