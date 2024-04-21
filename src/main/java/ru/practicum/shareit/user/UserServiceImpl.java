package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        return userRepository.createUser(user);
    }

    @Override
    public Collection<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public User getUserById(int userId) {
        return userRepository.getUserById(userId);
    }

    @Override
    public User updateUser(int userId, User user) {
        return userRepository.updateUser(userId, user);
    }

    @Override
    public void deleteUser(int userId) {
        userRepository.deleteUser(userId);
    }
}
