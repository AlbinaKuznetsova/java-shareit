package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDates;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "requestId", source = "item.request.id")
    ItemDto toItemDto(Item item);

    @Mapping(target = "id", source = "itemDto.id")
    @Mapping(target = "name", source = "itemDto.name")
    @Mapping(target = "request.id", source = "itemDto.requestId")
    Item toItem(ItemDto itemDto, UserDto owner);

    @Mapping(target = "requestId", source = "item.request.id")
    List<ItemDto> toItemDto(List<Item> items);

    ItemDtoWithDates toItemDtoWithDates(Item item);
}
