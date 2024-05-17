package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDates;
import ru.practicum.shareit.item.model.Comment;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestBody ItemDto itemDto,
                                              @RequestHeader("X-Sharer-User-Id") int userId) {
        return ResponseEntity.ok().body(itemService.createItem(itemDto, userId));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@PathVariable int itemId,
                                              @RequestBody ItemDto itemDto,
                                              @RequestHeader("X-Sharer-User-Id") int userId) {
        return ResponseEntity.ok().body(itemService.updateItem(itemId, itemDto, userId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDtoWithDates> getItem(@PathVariable int itemId,
                                                    @RequestHeader("X-Sharer-User-Id") int userId) {
        return ResponseEntity.ok().body(itemService.getItem(itemId, userId));
    }

    @GetMapping
    public ResponseEntity<Collection<ItemDtoWithDates>> getAllItems(@RequestHeader("X-Sharer-User-Id") int userId) {
        return ResponseEntity.ok().body(itemService.getAllItems(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<ItemDto>> searchItems(@RequestParam(defaultValue = "") String text) {
        return ResponseEntity.ok().body(itemService.searchItems(text));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> createComment(@RequestBody Comment comment,
                                                 @PathVariable int itemId,
                                                 @RequestHeader("X-Sharer-User-Id") int userId) {
        return ResponseEntity.ok().body(itemService.createComment(comment, itemId, userId));
    }

}
