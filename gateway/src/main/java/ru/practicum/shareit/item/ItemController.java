package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestBody ItemDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        validateItem(itemDto);
        return itemClient.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable int itemId,
                                             @RequestBody ItemDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Update item {}, itemId {}, userId={}", itemDto, itemId, userId);
        return itemClient.updateItem(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@PathVariable int itemId,
                                          @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Get item {}, userId={}", itemId, userId);
        return itemClient.getItem(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader("X-Sharer-User-Id") int userId,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get items, userId={}, from={}, size={}", userId, from, size);
        return itemClient.getAllItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam(defaultValue = "") String text,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                              @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Search items, text = {},  userId={}, from={}, size={}", text, userId, from, size);
        return itemClient.searchItems(text, from, size, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestBody @Valid CommentDto comment,
                                                @PathVariable int itemId,
                                                @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Creating comment {}, userId={}", comment, userId);
        return itemClient.createComment(comment, itemId, userId);
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null) {
            log.info("Название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        } else if (itemDto.getName().isBlank()) {
            log.info("Название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }
        if (itemDto.getDescription() == null) {
            log.info("Описание не может быть пустым");
            throw new ValidationException("Описание не может быть пустым");
        } else if (itemDto.getDescription().isBlank()) {
            log.info("Описание не может быть пустым");
            throw new ValidationException("Описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            log.info("Статус не может быть пустым");
            throw new ValidationException("Статус не может быть пустым");
        }
    }
}
