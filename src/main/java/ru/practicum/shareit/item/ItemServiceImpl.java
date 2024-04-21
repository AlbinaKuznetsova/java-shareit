package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    public final ItemRepository itemRepository;
    public final UserRepository userRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, int userId) {
        return ItemMapper.toItemDto(
                itemRepository.createItem(ItemMapper.toItem(itemDto, userRepository.getUserById(userId)))
        );
    }

    @Override
    public ItemDto updateItem(int itemId, ItemDto itemDto, int userId) {
        return ItemMapper.toItemDto(itemRepository.updateItem(itemId, itemDto, userId));
    }

    @Override
    public ItemDto getItem(int itemId) {
        return ItemMapper.toItemDto(itemRepository.getItem(itemId));
    }

    @Override
    public Collection<ItemDto> getAllItems(int userId) {
        return itemRepository.getAllItems(userId);
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        return itemRepository.searchItems(text);
    }
}
