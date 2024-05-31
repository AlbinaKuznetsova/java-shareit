package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDates;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemServiceControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;
    @Autowired
    private MockMvc mvc;
    private User user;
    private User user2;
    private ItemMapper itemMapper;
    Item item;
    Item item2;
    ItemDto itemDto;
    ItemDtoWithDates itemDtoWithDates;
    ItemDtoWithDates itemDtoWithDates2;
    ItemDto itemDto2;

    @BeforeEach
    void beforeEach() {
        itemMapper = new ItemMapperImpl();

        user = new User();
        user.setId(1);
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");

        user2 = new User();
        user2.setId(2);
        user2.setName("www");
        user2.setEmail("www@yandex.ru");

        item = new Item();
        item.setId(1);
        item.setName("стол");
        item.setDescription("описание");
        item.setAvailable(true);
        item.setOwner(user);
        item2 = new Item();
        item2.setId(2);
        item2.setName("утюг парогенератор");
        item2.setDescription("описание2");
        item2.setAvailable(true);
        item2.setOwner(user);

        itemDto = itemMapper.toItemDto(item);
        itemDto2 = itemMapper.toItemDto(item2);
        itemDtoWithDates = itemMapper.toItemDtoWithDates(item);
        itemDtoWithDates2 = itemMapper.toItemDtoWithDates(item2);

    }

    @Test
    void testCreateItem() throws Exception {
        when(itemService.createItem(any(), anyInt())).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId())))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void testUpdateItem() throws Exception {
        when(itemService.updateItem(anyInt(), any(), anyInt())).thenReturn(itemDto2);

        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto2))
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto2.getId())))
                .andExpect(jsonPath("$.name", is(itemDto2.getName())))
                .andExpect(jsonPath("$.description", is(itemDto2.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto2.getAvailable())));
    }

    @Test
    void testGetItem() throws Exception {
        when(itemService.getItem(anyInt(), anyInt())).thenReturn(itemDtoWithDates);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId())))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.available", is(item.getAvailable())));
    }

    @Test
    void testGetAllItems() throws Exception {
        when(itemService.getAllItems(anyInt(), any(), any())).thenReturn(List.of(itemDtoWithDates, itemDtoWithDates2));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(item.getId())))
                .andExpect(jsonPath("$[0].name", is(item.getName())))
                .andExpect(jsonPath("$[0].description", is(item.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item.getAvailable())))
                .andExpect(jsonPath("$[1].id", is(item2.getId())))
                .andExpect(jsonPath("$[1].name", is(item2.getName())))
                .andExpect(jsonPath("$[1].description", is(item2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(item2.getAvailable())));
    }

    @Test
    void testSearchItems() throws Exception {
        when(itemService.searchItems(anyString(), any(), any())).thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search?text=стол")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId())))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())));

    }

    @Test
    void testCreateComment() throws Exception {
        CommentMapper commentMapper = new CommentMapperImpl();
        Comment comment = new Comment(1, "комментарий", item, user, LocalDateTime.now());
        CommentDto commentDto = commentMapper.toCommentDto(comment);

        when(itemService.createComment(any(), anyInt(), anyInt())).thenReturn(commentDto);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId())))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.itemId", is(commentDto.getItemId())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
    }
}
