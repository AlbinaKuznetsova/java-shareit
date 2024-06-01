package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserRepository userRepository;

    UserMapper userMapper;
    User user;
    User user2;
    UserService userService;

    @BeforeEach
    void beforeEach() {
        userMapper = new UserMapperImpl();
        userService = new UserServiceImpl(userRepository, userMapper);
        user = new User();
        user.setId(1);
        user.setName("тестовый пользователь");
        user.setEmail("test@yandex.ru");
        user2 = new User();
        user2.setName("www");
        user2.setEmail("www@yandex.ru");
    }

    @Test
    void testCreateUser() {
        Mockito.when(userRepository.save(any())).thenReturn(user);
        UserDto user3 = userService.createUser(userMapper.toUserDto(user));
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
        Assertions.assertEquals(userMapper.toUserDto(user), user3);

        user2.setEmail(null);
        Assertions.assertThrows(ValidationException.class, () -> userService.createUser(userMapper.toUserDto(user2)));
        user2.setEmail("test");
        Assertions.assertThrows(ValidationException.class, () -> userService.createUser(userMapper.toUserDto(user2)));
    }

    @Test
    void testGetUserById() {
        Mockito.when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        UserDto user3 = userService.getUserById(user.getId());
        Mockito.verify(userRepository, Mockito.times(1)).findById(user.getId());
        Assertions.assertEquals(userMapper.toUserDto(user), user3);

    }

    @Test
    void testUpdateUser() {
        Mockito.when(userRepository.findById(anyInt())).thenAnswer(invocationOnMock -> {
            int userId = invocationOnMock.getArgument(0, Integer.class);
            if (userId == user.getId()) {
                return Optional.of(user);
            } else {
                return Optional.empty();
            }
        });
        Mockito.when(userRepository.save(any())).thenReturn(user2);
        UserDto user3 = userService.updateUser(user.getId(), userMapper.toUserDto(user2));
        Mockito.verify(userRepository, Mockito.times(1)).findById(user.getId());
        Assertions.assertEquals(user.getName(), user3.getName());
        Assertions.assertEquals(user.getEmail(), user3.getEmail());

        Assertions.assertThrows(ObjectNotFoundException.class, () -> userService.updateUser(user.getId() + 1000, userMapper.toUserDto(user2)));

        // Обновляем только email
        user2.setName(null);
        user3 = userService.updateUser(user.getId(), userMapper.toUserDto(user2));
        Assertions.assertNotEquals(user.getName(), user3.getName());
        Assertions.assertEquals(user.getEmail(), user3.getEmail());
    }
}
