package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        log.info("Добавлен пользователь {}", user);
        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        return userMapper.toUserDto(userRepository.findAll());
    }

    @Override
    public UserDto getUserById(int userId) {
        Optional<User> opUser = userRepository.findById(userId);
        if (opUser.isPresent()) {
            return userMapper.toUserDto(opUser.get());
        } else {
            throw new ObjectNotFoundException("Пользователь не найден");
        }

    }

    @Override
    public UserDto updateUser(int userId, UserDto userDto) {
        User userFromDb = null;
        Optional<User> opUser = userRepository.findById(userId);
        if (opUser.isPresent()) {
            userFromDb = opUser.get();
        } else {
            log.info("Пользователь с id {} не найден.", userId);
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        if (userDto.getName() != null) {
            userFromDb.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            userFromDb.setEmail(userDto.getEmail());
        }
        return userMapper.toUserDto(userRepository.save(userFromDb));
    }

    @Override
    public void deleteUser(int userId) {
        userRepository.deleteById(userId);
    }
}
