package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    ResponseEntity<ItemRequestDtoOut> createItemRequest(@RequestBody ItemRequestDtoIn itemRequest,
                                                        @RequestHeader("X-Sharer-User-Id") int userId) {
        return ResponseEntity.ok().body(itemRequestService.createItemRequest(itemRequest, userId));
    }

    @GetMapping
    ResponseEntity<List<ItemRequestDtoOut>> getRequestsByRequestor(@RequestHeader("X-Sharer-User-Id") int userId) {
        return ResponseEntity.ok().body(itemRequestService.getRequestsByRequestor(userId));
    }

    @GetMapping("/all")
    ResponseEntity<List<ItemRequestDtoOut>> getAllRequests(@RequestHeader("X-Sharer-User-Id") int userId,
                                                           @RequestParam(required = false) Integer from,
                                                           @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok().body(itemRequestService.getAllRequests(userId, from, size));
    }

    @GetMapping("/{requestId}")
    ResponseEntity<ItemRequestDtoOut> getRequestById(@RequestHeader("X-Sharer-User-Id") int userId,
                                                     @PathVariable int requestId) {
        return ResponseEntity.ok().body(itemRequestService.getRequestById(userId, requestId));
    }
}
