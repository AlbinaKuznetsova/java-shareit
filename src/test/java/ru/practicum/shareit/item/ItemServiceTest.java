package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDates;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestMapperImpl;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserMapperImpl;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserService userService;
    ItemMapper itemMapper;
    @Mock
    BookingService bookingService;
    @Mock
    CommentRepository commentRepository;
    @Mock
    ItemRequestService itemRequestService;
    ItemRequestMapper itemRequestMapper;
    CommentMapper commentMapper;
    ItemService itemService;
    UserDto user;
    UserDto user2;
    ItemDto itemDto;
    Item item;
    UserMapper userMapper;

    @BeforeEach
    void beforeEach() {
        itemMapper = new ItemMapperImpl();
        commentMapper = new CommentMapperImpl();
        userMapper = new UserMapperImpl();
        itemRequestMapper = new ItemRequestMapperImpl();
        itemService = new ItemServiceImpl(itemRepository, userService, itemMapper, bookingService, commentRepository, commentMapper);

        user = new UserDto();
        user.setId(1);
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        user2 = new UserDto();
        user2.setId(2);
        user2.setName("www");
        user2.setEmail("www@yandex.ru");
        itemDto = new ItemDto(1, "вещь", "описание", true, null);
        item = itemMapper.toItem(itemDto, user);
    }

    @Test
    void testCreateItem() {
        Mockito.when(userService.getUserById(anyInt())).thenReturn(user);
        Mockito.when(itemRepository.save(any())).thenReturn(item);
        ItemDto newItemDto = itemService.createItem(itemDto, user.getId());

        Mockito.verify(userService, Mockito.times(1)).getUserById(user.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).save(any(Item.class));
        Assertions.assertEquals(itemDto, newItemDto);

        // Пробуем создать вещь с пустыми полями
        itemDto.setName(null);
        Assertions.assertThrows(ValidationException.class, () -> itemService.createItem(itemDto, user.getId()));

        itemDto.setName("test");
        itemDto.setDescription("");
        Assertions.assertThrows(ValidationException.class, () -> itemService.createItem(itemDto, user.getId()));

        itemDto.setDescription("test");
        itemDto.setAvailable(null);
        Assertions.assertThrows(ValidationException.class, () -> itemService.createItem(itemDto, user.getId()));
    }

    @Test
    void testUpdateItem() {
        Mockito.when(itemRepository.findById(anyInt())).thenAnswer(invocationOnMock -> {
            int itemId = invocationOnMock.getArgument(0, Integer.class);
            if (itemId == item.getId()) {
                return Optional.of(item);
            } else {
                return Optional.empty();
            }
        });
        Mockito.when(itemRepository.save(any())).thenReturn(item);

        itemDto.setDescription("новое описание");
        itemDto.setName("TEST");

        ItemDto newItemDto = itemService.updateItem(item.getId(), itemDto, user.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).save(item);
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());

        Assertions.assertEquals(item, itemMapper.toItem(newItemDto, user));

        // Пробуем изменить вещь не владельцем
        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> itemService.updateItem(item.getId(), itemDto, user.getId() + 1000));

        // Пробуем изменить несуществующую вещь
        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> itemService.updateItem(item.getId() + 1000, itemDto, user.getId()));


    }

    @Test
    void testGetItem() {
        Mockito.when(itemRepository.findById(anyInt())).thenAnswer(invocationOnMock -> {
            int itemId = invocationOnMock.getArgument(0, Integer.class);
            if (itemId == item.getId()) {
                return Optional.of(item);
            } else {
                return Optional.empty();
            }
        });

        Comment comment = new Comment(1, "comment", item, userMapper.toUser(user2), LocalDateTime.now());
        Mockito.when(bookingService.getNextBooking(anyInt(), any(), any())).thenReturn(null);
        Mockito.when(bookingService.getLastBooking(anyInt(), any(), any())).thenReturn(null);
        Mockito.when(commentRepository.findAllByItemId(anyInt())).thenReturn(List.of(comment));

        ItemDtoWithDates itemDtoWithDates = itemService.getItem(item.getId(), user.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Mockito.verify(commentRepository, Mockito.times(1)).findAllByItemId(item.getId());

        Assertions.assertEquals(item.getId(), itemDtoWithDates.getId());
        Assertions.assertEquals(item.getName(), itemDtoWithDates.getName());
        Assertions.assertEquals(item.getDescription(), itemDtoWithDates.getDescription());
        Assertions.assertNull(itemDtoWithDates.getLastBooking());
        Assertions.assertNull(itemDtoWithDates.getNextBooking());
        Assertions.assertEquals(commentMapper.toCommentDto(comment), itemDtoWithDates.getComments().get(0));

        // Пробуем получить несуществующую вещь
        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> itemService.getItem(item.getId() + 1000, user.getId()));

    }

    @Test
    void testGetItemForBooking() {
        Mockito.when(itemRepository.findById(anyInt())).thenAnswer(invocationOnMock -> {
            int itemId = invocationOnMock.getArgument(0, Integer.class);
            if (itemId == item.getId()) {
                return Optional.of(item);
            } else {
                return Optional.empty();
            }
        });
        Item item2 = itemService.getItemForBooking(item.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).findById(item.getId());
        Assertions.assertEquals(item, item2);

        // Пробуем получить несуществующую вещь
        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> itemService.getItemForBooking(item.getId() + 1000));

    }

    @Test
    void testGetAllItems() {
        Mockito.when(itemRepository.findAllByOwnerId(anyInt())).thenAnswer(invocationOnMock -> {
            int itemId = invocationOnMock.getArgument(0, Integer.class);
            if (itemId == item.getId()) {
                return List.of(item);
            } else {
                return List.of();
            }
        });

        Comment comment = new Comment(1, "comment", item, userMapper.toUser(user2), LocalDateTime.now());
        Mockito.when(bookingService.getNextBooking(anyInt(), any(), any())).thenReturn(null);
        Mockito.when(bookingService.getLastBooking(anyInt(), any(), any())).thenReturn(null);
        Mockito.when(commentRepository.findAllByItemId(anyInt())).thenReturn(List.of(comment));
        Collection<ItemDtoWithDates> itemDtoWithDates = itemService.getAllItems(user.getId(), null, null);

        Mockito.verify(itemRepository, Mockito.times(1)).findAllByOwnerId(user.getId());
        Mockito.verify(commentRepository, Mockito.times(1)).findAllByItemId(item.getId());

        Object[] items = itemDtoWithDates.toArray();
        Assertions.assertEquals(1, items.length);
        Assertions.assertEquals(item.getId(), ((ItemDtoWithDates) items[0]).getId());
        Assertions.assertEquals(item.getName(), ((ItemDtoWithDates) items[0]).getName());
        Assertions.assertEquals(item.getDescription(), ((ItemDtoWithDates) items[0]).getDescription());
        Assertions.assertNull(((ItemDtoWithDates) items[0]).getLastBooking());
        Assertions.assertNull(((ItemDtoWithDates) items[0]).getNextBooking());
        Assertions.assertEquals(commentMapper.toCommentDto(comment), ((ItemDtoWithDates) items[0]).getComments().get(0));

        //Пользователь, не создавший ни одной вещи
        itemDtoWithDates = itemService.getAllItems(user2.getId(), null, null);
        Assertions.assertEquals(0, itemDtoWithDates.size());


    }

    @Test
    void testSearchItems() {
        Mockito.when(itemRepository.search(any())).thenReturn(List.of(item));
        Collection<ItemDto> itemDtos = itemService.searchItems("test", null, null);
        Mockito.verify(itemRepository, Mockito.times(1)).search("test");

        Assertions.assertEquals(itemMapper.toItemDto(item), itemDtos.toArray()[0]);


    }

    @Test
    void testCreateComment() {
        Booking booking = new Booking();
        booking.setId(1);
        booking.setStatus(Status.APPROVED);
        booking.setItem(item);
        booking.setBooker(userMapper.toUser(user2));
        booking.setStart(LocalDateTime.of(2024, 5, 10, 0, 0));
        booking.setEnd(LocalDateTime.of(2024, 5, 12, 0, 0));
        Mockito.when(bookingService.getBookingForComment(anyInt(), anyInt())).thenAnswer(invocationOnMock -> {
            int userId = invocationOnMock.getArgument(0, Integer.class);
            int itemId = invocationOnMock.getArgument(1, Integer.class);
            if ((itemId == item.getId()) && (userId == user2.getId())) {
                return booking;
            } else {
                return null;
            }
        });
        Comment comment = new Comment(1, "comment", item, userMapper.toUser(user2), LocalDateTime.now());
        Mockito.when(commentRepository.save(any())).thenReturn(comment);

        CommentDto commentDto = itemService.createComment(comment, item.getId(), user2.getId());
        Mockito.verify(bookingService, Mockito.times(1)).getBookingForComment(user2.getId(), item.getId());
        Mockito.verify(commentRepository, Mockito.times(1)).save(comment);
        Assertions.assertEquals(commentMapper.toCommentDto(comment), commentDto);

        Assertions.assertThrows(ObjectNotFoundException.class, () -> itemService.createComment(comment, item.getId() + 1000, user2.getId()));

        // Пробуем сохранить комментарий с пустым текстом
        comment.setText(null);
        Assertions.assertThrows(ValidationException.class, () -> itemService.createComment(comment, item.getId(), user2.getId()));

        // Пробуем сохранить для неоконченного бронирования
        comment.setText("text");
        booking.setEnd(LocalDateTime.of(2024, 9, 12, 0, 0));
        Assertions.assertThrows(ValidationException.class, () -> itemService.createComment(comment, item.getId(), user2.getId()));

    }
}
