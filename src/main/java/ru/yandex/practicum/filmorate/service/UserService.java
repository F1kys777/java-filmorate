package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.FriendshipStatus;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final Validator validator;

    public UserService(UserStorage userStorage, Validator validator) {
        this.userStorage = userStorage;
        this.validator = validator;
    }

    public Collection<UserDto> findAll() {
        log.info("Получение списка всех пользователей");
        return UserMapper.mapToListUserDto(userStorage.getAllUsers());
    }

    public UserDto getUserById(long userId) {
        log.info("Поиск пользователя с id {} ", userId);
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        return UserMapper.mapToUserDto(user);
    }

    public UserDto create(NewUserRequest request) {
        validator.userValidation(request.getEmail(), request.getLogin(), request.getBirthday());
        if (request.getName() == null || request.getName().isBlank()) {
            request.setName(request.getLogin());
            log.debug("Имя пользователя установлено равным логину: {}", request.getLogin());
        }
        validator.emailExists(request.getEmail(), userStorage);
        User user = UserMapper.mapToUser(request);

        user = userStorage.addUser(user);

        log.info("Пользователь с id {}, email {} успешно создан", user.getId(), request.getEmail());
        return UserMapper.mapToUserDto(user);
    }

    public UserDto update(Long userId, UpdateUserRequest request) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        String oldEmail = user.getEmail();

        UserMapper.updateUserFields(user, request);

        if (request.hasEmail() && !user.getEmail().equals(oldEmail)) {
            validator.emailExists(user.getEmail(), userStorage);
        }

        User updatedUser = userStorage.updateUser(user);
        return UserMapper.mapToUserDto(updatedUser);
    }

    public void addFriend(long userId, long friendId) {
        getUserById(userId);
        getUserById(friendId);
        userStorage.addFriend(userId, friendId, FriendshipStatus.CONFIRMED);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        getUserById(userId);
        getUserById(friendId);
        log.debug("Удаление из друзей: userId={}, friendId={}", userId, friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public List<UserDto> getFriends(long userId) {
        getUserById(userId);
        List<User> friends = userStorage.getFriends(userId);
        return UserMapper.mapToListUserDto(friends);
    }

    public List<UserDto> getCommonFriends(long userId, long otherId) {
        getUserById(userId);
        getUserById(otherId);
        List<User> mutualFriends = userStorage.getCommonFriends(userId,otherId);

        log.info("Получение общих друзей пользователей userId={} и {}", userId, otherId);
        return UserMapper.mapToListUserDto(mutualFriends);
    }
}