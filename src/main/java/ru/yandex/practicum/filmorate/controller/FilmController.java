package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private final Validator validator = new Validator();

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("Запрос на получение всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.debug("Запрос на создание фильма: {}", film);
        validator.filmValidation(film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        film.setId(getNextId());

        films.put(film.getId(), film);
        log.info("Фильм с id {} успешно создан", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilmData) {
        log.debug("Запрос на изменение данных фильма: {}", newFilmData);
        if (newFilmData.getId() == null) {
            log.warn("Попытка обновления фильма без указания id");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilmData.getId())) {
            Film oldFilmData = films.get(newFilmData.getId());
            log.debug("Найден существующий фильм с id {}", oldFilmData.getId());

            if (!(newFilmData.getDescription() == null)) {
                if (oldFilmData.getDescription().equals(newFilmData.getDescription())) {
                    log.warn("Новое описание фильма совпадает со старым для id {}", oldFilmData.getId());
                    throw new DuplicatedDataException("Новое описание фильма совпадает со старым");
                }
                validator.descriptionLength(newFilmData.getDescription());
                oldFilmData.setDescription(newFilmData.getDescription());
                log.debug("Описание фильма {} обновлено", oldFilmData.getId());
            }

            if (!(newFilmData.getName() == null)) {
                if (oldFilmData.getName().equals(newFilmData.getName())) {
                    log.warn("Новое название фильма совпадает со старым для id {}", oldFilmData.getId());
                    throw new DuplicatedDataException("Новое название фильма совпадает со старым");
                }
                oldFilmData.setName(newFilmData.getName());
                log.debug("Название фильма {} обновлено", oldFilmData.getId());
            }

            if (!(newFilmData.getReleaseDate() == null)) {
                if (oldFilmData.getReleaseDate().equals(newFilmData.getReleaseDate())) {
                    log.warn("Новая дата выхода совпадает со старой для id {}", oldFilmData.getId());
                    throw new DuplicatedDataException("Новая дата выхода совпадает со старой");
                }
                validator.filmDate(newFilmData.getReleaseDate());
                oldFilmData.setReleaseDate(newFilmData.getReleaseDate());
                log.debug("Дата выхода фильма {} обновлена", oldFilmData.getId());
            }

            if (!(newFilmData.getDuration() == null)) {
                if (oldFilmData.getDuration().equals(newFilmData.getDuration())) {
                    log.warn("Новая продолжительность совпадает со старой для id {}", oldFilmData.getId());
                    throw new DuplicatedDataException("Новая продолжительность совпадает с уже установленной!");
                }
                validator.positiveCheck(newFilmData.getDuration());
                oldFilmData.setDuration(newFilmData.getDuration());
                log.debug("Продолжительность фильма {} обновлена", oldFilmData.getId());
            }
            log.info("Фильм с id {} успешно обновлён", oldFilmData.getId());
            return oldFilmData;
        }
        log.warn("Попытка обновления несуществующего фильма с id {}", newFilmData.getId());
        throw new NotFoundException("Фильм с id = " + newFilmData.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
