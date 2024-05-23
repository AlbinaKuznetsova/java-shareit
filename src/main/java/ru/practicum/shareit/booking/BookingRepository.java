package ru.practicum.shareit.booking;

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

    List<Booking> findAllByItemOwnerIdOrderByEndDesc(int userId);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByEndDesc(int userId, Status status);

    List<Booking> findAllByItemOwnerIdAndEndIsBeforeOrderByEndDesc(int userId, LocalDateTime dateTime);

    List<Booking> findAllByItemOwnerIdAndStartIsAfterOrderByEndDesc(int userId, LocalDateTime dateTime);

    List<Booking> findAllByItemOwnerIdAndEndIsAfterAndStartIsBeforeOrderByEndDesc(int userId, LocalDateTime dateTime1, LocalDateTime dateTime2);


    BookingDtoForItem findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(int itemId, LocalDateTime date, Status status);

    BookingDtoForItem findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(int itemId, LocalDateTime date, Status status);

    Booking findFirst1ByBookerIdAndItemIdOrderByEndAsc(int userId, int itemId);
}
