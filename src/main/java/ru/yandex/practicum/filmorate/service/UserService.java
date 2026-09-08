package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

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
        return UserMapper.mapToUserDto(userStorage.getAllUsers());
    }

    public UserDto getUserById(long userId) {
        log.info("Поиск пользователя с id {} ", userId);
        return UserMapper.mapToUserDto(userStorage.getUserById(userId))
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    public UserDto create(NewUserRequest request) {
        validator.userValidation(request.getEmail(), request.getLogin(), request.getBirthday());
        if (request.getName() == null || request.getName().isBlank()) {
            request.setName(request.getLogin());
            log.debug("Имя пользователя установлено равным логину: {}", request.getLogin());
        }
        validator.emailExists(userStorage, request);
        User user = UserMapper.mapToUser(request);

        user = userStorage.addUser(user);

        log.info("Пользователь с id {}, email {} успешно создан", user.getId(), request.getEmail());
        return UserMapper.mapToUserDto(user);
    }

    public UserDto update(Long userId, UpdateUserRequest request) {
        User updatedUser = userStorage.getUserById(userId)
                .map(user -> UserMapper.updateUserFields(user, request))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        validator.emailExists(request.getEmail());
        updatedUser = userStorage.updateUser(updatedUser);
        return UserMapper.mapToUserDto(updatedUser);
        /*log.debug("Запрос на обновление пользователя c id: {}", userId);
        if (userId == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        boolean exists = userStorage.getUserById(userId).isPresent();
        if (exists) {
            UserDto oldUserData = userStorage.getUserById(userId)
                    .orElseThrow(() -> new NotFoundException("Пользователь id=" + userId + " не найден"));
            log.debug("Найден существующий пользователь с id {}", oldUserData.getId());
            if (request.getEmail() != null) {
                if (!(request.getEmail().equals(oldUserData.getEmail()))) {
                    validator.emailExists(userStorage, request);
                }
                validator.emailCheck(request.getEmail());
                oldUserData.setEmail(request.getEmail());
                log.debug("Email пользователя {} обновлён на {}", oldUserData.getId(), request.getEmail());
            }

            if (request.getName() != null) {
                oldUserData.setName(request.getName());
                log.debug("Имя {} обновлено на {}", oldUserData.getId(), request.getName());
            }

            if (request.getLogin() != null) {
                validator.loginCheck(request.getLogin());
                oldUserData.setLogin(request.getLogin());
                log.debug("Логин {} обновлён на {}", oldUserData.getId(), request.getLogin());
            }

            if (request.getBirthday() != null) {
                validator.birthDayCheck(request.getBirthday());
                oldUserData.setBirthday(request.getBirthday());
                log.debug("Дата рождения {} обновлена на {}", oldUserData.getId(), request.getBirthday());
            }
            log.info("id {} успешно изменен", oldUserData.getId());
            return userStorage.updateUser(UserMapper.(oldUserData));
        }
        log.warn("Попытка обновления несуществующего id {}", userId);
        throw new NotFoundException("Пользователь с id = " + userId + " не найден");
    */}

    public void addFriend(long userId, long friendId) {
        log.debug("Добавление в друзья: userId={}, friendId={}", userId, friendId);
        userStorage.addFriend(userId,friendId);
    }

    public void removeFriend(long userId, long friendId) {
        log.debug("Удаление из друзей: userId={}, friendId={}", userId, friendId);
        userStorage.removeFriend(userId,friendId);
    }

    public List<User> getFriends(long userId) {
        List<User> friends = userStorage.getFriends(userId);
        return friends;
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        List<User> mutualFriends = userStorage.getCommonFriends(userId,otherId);

        log.info("Получение общих друзей пользователей userId={} и {}", userId, otherId);
        return mutualFriends;
    }
}