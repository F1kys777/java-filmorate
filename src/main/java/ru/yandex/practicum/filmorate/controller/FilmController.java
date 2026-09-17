package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<FilmDto> findAll() {
        log.debug("Запрос на получение всех фильмов");
        return filmService.findAll();
    }

    @PostMapping
    public FilmDto create(@RequestBody NewFilmRequest request) {
        log.debug("Создание фильма {}", request);
        return filmService.create(request);
    }

    @PutMapping("/{filmId}")
    public FilmDto update(@PathVariable long filmId, @RequestBody UpdateFilmRequest request) {
        log.debug("Попытка изменение фильма {}", request);
        return filmService.update(filmId, request);
    }

    @GetMapping("/{id}")
    public FilmDto findById(@PathVariable long id) {
        log.debug("Поиск фильма с id {}", id);
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.debug("Пользователь id {} пытается лайк фильму id {}", userId, id);
        filmService.addLike(id,userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        log.debug("Пользователь id {} пытается убрать лайк фильму id {}", userId, id);
        filmService.removeLike(id,userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.debug("Запрос на получение списка популярных фильмов со значение count {}", count);
        return filmService.getPopularFilms(count);
    }

    @PutMapping
    public FilmDto update(@RequestBody UpdateFilmRequest request) {
        if (request.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        return filmService.update(request.getId(), request);
    }

    @GetMapping("/common")
    public List<FilmDto> getCommonFriendsFilms(@RequestParam long userId, @RequestParam long friendId) {
        log.debug("Запрос на получение списка общих фильмов {} и {}", userId, friendId);
        return filmService.getCommonFriendsFilms(userId, friendId);
    }

    @DeleteMapping("/{filmId}")
    public void remove(@PathVariable long filmId) {
        log.debug("Запрос на удаление фильма с id {}", filmId);
        filmService.remove(filmId);
    }
}
