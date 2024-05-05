package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    User createUser(User user);

    Collection<User> getAllUsers();

    User getUserById(int userId);

    User updateUser(int userId, User user);

    void deleteUser(int userId);
}
