package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRepositoryTest {
    private final EntityManager em;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    User user;
    Item item;
    Item item2;
    Item item3;
    ItemRequest itemRequest;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        user = userRepository.save(user);

        itemRequest = new ItemRequest();
        itemRequest.setDescription("Нужен стул");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(user);
        itemRequest = itemRequestRepository.save(itemRequest);

        item = new Item();
        item.setName("Робот пылесос");
        item.setDescription("Описание пылесоса");
        item.setAvailable(true);
        item.setOwner(user);
        itemRepository.save(item);

        item2 = new Item();
        item2.setName("Мойщик окон");
        item2.setDescription("Робот мойщик окон");
        item2.setAvailable(true);
        item2.setOwner(user);
        item2.setRequest(itemRequest);
        itemRepository.save(item2);

        item3 = new Item();
        item3.setName("Фонарь");
        item3.setDescription("Описание");
        item3.setAvailable(true);
        item3.setOwner(user);
        itemRepository.save(item3);
    }

    @Test
    void testSearchItem() {
        List<Item> items = itemRepository.search("робот");

        Assertions.assertEquals(2, items.size());
        Assertions.assertTrue(items.contains(item));
        Assertions.assertTrue(items.contains(item2));
    }

    @Test
    void testPageSearchItem() {
        Pageable pageable = PageRequest.of(0, 1);
        List<Item> items = itemRepository.search("робот", pageable).toList();

        Assertions.assertEquals(1, items.size());
        Assertions.assertTrue(items.contains(item));
        Assertions.assertFalse(items.contains(item2));
    }

    @Test
    void testFindAllByOwnerId() {
        List<Item> items = itemRepository.findAllByOwnerId(user.getId());

        Assertions.assertEquals(3, items.size());
        Assertions.assertTrue(items.contains(item));
        Assertions.assertTrue(items.contains(item2));
        Assertions.assertTrue(items.contains(item3));
    }

    @Test
    void testFindAllByOwnerIdPage() {
        Pageable pageable = PageRequest.of(0, 2);
        List<Item> items = itemRepository.findAllByOwnerId(user.getId(), pageable).toList();

        Assertions.assertEquals(2, items.size());
        Assertions.assertTrue(items.contains(item));
        Assertions.assertTrue(items.contains(item2));
        Assertions.assertFalse(items.contains(item3));
    }

    @Test
    void testFindAllByRequestId() {
        List<Item> items = itemRepository.findAllByRequestId(itemRequest.getId());

        Assertions.assertEquals(1, items.size());
        Assertions.assertTrue(items.contains(item2));
    }
}
