package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findAllByBookerIdOrderByEndDesc(int userId);

    List<Booking> findAllByBookerIdAndStatusOrderByEndDesc(int userId, Status status);

    List<Booking> findAllByBookerIdAndEndIsBeforeOrderByEndDesc(int userId, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndStartIsAfterOrderByEndDesc(int userId, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByEndDesc(int userId, LocalDateTime dateTime1, LocalDateTime dateTime2);

    Page<Booking> findAllByBookerId(int userId, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStatus(int userId, Status status, Pageable pageable);

    Page<Booking> findAllByBookerIdAndEndIsBefore(int userId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartIsAfter(int userId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> findAllByBookerIdAndEndIsAfterAndStartIsBefore(int userId, LocalDateTime dateTime1, LocalDateTime dateTime2, Pageable pageable);

    List<Booking> findAllByItemOwnerIdOrderByEndDesc(int userId);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByEndDesc(int userId, Status status);

    List<Booking> findAllByItemOwnerIdAndEndIsBeforeOrderByEndDesc(int userId, LocalDateTime dateTime);

    List<Booking> findAllByItemOwnerIdAndStartIsAfterOrderByEndDesc(int userId, LocalDateTime dateTime);

    List<Booking> findAllByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByEndDesc(int userId, LocalDateTime dateTime1, LocalDateTime dateTime2);

    Page<Booking> findAllByItemOwnerId(int userId, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStatus(int userId, Status status, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndEndIsBefore(int userId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStartIsAfter(int userId, LocalDateTime dateTime, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndEndIsAfterAndStartIsBefore(int userId, LocalDateTime dateTime1, LocalDateTime dateTime2, Pageable pageable);

    BookingDtoForItem findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(int itemId, LocalDateTime date, Status status);

    BookingDtoForItem findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(int itemId, LocalDateTime date, Status status);

    Booking findFirst1ByBookerIdAndItemIdOrderByEndAsc(int userId, int itemId);
}
