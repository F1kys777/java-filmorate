package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private final Validator validator = new Validator();

    @GetMapping
    public Collection<User> findAll() {
        log.debug("Запрос на получение всех пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.debug("Запрос на создание пользователя: {}", user);
        validator.userValidation(user.getEmail(), user.getLogin(), user.getBirthday());
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя установлено равным логину: {}", user.getLogin());
        }
        boolean emailExists = users.values().stream()
                .anyMatch(existing -> existing.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExists) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        users.put(user.getId(), user);
        log.info("Пользователь с id {}, email {} успешно создан", user.getId(), user.getEmail());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUserData) {
        log.debug("Запрос на обновление пользователя: {}", newUserData);
        if (!(newUserData.getId() == null || users.containsKey(newUserData.getId()))) {
            User oldUserData = users.get(newUserData.getId());
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

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
