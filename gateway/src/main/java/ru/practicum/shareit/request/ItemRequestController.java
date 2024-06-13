package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    ResponseEntity<Object> createItemRequest(@RequestBody @Valid ItemRequestDtoIn itemRequest,
                                             @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Create itemRequest {}, userId={}", itemRequest, userId);
        return itemRequestClient.createItemRequest(itemRequest, userId);
    }

    @GetMapping
    ResponseEntity<Object> getRequestsByRequestor(@RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Create itemRequest by requestor, userId={}", userId);
        return itemRequestClient.getRequestsByRequestor(userId);
    }

    @GetMapping("/all")
    ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") int userId,
                                          @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                          @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get all requests, userId={}, from {}, size {}", userId, from, size);
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") int userId,
                                          @PathVariable int requestId) {
        log.info("Create itemRequest by id {}, userId={}", requestId, userId);
        return itemRequestClient.getRequestById(userId, requestId);
    }
}
