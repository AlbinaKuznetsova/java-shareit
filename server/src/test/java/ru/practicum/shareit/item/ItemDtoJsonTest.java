package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDates;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoJsonTest {
    @Autowired
    private JacksonTester<ItemDto> json;
    @Autowired
    private JacksonTester<ItemDtoWithDates> json2;

    @Test
    void testItemDto() throws Exception {
        ItemMapper itemMapper = new ItemMapperImpl();
        User user = new User();
        user.setId(1);
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");

        Item item = new Item(1, "вещь", "описание", true, user, null);
        ItemDto itemDto = itemMapper.toItemDto(item);

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemDto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(itemDto.getAvailable());

    }

    @Test
    void testItemDtoWithDates() throws Exception {
        ItemMapper itemMapper = new ItemMapperImpl();
        User user = new User();
        user.setId(1);
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");

        Item item = new Item(1, "вещь", "описание", true, user, null);

        CommentDto commentDto = new CommentDto(1, "test", item.getId(), user.getName(), LocalDateTime.now());
        BookingDtoForItem bookingDtoForItem1 = new BookingDtoForItem() {
            @Override
            public Integer getId() {
                return 1;
            }

            @Override
            public Integer getBookerId() {
                return 1;
            }
        };

        ItemDtoWithDates itemDtoWithDates = itemMapper.toItemDtoWithDates(item);
        itemDtoWithDates.setNextBooking(bookingDtoForItem1);
        itemDtoWithDates.setComments(List.of(commentDto));

        JsonContent<ItemDtoWithDates> result = json2.write(itemDtoWithDates);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemDtoWithDates.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemDtoWithDates.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(itemDtoWithDates.getAvailable());
        assertThat(result).extractingJsonPathValue("$.lastBooking").isEqualTo(null);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(1);
    }
}
