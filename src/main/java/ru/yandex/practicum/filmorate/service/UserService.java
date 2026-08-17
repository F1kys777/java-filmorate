package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    UserStorage userStorage;
    private final Validator validator;

    public UserService(UserStorage userStorage, Validator validator) {
        this.userStorage = userStorage;
        this.validator = validator;
    }

    public Collection<User> findAll() {
        return userStorage.getAllUsers();
    }

    public User getUserById(long userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return userStorage.getUserById(userId);
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя установлено равным логину: {}", user.getLogin());
        }
        boolean emailExists = userStorage.getAllUsers().stream()
                .anyMatch(existing -> existing.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExists) {
            throw new ValidationException("Этот имейл уже используется");
        }
        userStorage.addUser(user);
        log.info("Пользователь с id {}, email {} успешно создан", user.getId(), user.getEmail());
        return user;
    }

    public User update(User newUserData) {
        log.debug("Запрос на обновление пользователя: {}", newUserData);
        if ((!(newUserData.getEmail() == null)) || userStorage.containsUser(newUserData.getId())) {
            User oldUserData = userStorage.getUserById(newUserData.getId());
            log.debug("Найден существующий пользователь с id {}", oldUserData.getId());
            if (!(newUserData.getEmail() == null)) {
                validator.emailCheck(newUserData.getEmail());
                oldUserData.setEmail(newUserData.getEmail());
                log.debug("Email пользователя {} обновлён на {}", oldUserData.getId(), newUserData.getEmail());
            }

            if (!(newUserData.getName() == null)) {
                oldUserData.setName(newUserData.getName());
                log.debug("Имя {} обновлено на {}", oldUserData.getId(), newUserData.getName());
            }

            if (!(newUserData.getLogin() == null)) {
                validator.loginCheck(newUserData.getLogin());
                oldUserData.setLogin(newUserData.getLogin());
                log.debug("Логин {} обновлён на {}", oldUserData.getId(), newUserData.getLogin());
            }

            if (!(newUserData.getBirthday() == null)) {
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
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Set<Long> getFriends(long userId) {
        User user = userStorage.getUserById(userId);
        return user.getFriends();
    }

    public Set<Long> getCommonFriends(long userId, long otherId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(otherId);
        Set<Long> mutualFriends = new HashSet<>();
        for (Long id : user.getFriends()) {
            if (friend.getFriends().contains(id)) {
                mutualFriends.add(id);
            }
        }
        return mutualFriends;
    }
}
