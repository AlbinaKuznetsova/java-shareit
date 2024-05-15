package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.Collection;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class BookingController {
    public final BookingService bookingService;

    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking(@RequestBody BookingDto bookingDto,
                                                 @RequestHeader("X-Sharer-User-Id") int userId) {
        return ResponseEntity.ok().body(bookingService.createBooking(bookingDto, userId));
    }

    @PatchMapping("/bookings/{bookingId}")
    public ResponseEntity<Booking> changeStatus(@RequestHeader("X-Sharer-User-Id") int userId,
                                                @PathVariable Integer bookingId,
                                                @RequestParam boolean approved) {
        return ResponseEntity.ok().body(bookingService.changeStatus(userId, bookingId, approved));
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<Booking> getBookingById(@RequestHeader("X-Sharer-User-Id") int userId,
                                                  @PathVariable Integer bookingId) {
        return ResponseEntity.ok().body(bookingService.getBookingById(userId, bookingId));
    }

    @GetMapping("/bookings")
    public ResponseEntity<Collection<Booking>> getAllForBooker(@RequestHeader("X-Sharer-User-Id") int userId,
                                                               @RequestParam(defaultValue = "ALL", required = false) String state) {
        return ResponseEntity.ok().body(bookingService.getAllForBooker(userId, state));
    }

    @GetMapping("/bookings/owner")
    public ResponseEntity<Collection<Booking>> getAllForOwner(@RequestHeader("X-Sharer-User-Id") int userId,
                                                              @RequestParam(defaultValue = "ALL", required = false) String state) {
        return ResponseEntity.ok().body(bookingService.getAllForOwner(userId, state));
    }
}
