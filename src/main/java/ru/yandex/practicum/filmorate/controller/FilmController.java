package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
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
    public Collection<Film> findAll() {
        log.debug("Запрос на получение всех фильмов");
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.debug("Создание фильма {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilmData) {
        log.debug("Попытка изменение фильма {}", newFilmData);
        return filmService.update(newFilmData);
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable long id) {
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
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.debug("Запрос на получение списка популярных фильмов со значение count {}", count);
        return filmService.getPopularFilms(count);
    }
}
