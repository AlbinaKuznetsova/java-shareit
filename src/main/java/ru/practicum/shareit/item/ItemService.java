package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDates;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, int userId);

    ItemDto updateItem(int itemId, ItemDto itemDto, int userId);

    ItemDtoWithDates getItem(int itemId, int userId);

    Item getItemForBooking(int itemId);

    Collection<ItemDtoWithDates> getAllItems(int userId);

    Collection<ItemDto> searchItems(String text);

    CommentDto createComment(Comment comment, int itemId, int userId);
}
