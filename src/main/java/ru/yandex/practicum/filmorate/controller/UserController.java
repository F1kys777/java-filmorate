package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import java.util.Collection;
import java.util.List;


@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.debug("Запрос на получение всех пользователей");
        return userService.findAll();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.debug("Создание пользователя с email {}", user.getEmail());
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User newUserData) {
        log.debug("Изменение пользователя с email {} и id {}", newUserData.getEmail(), newUserData.getId());
        return userService.update(newUserData);
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable long id) {
        log.debug("Поиск пользователя с id {}", id);
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.debug("Пользователь id {} пытается добавить в друзья пользователя с id {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable long id, @PathVariable long friendId) {
        log.debug("Пользователь id {} пытается удалить из друзей пользователя с id {}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFiends(@PathVariable long id) {
        log.debug("Запрос на получение всех друзей пользователя с id {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getFiends(@PathVariable long id, @PathVariable long otherId) {
        log.debug("Запрос на получение общих друзей пользователей с id {} и id {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
