package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emails = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) { // ВАЛИДАЦИЯ ОТСТУТСТВУЕТ - ПРОСТО ДОБАВЛЯЕТ
        user.setId(getNextId());

        users.put(user.getId(), user);
        emails.put(user.getEmail().toLowerCase(), user.getId());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUserData) {
        // проверяем необходимые условия
        if (newUserData.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUserData.getId())) {
            User oldUserData = users.get(newUserData.getId());
            if (!(newUserData.getEmail() == null)) {
                if (!oldUserData.getEmail().equals(newUserData.getEmail())) {
                    if (emails.containsKey(newUserData.getEmail().toLowerCase())) {
                        throw new DuplicatedDataException("Этот имейл уже используется");
                    }
                    oldUserData.setEmail(newUserData.getEmail());
                }
            }

            if (!(newUserData.getName() == null)) {
                if (oldUserData.getName().equals(newUserData.getName())) {
                    throw new DuplicatedDataException("Новый имя пользователя совпадает со старым");
                }
                oldUserData.setName(newUserData.getName());
            }

            if (!(newUserData.getLogin() == null)) {
                if (oldUserData.getLogin().equals(newUserData.getLogin())) {
                    throw new DuplicatedDataException("Новый Login совпадает со старым");
                }
                oldUserData.setLogin(newUserData.getLogin());
            }

            if (!(newUserData.getBirthday() == null)) {
                if (oldUserData.getBirthday().equals(newUserData.getBirthday())) {
                    throw new DuplicatedDataException("Новый дата рождения совпадает с уже установленной!");
                }
                oldUserData.setBirthday(newUserData.getBirthday());
            }
            return oldUserData;
        }
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
