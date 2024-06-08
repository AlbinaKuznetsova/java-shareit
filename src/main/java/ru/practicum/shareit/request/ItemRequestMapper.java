package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {
    ItemRequestDtoOut toItemRequestDtoOut(ItemRequest itemRequest);

    List<ItemRequestDtoOut> toItemRequestDtoOut(List<ItemRequest> itemRequests);

    @Mapping(target = "id", source = "itemRequestDtoIn.id")
    ItemRequest toItemRequest(ItemRequestDtoIn itemRequestDtoIn, UserDto requestor);

    ItemRequest toItemRequest(ItemRequestDtoOut itemRequestDtoOut);
}
