package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.exceptions.ValidationException;

import java.time.LocalDateTime;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        State state = State.from(stateParam)
                .orElseThrow(() -> new ValidationException("Unknown state: " + stateParam));
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader("X-Sharer-User-Id") int userId,
                                           @RequestBody @Valid BookingDtoIn bookingDtoIn) {
        log.info("Creating booking {}, userId={}", bookingDtoIn, userId);
        validateBooking(bookingDtoIn);
        return bookingClient.bookItem(userId, bookingDtoIn);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") int userId,
                                             @PathVariable Integer bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> changeStatus(@RequestHeader("X-Sharer-User-Id") int userId,
                                               @PathVariable Integer bookingId,
                                               @RequestParam boolean approved) {
        log.info("Change status booking {}, userId={}", bookingId, userId);
        return bookingClient.changeStatus(userId, bookingId, approved);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllForOwner(@RequestHeader("X-Sharer-User-Id") int userId,
                                                 @RequestParam(name = "state", defaultValue = "all") String state,
                                                 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                 @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get booking by owner userId={}", userId);
        State state1 = State.from(state)
                .orElseThrow(() -> new ValidationException("Unknown state: " + state));
        return bookingClient.getAllForOwner(userId, state1, from, size);
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