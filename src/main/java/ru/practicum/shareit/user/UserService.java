package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    UserDto createUser(UserDto userDto);

    Collection<UserDto> getAllUsers();

    UserDto getUserById(int userId);

    UserDto updateUser(int userId, UserDto userDto);

    void deleteUser(int userId);
}
