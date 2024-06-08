package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDtoOut createItemRequest(ItemRequestDtoIn itemRequest, int userId);

    List<ItemRequestDtoOut> getRequestsByRequestor(int userId);

    List<ItemRequestDtoOut> getAllRequests(int userId, Integer from, Integer size);

    ItemRequestDtoOut getRequestById(int userId, int requestId);

}
