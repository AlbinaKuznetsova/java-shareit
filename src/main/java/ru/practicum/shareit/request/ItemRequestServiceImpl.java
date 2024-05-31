package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public ItemRequestDtoOut createItemRequest(ItemRequestDtoIn itemRequestDtoIn, int userId) {
        validateRequest(itemRequestDtoIn);
        User user = userService.getUserById(userId);
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDtoIn, user);
        return itemRequestMapper.toItemRequestDtoOut(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDtoOut> getRequestsByRequestor(int userId) {
        userService.getUserById(userId);
        List<ItemRequestDtoOut> itemRequestsDto = itemRequestMapper.toItemRequestDtoOut(
                itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId));
        for (ItemRequestDtoOut itemRequestDto : itemRequestsDto) {
            itemRequestDto.setItems(itemService.findByRequestId(itemRequestDto.getId()));
        }
        return itemRequestsDto;
    }

    @Override
    public List<ItemRequestDtoOut> getAllRequests(int userId, Integer from, Integer size) {
        userService.getUserById(userId);
        if (from == null || size == null) {
            List<ItemRequestDtoOut> itemRequestsDto = itemRequestMapper.toItemRequestDtoOut(itemRequestRepository.findAll());
            for (ItemRequestDtoOut itemRequestDto : itemRequestsDto) {
                itemRequestDto.setItems(itemService.findByRequestId(itemRequestDto.getId()));
            }
            return itemRequestsDto;
        } else {
            validatePageParams(from, size);
            int pageNumber = from / size;
            Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "created"));
            List<ItemRequestDtoOut> itemRequestsDto = itemRequestMapper.toItemRequestDtoOut(
                    itemRequestRepository.findAllByRequestorIdNot(userId, pageable).toList());
            for (ItemRequestDtoOut itemRequestDto : itemRequestsDto) {
                itemRequestDto.setItems(itemService.findByRequestId(itemRequestDto.getId()));
            }
            return itemRequestsDto;
        }
    }

    @Override
    public ItemRequestDtoOut getRequestById(int userId, int requestId) {
        userService.getUserById(userId);
        Optional<ItemRequest> itemRequestOptional = itemRequestRepository.findById(requestId);
        if (itemRequestOptional.isPresent()) {
            ItemRequestDtoOut itemRequestDto = itemRequestMapper.toItemRequestDtoOut(itemRequestOptional.get());
            itemRequestDto.setItems(itemService.findByRequestId(itemRequestDto.getId()));
            return itemRequestDto;
        } else {
            throw new ObjectNotFoundException("Запрос не найден");
        }
    }

    private void validateRequest(ItemRequestDtoIn itemRequestDtoIn) {
        if (itemRequestDtoIn.getDescription() == null) {
            log.info("Описание не может быть пустым");
            throw new ValidationException("Описание не может быть пустым");
        } else if (itemRequestDtoIn.getDescription().isBlank()) {
            log.info("Описание не может быть пустым");
            throw new ValidationException("Описание не может быть пустым");
        }
    }

    private void validatePageParams(Integer from, Integer size) {
        if (from < 0) {
            throw new ValidationException("Индекс элемента не может быть меньше 0");
        }
        if (size < 1) {
            throw new ValidationException("Количество элементов не может быть меньше 1");
        }
    }
}
