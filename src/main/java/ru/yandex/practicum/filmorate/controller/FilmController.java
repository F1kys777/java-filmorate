package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) { // ВАЛИДАЦИЯ ОТСТУТСТВУЕТ - ПРОСТО ДОБАВЛЯЕТ
        film.setId(getNextId());

        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilmData) {
        // проверяем необходимые условия
        if (newFilmData.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilmData.getId())) {
            Film oldFilmData = films.get(newFilmData.getId());

            if (!(newFilmData.getDescription() == null)) {
                if (oldFilmData.getDescription().equals(newFilmData.getDescription())) {
                    throw new DuplicatedDataException("Новое описание фильма совпадает со старым");
                }
                oldFilmData.setDescription(newFilmData.getDescription());
            }

            if (!(newFilmData.getName() == null)) {
                if (oldFilmData.getName().equals(newFilmData.getName())) {
                    throw new DuplicatedDataException("Новое название фильма совпадает со старым");
                }
                oldFilmData.setName(newFilmData.getName());
            }

            if (!(newFilmData.getReleaseDate() == null)) {
                if (oldFilmData.getReleaseDate().equals(newFilmData.getReleaseDate())) {
                    throw new DuplicatedDataException("Новый Login совпадает со старым");
                }
                oldFilmData.setReleaseDate(newFilmData.getReleaseDate());
            }

            if (!(newFilmData.getDuration() == null)) {
                if (oldFilmData.getDuration().equals(newFilmData.getDuration())) {
                    throw new DuplicatedDataException("Новый дата рождения совпадает с уже установленной!");
                }
                oldFilmData.setDuration(newFilmData.getDuration());
            }
            return oldFilmData;
        }
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
