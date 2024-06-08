package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserMapperImpl;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceIntegrationTest {
    private final EntityManager em;
    private final UserService userService;
    private final ItemRequestService itemRequestService;

    UserDto user;
    UserDto user2;
    ItemRequestDtoIn itemRequestDtoIn;
    ItemRequestDtoIn itemRequestDtoIn2;
    UserMapper userMapper;

    @BeforeEach
    void beforeEach() {
        userMapper = new UserMapperImpl();
        user = new UserDto();
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        user2 = new UserDto();
        user2.setName("www");
        user2.setEmail("www@yandex.ru");

        user = userService.createUser(user);
        user2 = userService.createUser(user2);

        itemRequestDtoIn = new ItemRequestDtoIn();
        itemRequestDtoIn.setDescription("нужен пылесос");

        itemRequestDtoIn2 = new ItemRequestDtoIn();
        itemRequestDtoIn2.setDescription("нужна лестница");
    }

    @Test
    void testCreateItemRequest() {
        itemRequestService.createItemRequest(itemRequestDtoIn, user.getId());

        TypedQuery<ItemRequest> query = em.createQuery("Select i from ItemRequest i where i.description = :description", ItemRequest.class);
        ItemRequest itemRequestFromDB = query.setParameter("description", itemRequestDtoIn.getDescription()).getSingleResult();

        assertThat(itemRequestFromDB.getId(), notNullValue());
        assertThat(itemRequestFromDB.getDescription(), equalTo(itemRequestDtoIn.getDescription()));
        assertThat(itemRequestFromDB.getCreated(), notNullValue());
        assertThat(itemRequestFromDB.getRequestor(), equalTo(userMapper.toUser(user)));

        itemRequestDtoIn.setDescription("");
        Assertions.assertThrows(ValidationException.class, () -> itemRequestService.createItemRequest(itemRequestDtoIn, user.getId()));
    }

    @Test
    void testGetRequestsByRequestor() {
        ItemRequestDtoOut itemRequestDtoOut = itemRequestService.createItemRequest(itemRequestDtoIn, user.getId());
        itemRequestService.createItemRequest(itemRequestDtoIn2, user2.getId());

        List<ItemRequestDtoOut> requests = itemRequestService.getRequestsByRequestor(user.getId());
        assertThat(requests.size(), equalTo(1));

        Assertions.assertEquals(requests.get(0).getId(), itemRequestDtoOut.getId());
        Assertions.assertEquals(requests.get(0).getRequestor(), itemRequestDtoOut.getRequestor());
        Assertions.assertEquals(requests.get(0).getDescription(), itemRequestDtoOut.getDescription());
        Assertions.assertEquals(requests.get(0).getCreated(), itemRequestDtoOut.getCreated());
    }

    @Test
    void testGetAllRequests() {
        ItemRequestDtoOut itemRequestDtoOut = itemRequestService.createItemRequest(itemRequestDtoIn, user.getId());
        ItemRequestDtoOut itemRequestDtoOut2 = itemRequestService.createItemRequest(itemRequestDtoIn2, user2.getId());

        List<ItemRequestDtoOut> requests = itemRequestService.getAllRequests(user.getId(), null, null);

        assertThat(requests.size(), equalTo(2));
        Assertions.assertEquals(requests.get(0).getId(), itemRequestDtoOut.getId());
        Assertions.assertEquals(requests.get(0).getRequestor(), itemRequestDtoOut.getRequestor());
        Assertions.assertEquals(requests.get(0).getDescription(), itemRequestDtoOut.getDescription());
        Assertions.assertEquals(requests.get(0).getCreated(), itemRequestDtoOut.getCreated());
        Assertions.assertEquals(requests.get(1).getId(), itemRequestDtoOut2.getId());
        Assertions.assertEquals(requests.get(1).getRequestor(), itemRequestDtoOut2.getRequestor());
        Assertions.assertEquals(requests.get(1).getDescription(), itemRequestDtoOut2.getDescription());
        Assertions.assertEquals(requests.get(1).getCreated(), itemRequestDtoOut2.getCreated());

        requests = itemRequestService.getAllRequests(user.getId(), 0, 1);
        assertThat(requests.size(), equalTo(1));

        Assertions.assertThrows(ValidationException.class, () -> itemRequestService.getAllRequests(user.getId(), -1, 1));
        Assertions.assertThrows(ValidationException.class, () -> itemRequestService.getAllRequests(user.getId(), 0, -1));
    }

    @Test
    void testGetRequestById() {
        ItemRequestDtoOut itemRequestDtoOut = itemRequestService.createItemRequest(itemRequestDtoIn, user.getId());

        ItemRequestDtoOut request = itemRequestService.getRequestById(user.getId(), itemRequestDtoOut.getId());

        Assertions.assertEquals(request.getId(), itemRequestDtoOut.getId());
        Assertions.assertEquals(request.getRequestor(), itemRequestDtoOut.getRequestor());
        Assertions.assertEquals(request.getDescription(), itemRequestDtoOut.getDescription());
        Assertions.assertEquals(request.getCreated(), itemRequestDtoOut.getCreated());
    }
}
