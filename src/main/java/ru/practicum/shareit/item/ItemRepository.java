package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Slf4j
@Repository
public class ItemRepository {
    private final Map<Integer, Item> items = new HashMap<>();
    @Autowired
    private ItemMapper itemMapper;
    private static Integer count = 0;

    public Item createItem(Item item) {
        validateItem(item);
        if (item.getId() == null) {
            item.setId(generateId());
        }
        items.put(item.getId(), item);
        log.info("Добавлена вещь {}", item);
        return item;
    }

    public Item updateItem(int itemId, ItemDto itemDto, int userId) {
        Item itemFromMap = null;
        if (items.containsKey(itemId)) {
            itemFromMap = items.get(itemId);
            if (userId == itemFromMap.getOwner().getId()) {
                if (itemDto.getName() != null) {
                    itemFromMap.setName(itemDto.getName());
                }
                if (itemDto.getDescription() != null) {
                    itemFromMap.setDescription(itemDto.getDescription());
                }
                if (itemDto.getAvailable() != null) {
                    itemFromMap.setAvailable(itemDto.getAvailable());
                }
                items.put(itemId, itemFromMap);
            } else {
                log.info("Вещь может изменить только владелец, itemId = {}", itemId);
                throw new ObjectNotFoundException("Вещь может изменить только владелец");
            }
        } else {
            log.info("Вещь с id {} не найдена.", itemId);
            throw new ObjectNotFoundException("Вещь не найдена");
        }
        log.info("Вещь с id {} обновлена.", itemId);
        return itemFromMap;
    }

    public Item getItem(int itemId) {
        return items.get(itemId);
    }

    public Collection<ItemDto> getAllItems(int userId) {
        List<ItemDto> itemsOfUser = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwner().getId() == userId) {
                itemsOfUser.add(itemMapper.toItemDto(item));
            }
        }
        return itemsOfUser;
    }

    public Collection<ItemDto> searchItems(String text) {
        List<ItemDto> resultItems = new ArrayList<>();
        if (!text.isBlank()) {
            for (Item item : items.values()) {
                if (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase())) {
                    if (item.getAvailable().equals(true)) {
                        resultItems.add(itemMapper.toItemDto(item));
                    }
                }
            }
        }
        return resultItems;
    }

    private Integer generateId() {
        return ++count;
    }

    private void validateItem(Item item) {
        if (item.getName() == null) {
            log.info("Название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        } else if (item.getName().isBlank()) {
            log.info("Название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }
        if (item.getDescription() == null) {
            log.info("Описание не может быть пустым");
            throw new ValidationException("Описание не может быть пустым");
        } else if (item.getDescription().isBlank()) {
            log.info("Описание не может быть пустым");
            throw new ValidationException("Описание не может быть пустым");
        }
        if (item.getAvailable() == null) {
            log.info("Статус не может быть пустым");
            throw new ValidationException("Статус не может быть пустым");
        }
    }
}
