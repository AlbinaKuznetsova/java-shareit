package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    public final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") int userId) {
        return itemService.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable int itemId,
                              @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") int userId) {
        return itemService.updateItem(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable int itemId) {
        return itemService.getItem(itemId);
    }

    @GetMapping
    public Collection<ItemDto> getAllItems(@RequestHeader("X-Sharer-User-Id") int userId) {
        return itemService.getAllItems(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(@RequestParam(defaultValue = "") String text) {
        return itemService.searchItems(text);
    }

}
