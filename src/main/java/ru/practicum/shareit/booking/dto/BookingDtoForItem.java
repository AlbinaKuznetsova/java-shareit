package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;

@Component
public interface BookingDtoForItem {
    Integer getId();

    Integer getBookerId();
}
