package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIntegrationTest {
    private final UserService userService;
    private final EntityManager em;

    @Test
    void testCreateUser() {
        User user = new User();
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        userService.createUser(user);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User userFromDB = query.setParameter("email", user.getEmail()).getSingleResult();
        assertThat(userFromDB.getId(), notNullValue());
        assertThat(userFromDB.getEmail(), equalTo(user.getEmail()));
        assertThat(userFromDB.getName(), equalTo(user.getName()));

    }

    @Test
    void testGetUserById() {
        User user = new User();
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        User userFromDB1 = userService.createUser(user);
        User userFromDB2 = userService.getUserById(userFromDB1.getId());

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User userFromDB = query.setParameter("id", userFromDB1.getId()).getSingleResult();
        assertThat(userFromDB.getId(), equalTo(userFromDB2.getId()));
        assertThat(userFromDB.getEmail(), equalTo(userFromDB2.getEmail()));
        assertThat(userFromDB.getName(), equalTo(userFromDB2.getName()));
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        User userFromDB1 = userService.createUser(user);
        User user2 = new User();
        user2.setName("www");
        user2.setEmail("www@yandex.ru");
        userService.updateUser(user.getId(), user2);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User userFromDB = query.setParameter("id", userFromDB1.getId()).getSingleResult();
        assertThat(userFromDB.getId(), equalTo(userFromDB1.getId()));
        assertThat(userFromDB.getEmail(), equalTo(user2.getEmail()));
        assertThat(userFromDB.getName(), equalTo(user2.getName()));
    }

    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        userService.createUser(user);
        User user2 = new User();
        user2.setName("www");
        user2.setEmail("www@yandex.ru");
        userService.createUser(user2);

        Collection<User> users = userService.getAllUsers();
        Assertions.assertEquals(2, users.size());
        Assertions.assertTrue(users.contains(user));
        Assertions.assertTrue(users.contains(user2));
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        userService.createUser(user);

        Collection<User> users = userService.getAllUsers();
        Assertions.assertEquals(1, users.size());

        userService.deleteUser(user.getId());
        users = userService.getAllUsers();
        Assertions.assertEquals(0, users.size());
    }
}
