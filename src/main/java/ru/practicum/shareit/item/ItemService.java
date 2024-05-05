package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, int userId);

    ItemDto updateItem(int itemId, ItemDto itemDto, int userId);

    ItemDto getItem(int itemId);

    Collection<ItemDto> getAllItems(int userId);

    Collection<ItemDto> searchItems(String text);
}
