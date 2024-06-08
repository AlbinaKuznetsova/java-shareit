package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestRepositoryTest {
    private final EntityManager em;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    User user;
    User user2;
    ItemRequest itemRequest;
    ItemRequest itemRequest2;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        userRepository.save(user);

        user2 = new User();
        user2.setName("тестовый пользователь");
        user2.setEmail("test2@yandex.ru");
        userRepository.save(user2);

        itemRequest = new ItemRequest();
        itemRequest.setDescription("Нужен стул");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(user);
        itemRequestRepository.save(itemRequest);

        itemRequest2 = new ItemRequest();
        itemRequest2.setDescription("Нужен стол");
        itemRequest2.setCreated(LocalDateTime.now());
        itemRequest2.setRequestor(user2);
        itemRequestRepository.save(itemRequest2);
    }

    @Test
    void testFindAllByRequestorIdNot() {
        Pageable pageable = PageRequest.of(0, 1);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdNot(user.getId(), pageable).toList();

        Assertions.assertEquals(1, itemRequests.size());
        Assertions.assertTrue(itemRequests.contains(itemRequest2));
        Assertions.assertFalse(itemRequests.contains(itemRequest));
    }

    @Test
    void testfindAllByRequestor() {
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(user.getId());

        Assertions.assertEquals(1, itemRequests.size());
        Assertions.assertTrue(itemRequests.contains(itemRequest));
        Assertions.assertFalse(itemRequests.contains(itemRequest2));
    }
}
