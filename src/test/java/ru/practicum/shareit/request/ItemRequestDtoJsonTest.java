package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDtoOut> json;

    @Test
    void testItemRequestDto() throws Exception {
        ItemRequestMapper itemRequestMapper = new ItemRequestMapperImpl();
        User user = new User();
        user.setId(1);
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");

        ItemRequest itemRequest = new ItemRequest(1,
                "нужна лестница",
                user,
                LocalDateTime.of(2024, 6, 20, 10, 10, 10),
                null);
        ItemRequestDtoOut itemRequestDtoOut = itemRequestMapper.toItemRequestDtoOut(itemRequest);

        JsonContent<ItemRequestDtoOut> result = json.write(itemRequestDtoOut);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemRequestDtoOut.getDescription());
        assertThat(result).extractingJsonPathNumberValue("$.requestor.id").isEqualTo(itemRequestDtoOut.getRequestor().getId());
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(itemRequestDtoOut.getCreated().toString());
        assertThat(result).extractingJsonPathValue("$.items").isEqualTo(null);

    }
}
