package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestServiceControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mvc;
    private ItemRequestMapper itemRequestMapper;

    private User user;
    private User user2;
    private ItemRequestDtoIn itemRequestDtoIn;
    private ItemRequestDtoOut itemRequestDtoOut;
    private ItemRequestDtoIn itemRequestDtoIn2;
    private ItemRequestDtoOut itemRequestDtoOut2;

    @BeforeEach
    void beforeEach() {
        itemRequestMapper = new ItemRequestMapperImpl();

        user = new User();
        user.setId(1);
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");

        user2 = new User();
        user2.setId(2);
        user2.setName("www");
        user2.setEmail("www@yandex.ru");

        itemRequestDtoIn = new ItemRequestDtoIn(1, "нужен пылесос", LocalDateTime.of(2024, 6, 10, 1, 1, 1));
        itemRequestDtoOut = itemRequestMapper.toItemRequestDtoOut(
                itemRequestMapper.toItemRequest(itemRequestDtoIn, user));

        itemRequestDtoIn2 = new ItemRequestDtoIn(2, "нужен шуруповерт", LocalDateTime.of(2024, 6, 10, 1, 1, 1));
        itemRequestDtoOut2 = itemRequestMapper.toItemRequestDtoOut(
                itemRequestMapper.toItemRequest(itemRequestDtoIn2, user2));
    }

    @Test
    void testCreateItemRequest() throws Exception {
        when(itemRequestService.createItemRequest(any(), anyInt())).thenReturn(itemRequestDtoOut);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDtoIn))
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDtoOut.getId())))
                .andExpect(jsonPath("$.description", is(itemRequestDtoOut.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDtoOut.getCreated().toString())))
                .andExpect(jsonPath("$.requestor.id", is(itemRequestDtoOut.getRequestor().getId())));
    }

    @Test
    void testGetRequestsByRequestor() throws Exception {
        when(itemRequestService.getRequestsByRequestor(anyInt())).thenReturn(List.of(itemRequestDtoOut));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestDtoOut.getId())))
                .andExpect(jsonPath("$[0].description", is(itemRequestDtoOut.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestDtoOut.getCreated().toString())))
                .andExpect(jsonPath("$[0].requestor.id", is(itemRequestDtoOut.getRequestor().getId())));

    }

    @Test
    void testGetAllRequests() throws Exception {
        when(itemRequestService.getAllRequests(anyInt(), any(), any())).thenReturn(List.of(itemRequestDtoOut, itemRequestDtoOut2));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemRequestDtoOut.getId())))
                .andExpect(jsonPath("$[0].description", is(itemRequestDtoOut.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestDtoOut.getCreated().toString())))
                .andExpect(jsonPath("$[0].requestor.id", is(itemRequestDtoOut.getRequestor().getId())))
                .andExpect(jsonPath("$[1].id", is(itemRequestDtoOut2.getId())))
                .andExpect(jsonPath("$[1].description", is(itemRequestDtoOut2.getDescription())))
                .andExpect(jsonPath("$[1].created", is(itemRequestDtoOut2.getCreated().toString())))
                .andExpect(jsonPath("$[1].requestor.id", is(itemRequestDtoOut2.getRequestor().getId())));
    }

    @Test
    void testGetRequestById() throws Exception {
        when(itemRequestService.getRequestById(anyInt(), anyInt())).thenReturn(itemRequestDtoOut2);

        mvc.perform(get("/requests/2")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDtoOut2.getId())))
                .andExpect(jsonPath("$.description", is(itemRequestDtoOut2.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDtoOut2.getCreated().toString())))
                .andExpect(jsonPath("$.requestor.id", is(itemRequestDtoOut2.getRequestor().getId())));
    }
}
