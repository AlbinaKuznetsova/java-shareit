package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        validateEmail(user.getEmail());
        log.info("Добавлен пользователь {}", user);
        return userRepository.save(user);
    }

    @Override
    public Collection<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(int userId) {
        Optional<User> opUser = userRepository.findById(userId);
        if (opUser.isPresent()) {
            return opUser.get();
        } else {
            throw new ObjectNotFoundException("Пользователь не найден");
        }

    }

    @Override
    public User updateUser(int userId, User user) {
        User userFromDb = null;
        Optional<User> opUser = userRepository.findById(userId);
        if (opUser.isPresent()) {
            userFromDb = opUser.get();
        } else {
            log.info("Пользователь с id {} не найден.", userId);
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        if (user.getName() != null) {
            userFromDb.setName(user.getName());
        }
        if (user.getEmail() != null) {
            userFromDb.setEmail(user.getEmail());
        }
        return userRepository.save(userFromDb);
    }

    @Override
    public void deleteUser(int userId) {
        userRepository.deleteById(userId);
    }

    private void validateEmail(String email) throws ValidationException {
        if (email == null) {
            throw new ValidationException("Пустой email");
        } else if (email.isBlank() || !email.contains("@")) {
            log.info("Неправильный email\n" + email);
            throw new ValidationException("Неправильный email");
        }
    }
}
