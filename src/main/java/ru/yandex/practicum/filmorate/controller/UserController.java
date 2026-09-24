package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FeedEventsDto;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import java.util.Collection;
import java.util.List;


@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;
    private final FilmService filmService;

    public UserController(UserService userService, FilmService filmService) {
        this.userService = userService;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<UserDto> findAll() {
        log.debug("Запрос на получение всех пользователей");
        return userService.findAll();
    }

    @PostMapping
    public UserDto create(@RequestBody NewUserRequest userRequest) {
        log.debug("Создание пользователя с email {}", userRequest.getEmail());
        return userService.create(userRequest);
    }

    @PutMapping("/{userId}")
    public UserDto update(@PathVariable("userId") long userId, @RequestBody UpdateUserRequest request) {
        log.debug("Изменение пользователя с email {} и id {}", request.getEmail(), userId);
        return userService.update(userId, request);
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable long id) {
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
    public List<UserDto> getFriends(@PathVariable long id) {
        log.debug("Запрос на получение всех друзей пользователя с id {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/feed")
    public Collection<FeedEventsDto> getFeeds(@PathVariable long id) {
        log.debug("Запрос на получение ленты событий всех друзей пользователя с id {}", id);
        return userService.getFeedsFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        log.debug("Запрос на получение общих друзей пользователей с id {} и id {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @PutMapping
    public UserDto update(@RequestBody UpdateUserRequest request) {
        if (!request.hasId()) {
            throw new ValidationException("Id должен быть указан");
        }
        return userService.update(request.getId(), request);
    }

    @DeleteMapping("/{userId}")
    public void remove(@PathVariable long userId) {
        log.debug("Запрос на удаление пользователя с id {}", userId);
        userService.remove(userId);
    }

    @GetMapping("/{id}/recommendations")
    public List<FilmDto> getRecommendations(@PathVariable long id) {
        log.debug("Запрос рекомендаций для пользователя {}", id);
        return filmService.getRecommendations(id);
    }
}
