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
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.util.List;

//Тесты для кастомных запросов
@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRepositoryTest {
    private final EntityManager em;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    User user;
    Item item;
    Item item2;
    Item item3;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        userRepository.save(user);

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
}
