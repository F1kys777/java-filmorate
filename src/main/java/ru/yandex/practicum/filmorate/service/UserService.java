package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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

    public Collection<User> findAll() {
        log.info("Получение списка всех пользователей");
        return userStorage.getAllUsers();
    }

    public User getUserById(long userId) {
        log.info("Поиск пользователя с id {} ", userId);
        return userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    public User create(User user) {
        validator.userValidation(user.getEmail(), user.getLogin(), user.getBirthday());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя установлено равным логину: {}", user.getLogin());
        }
        validator.emailExists(userStorage, user);
        userStorage.addUser(user);
        log.info("Пользователь с id {}, email {} успешно создан", user.getId(), user.getEmail());
        return user;
    }

    public User update(User newUserData) {
        log.debug("Запрос на обновление пользователя: {}", newUserData);
        if (newUserData.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (userStorage.containsUser(newUserData.getId())) {
            User oldUserData = userStorage.getUserById(newUserData.getId())
                    .orElseThrow(() -> new NotFoundException("Пользователь id=" + newUserData.getId() + " не найден"));
            log.debug("Найден существующий пользователь с id {}", oldUserData.getId());
            if (newUserData.getEmail() != null) {
                if (!(newUserData.getEmail().equals(oldUserData.getEmail()))) {
                    validator.emailExists(userStorage, newUserData);
                }
                validator.emailCheck(newUserData.getEmail());
                oldUserData.setEmail(newUserData.getEmail());
                log.debug("Email пользователя {} обновлён на {}", oldUserData.getId(), newUserData.getEmail());
            }

            if (newUserData.getName() != null) {
                oldUserData.setName(newUserData.getName());
                log.debug("Имя {} обновлено на {}", oldUserData.getId(), newUserData.getName());
            }

            if (newUserData.getLogin() != null) {
                validator.loginCheck(newUserData.getLogin());
                oldUserData.setLogin(newUserData.getLogin());
                log.debug("Логин {} обновлён на {}", oldUserData.getId(), newUserData.getLogin());
            }

            if (newUserData.getBirthday() != null) {
                validator.birthDayCheck(newUserData.getBirthday());
                oldUserData.setBirthday(newUserData.getBirthday());
                log.debug("Дата рождения {} обновлена на {}", oldUserData.getId(), newUserData.getBirthday());
            }
            log.info("id {} успешно изменен", oldUserData.getId());
            return oldUserData;
        }
        log.warn("Попытка обновления несуществующего id {}", newUserData.getId());
        throw new NotFoundException("Пользователь с id = " + newUserData.getId() + " не найден");
    }

    public void addFriend(long userId, long friendId) {
        log.debug("Добавление в друзья: userId={}, friendId={}", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(long userId, long friendId) {
        log.debug("Удаление из друзей: userId={}, friendId={}", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public List<User> getFriends(long userId) {
        User user = getUserById(userId);
        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        User user = getUserById(userId);
        User other = getUserById(otherId);

        List<User> mutualFriends = user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(this::getUserById)
                .collect(Collectors.toList());

        log.info("Получение общих друзей пользователей userId={} и {}", userId, otherId);
        return mutualFriends;
    }
}
