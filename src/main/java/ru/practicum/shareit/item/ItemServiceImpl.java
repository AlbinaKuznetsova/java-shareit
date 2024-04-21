package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto createItem(ItemDto itemDto, int userId) {
        return itemMapper.toItemDto(
                itemRepository.createItem(itemMapper.toItem(itemDto, userService.getUserById(userId)))
        );
    }

    @Override
    public ItemDto updateItem(int itemId, ItemDto itemDto, int userId) {
        return itemMapper.toItemDto(itemRepository.updateItem(itemId, itemDto, userId));
    }

    @Override
    public ItemDto getItem(int itemId) {
        return itemMapper.toItemDto(itemRepository.getItem(itemId));
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
