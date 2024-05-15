package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDates;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemMapper itemMapper;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto createItem(ItemDto itemDto, int userId) {
        validateItem(itemDto);
        Item item = itemMapper.toItem(itemDto, userService.getUserById(userId));
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(int itemId, ItemDto itemDto, int userId) {
        Optional<Item> opItem = itemRepository.findById(itemId);
        Item itemFromDB = null;
        if (opItem.isPresent()) {
            itemFromDB = opItem.get();
            if (userId == itemFromDB.getOwner().getId()) {
                if (itemDto.getName() != null) {
                    itemFromDB.setName(itemDto.getName());
                }
                if (itemDto.getDescription() != null) {
                    itemFromDB.setDescription(itemDto.getDescription());
                }
                if (itemDto.getAvailable() != null) {
                    itemFromDB.setAvailable(itemDto.getAvailable());
                }
            } else {
                log.info("Вещь может изменить только владелец, itemId = {}", itemId);
                throw new ObjectNotFoundException("Вещь может изменить только владелец");
            }
        } else {
            log.info("Вещь с id {} не найдена.", itemId);
            throw new ObjectNotFoundException("Вещь не найдена");
        }
        return itemMapper.toItemDto(itemRepository.save(itemFromDB));
    }

    @Override
    public ItemDtoWithDates getItem(int itemId, int userId) {
        Optional<Item> opItem = itemRepository.findById(itemId);
        if (opItem.isPresent()) {
            Item item = opItem.get();
            ItemDtoWithDates itemDtoWithDates = itemMapper.toItemDtoWithDates(item);
            if (item.getOwner().getId() == userId) {
                LocalDateTime now = LocalDateTime.now();
                BookingDtoForItem nextBooking = bookingRepository.findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(item.getId(), now, Status.APPROVED);
                BookingDtoForItem lastBooking = bookingRepository.findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(item.getId(), now, Status.APPROVED);
                itemDtoWithDates.setLastBooking(lastBooking);
                itemDtoWithDates.setNextBooking(nextBooking);
            } else {
                itemDtoWithDates.setLastBooking(null);
                itemDtoWithDates.setNextBooking(null);
            }
            itemDtoWithDates.setComments(commentMapper.toCommentDto(commentRepository.findAllByItemId(itemId)));
            return itemDtoWithDates;
        } else {
            log.info("Вещь с id {} не найдена.", itemId);
            throw new ObjectNotFoundException("Вещь не найдена");
        }
    }

    @Override
    public Item getItemForBooking(int itemId) {
        Optional<Item> opItem = itemRepository.findById(itemId);
        if (opItem.isPresent()) {
            return opItem.get();
        } else {
            log.info("Вещь с id {} не найдена.", itemId);
            throw new ObjectNotFoundException("Вещь не найдена");
        }
    }

    @Override
    public Collection<ItemDtoWithDates> getAllItems(int userId) {
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<ItemDtoWithDates> itemDtoWithDatesList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Item item : items) {
            BookingDtoForItem nextBooking = bookingRepository.findFirst1ByItemIdAndStartIsAfterAndStatusOrderByStartAsc(item.getId(), now, Status.APPROVED);
            BookingDtoForItem lastBooking = bookingRepository.findFirst1ByItemIdAndStartIsBeforeAndStatusOrderByStartDesc(item.getId(), now, Status.APPROVED);
            ItemDtoWithDates itemDtoWithDates = itemMapper.toItemDtoWithDates(item);
            itemDtoWithDates.setLastBooking(lastBooking);
            itemDtoWithDates.setNextBooking(nextBooking);
            itemDtoWithDates.setComments(commentMapper.toCommentDto(commentRepository.findAllByItemId(item.getId())));
            itemDtoWithDatesList.add(itemDtoWithDates);
        }
        return itemDtoWithDatesList;
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        List<Item> resultItems = new ArrayList<>();
        if (!text.isBlank()) {
            resultItems = itemRepository.search(text);
        }
        return itemMapper.toItemDto(resultItems);
    }

    @Override
    public CommentDto createComment(Comment comment, int itemId, int userId) {
        Booking booking = bookingRepository.findFirst1ByBookerIdAndItemIdOrderByEndAsc(userId, itemId);
        if (booking != null) {
            if (booking.getEnd().isBefore(LocalDateTime.now())) {
                if (comment.getText() != null) {
                    if (!comment.getText().isBlank()) {
                        comment.setCreated(LocalDateTime.now());
                        comment.setAuthor(booking.getBooker());
                        comment.setItem(booking.getItem());
                        return commentMapper.toCommentDto(commentRepository.save(comment));
                    } else {
                        throw new ValidationException("Текст не может быть пустым");
                    }
                } else {
                    throw new ValidationException("Текст не может быть пустым");
                }
            } else {
                log.info("Бронирование еще не закончено");
                throw new ValidationException("Бронирование еще не закончено");
            }
        } else {
            log.info("Бронирование не найдено");
            throw new ObjectNotFoundException("Бронирование не найдено");
        }
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
