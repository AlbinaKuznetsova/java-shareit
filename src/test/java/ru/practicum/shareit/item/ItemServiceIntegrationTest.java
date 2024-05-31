package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDates;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceIntegrationTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final ItemMapper itemMapper;
    User user;
    User user2;
    Item item;
    Item item2;
    ItemDto itemDto;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        user2 = new User();
        user2.setName("www");
        user2.setEmail("www@yandex.ru");

        item = new Item();
        item.setName("стол");
        item.setDescription("описание");
        item.setAvailable(true);
        item2 = new Item();
        item2.setName("утюг парогенератор");
        item2.setDescription("описание2");
        item2.setAvailable(true);

        userService.createUser(user);
        userService.createUser(user2);
    }

    @Test
    void testCreateItem() {
        itemDto = itemService.createItem(itemMapper.toItemDto(item), user.getId());

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.id = :id", Item.class);
        Item itemFromDB = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertThat(itemFromDB.getId(), equalTo(itemDto.getId()));
        assertThat(itemFromDB.getName(), equalTo(itemDto.getName()));
        assertThat(itemFromDB.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(itemFromDB.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    void testUpdateItem() {
        itemDto = itemService.createItem(itemMapper.toItemDto(item), user.getId());
        ItemDto itemDto2 = itemMapper.toItemDto(item2);
        itemService.updateItem(itemDto.getId(), itemDto2, user.getId());

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.id = :id", Item.class);
        Item itemFromDB = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertThat(itemFromDB.getName(), equalTo(itemDto2.getName()));
        assertThat(itemFromDB.getDescription(), equalTo(itemDto2.getDescription()));
        assertThat(itemFromDB.getAvailable(), equalTo(itemDto2.getAvailable()));
    }

    @Test
    void testGetItem() {
        itemDto = itemService.createItem(itemMapper.toItemDto(item), user.getId());
        ItemDtoWithDates itemDto2 = itemService.getItem(itemDto.getId(), user.getId());

        assertThat(itemDto.getId(), equalTo(itemDto2.getId()));
        assertThat(itemDto.getName(), equalTo(itemDto2.getName()));
        assertThat(itemDto.getDescription(), equalTo(itemDto2.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(itemDto2.getAvailable()));
    }

    @Test
    void testGetItemForBooking() {
        itemDto = itemService.createItem(itemMapper.toItemDto(item), user.getId());
        Item item2 = itemService.getItemForBooking(itemDto.getId());

        assertThat(itemDto.getId(), equalTo(item2.getId()));
        assertThat(itemDto.getName(), equalTo(item2.getName()));
        assertThat(itemDto.getDescription(), equalTo(item2.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(item2.getAvailable()));
    }

    @Test
    void testGetAllItems() {
        itemDto = itemService.createItem(itemMapper.toItemDto(item), user.getId());
        ItemDto itemDto2 = itemService.createItem(itemMapper.toItemDto(item2), user.getId());

        Collection<ItemDtoWithDates> items = itemService.getAllItems(user.getId(), null, null);
        Object[] itemDtoWithDates = items.toArray();
        assertThat(items.size(), equalTo(2));
        Assertions.assertEquals(((ItemDtoWithDates) itemDtoWithDates[0]).getId(), itemDto.getId());
        Assertions.assertEquals(((ItemDtoWithDates) itemDtoWithDates[0]).getName(), itemDto.getName());
        Assertions.assertEquals(((ItemDtoWithDates) itemDtoWithDates[0]).getDescription(), itemDto.getDescription());
        Assertions.assertEquals(((ItemDtoWithDates) itemDtoWithDates[1]).getId(), itemDto2.getId());
        Assertions.assertEquals(((ItemDtoWithDates) itemDtoWithDates[1]).getName(), itemDto2.getName());
        Assertions.assertEquals(((ItemDtoWithDates) itemDtoWithDates[1]).getDescription(), itemDto2.getDescription());

        items = itemService.getAllItems(user.getId(), 0, 1);
        assertThat(items.size(), equalTo(1));
    }

    @Test
    void testSearchItems() {
        itemDto = itemService.createItem(itemMapper.toItemDto(item), user.getId());
        ItemDto itemDto2 = itemService.createItem(itemMapper.toItemDto(item2), user.getId());

        Collection<ItemDto> items = itemService.searchItems("утюг", null, null);
        Object[] itemDto = items.toArray();
        assertThat(items.size(), equalTo(1));
        Assertions.assertEquals(((ItemDto) itemDto[0]).getId(), itemDto2.getId());
        Assertions.assertEquals(((ItemDto) itemDto[0]).getName(), itemDto2.getName());
        Assertions.assertEquals(((ItemDto) itemDto[0]).getDescription(), itemDto2.getDescription());

        items = itemService.searchItems("описание", 0, 2);
        assertThat(items.size(), equalTo(2));
    }

    @Test
    void testFindByRequestId() {
        item.setRequestId(1);
        itemDto = itemService.createItem(itemMapper.toItemDto(item), user.getId());
        List<ItemDto> items = itemService.findByRequestId(1);

        assertThat(items.size(), equalTo(1));
        Assertions.assertTrue(items.contains(itemDto));


    }


}
