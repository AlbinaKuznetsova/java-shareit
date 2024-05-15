package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.exceptions.EmailExistsException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Collection;

//Старая версия репозитория
@Slf4j
public class UserRepositoryOld {
    private final HashMap<Integer, User> users = new HashMap<>();
    private final HashMap<String, Integer> emails = new HashMap<>();
    private static Integer count = 0;

    public User createUser(User user) {
        if (emails.get(user.getEmail()) != null) {
            log.info("Email {} уже существует", user.getEmail());
            throw new EmailExistsException("Email уже существует");
        }
        validateEmail(user.getEmail());
        if (user.getId() == null) {
            user.setId(generateId());
        }
        users.put(user.getId(), user);
        emails.put(user.getEmail(), user.getId());
        log.info("Добавлен пользователь {}", user);
        return user;
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }

    public User updateUser(Integer id, User user) {
        User userFromMap = null;
        if (users.containsKey(id)) {
            userFromMap = users.get(id);
            if (user.getEmail() != null) {
                if (emails.get(user.getEmail()) != null) {
                    if (!emails.get(user.getEmail()).equals(id)) {
                        log.info("Недопустимый email {}", user.getEmail());
                        throw new EmailExistsException("Недопустимый email");
                    }
                }
                emails.remove(users.get(id).getEmail());
                emails.put(user.getEmail(), user.getId());
                userFromMap.setEmail(user.getEmail());
            }
            if (user.getName() != null) {
                userFromMap.setName(user.getName());
            }
            users.put(id, userFromMap);
        } else {
            log.info("Пользователь с id {} не найден.", id);
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        log.info("Обновлен пользователь {}", user);
        return userFromMap;
    }

    public User getUserById(Integer id) {
        if (users.get(id) == null) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        return users.get(id);
    }

    public void deleteUser(Integer id) {
        if (users.get(id) == null) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        emails.remove(users.get(id).getEmail());
        users.remove(id);
        log.info("Удален пользователь {}", id);
    }

    private Integer generateId() {
        return ++count;
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
