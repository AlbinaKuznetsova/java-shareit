package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    @Mock
    ItemRequestRepository itemRequestRepository;
    ItemRequestMapper itemRequestMapper;
    @Mock
    UserService userService;
    @Mock
    ItemService itemService;
    ItemRequestService itemRequestService;
    UserDto user;
    ItemRequestDtoIn itemRequestDtoIn;
    ItemRequest itemRequest;

    @BeforeEach
    void beforeEach() {
        itemRequestMapper = new ItemRequestMapperImpl();
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, itemRequestMapper, userService, itemService);
        user = new UserDto();
        user.setId(1);
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        itemRequestDtoIn = new ItemRequestDtoIn(1, "тестовый запрос", LocalDateTime.now());
        itemRequest = itemRequestMapper.toItemRequest(itemRequestDtoIn, user);
    }

    @Test
    void testCreateItemRequest() {
        Mockito.when(userService.getUserById(anyInt())).thenReturn(user);

        Mockito.when(itemRequestRepository.save(any())).thenReturn(itemRequest);

        ItemRequestDtoOut itemRequestDtoOut = itemRequestService.createItemRequest(itemRequestDtoIn, user.getId());

        Mockito.verify(userService, Mockito.times(1)).getUserById(user.getId());
        Mockito.verify(itemRequestRepository, Mockito.times(1)).save(itemRequest);

        Assertions.assertEquals(itemRequestMapper.toItemRequestDtoOut(itemRequest), itemRequestDtoOut);
    }

    @Test
    void testGetRequestsByRequestor() {
        Mockito.when(userService.getUserById(anyInt())).thenReturn(user);
        Mockito.when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(anyInt())).thenReturn(
                List.of(itemRequest));
        Mockito.when(itemService.findByRequestId(anyInt())).thenReturn(null);

        List<ItemRequestDtoOut> itemRequestDtoOut = itemRequestService.getRequestsByRequestor(user.getId());

        Mockito.verify(userService, Mockito.times(1)).getUserById(user.getId());
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findAllByRequestorIdOrderByCreatedDesc(user.getId());
        Assertions.assertTrue(itemRequestDtoOut.contains(itemRequestMapper.toItemRequestDtoOut(itemRequest)));
    }

    @Test
    void testGetAllRequests() {
        Mockito.when(userService.getUserById(anyInt())).thenReturn(user);
        Mockito.when(itemRequestRepository.findAll()).thenReturn(List.of(itemRequest));
        Mockito.when(itemService.findByRequestId(anyInt())).thenReturn(null);

        List<ItemRequestDtoOut> itemRequestDtoOuts = itemRequestService.getAllRequests(user.getId(), null, null);

        Mockito.verify(userService, Mockito.times(1)).getUserById(user.getId());
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findAll();
        Mockito.verify(itemService, Mockito.times(1)).findByRequestId(itemRequest.getId());

        Assertions.assertTrue(itemRequestDtoOuts.contains(itemRequestMapper.toItemRequestDtoOut(itemRequest)));

    }

    @Test
    void testGetRequestById() {
        Mockito.when(userService.getUserById(anyInt())).thenReturn(user);
        Mockito.when(itemRequestRepository.findById(itemRequest.getId())).thenAnswer(invocationOnMock -> {
            int itemRequestId = invocationOnMock.getArgument(0, Integer.class);
            if (itemRequestId == itemRequest.getId()) {
                return Optional.of(itemRequest);
            } else {
                return Optional.empty();
            }
        });
        Mockito.when(itemService.findByRequestId(anyInt())).thenReturn(null);

        ItemRequestDtoOut itemRequestDtoOut = itemRequestService.getRequestById(user.getId(), itemRequest.getId());

        Mockito.verify(userService, Mockito.times(1)).getUserById(user.getId());
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findById(user.getId());

        Assertions.assertEquals(itemRequestMapper.toItemRequestDtoOut(itemRequest), itemRequestDtoOut);

        // Пробуем получить несуществующее бронирование
        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getRequestById(user.getId(), itemRequest.getId() + 1000));

    }
}
