package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;


import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    FilmStorage filmStorage;
    private final Validator validator;

    public FilmService(FilmStorage filmStorage, Validator validator) {
        this.filmStorage = filmStorage;
        this.validator = validator;
    }

    public Collection<Film> findAll() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(long filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        return filmStorage.getFilmById(filmId);
    }

    public Film create(Film film) {
        filmStorage.addFilm(film);
        return film;
    }

    public Film update(Film newFilmData) {
        log.debug("Запрос на изменение данных фильма: {}", newFilmData);
        if (newFilmData.getId() == null) {
            log.warn("Попытка обновления фильма без указания id");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (filmStorage.containsFilm(newFilmData.getId())) {
            Film oldFilmData = filmStorage.getFilmById(newFilmData.getId());
            log.debug("Найден существующий фильм с id {}", oldFilmData.getId());

            if (!(newFilmData.getDescription() == null)) {
                validator.descriptionLength(newFilmData.getDescription());
                oldFilmData.setDescription(newFilmData.getDescription());
                log.debug("Описание фильма {} обновлено", oldFilmData.getId());
            }

            if (!(newFilmData.getName() == null)) {
                oldFilmData.setName(newFilmData.getName());
                log.debug("Название фильма {} обновлено", oldFilmData.getId());
            }

            if (!(newFilmData.getReleaseDate() == null)) {
                validator.filmDate(newFilmData.getReleaseDate());
                oldFilmData.setReleaseDate(newFilmData.getReleaseDate());
                log.debug("Дата выхода фильма {} обновлена", oldFilmData.getId());
            }

            if (!(newFilmData.getDuration() == null)) {
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

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);
        film.getLikes().add(userId); // ДОБАВИТЬ ПРОВЕРКУ И ВАЛИДАЦИЮ ЧТО ФИЛЬМА ЧТО ПОЛЬЗОВАТЕЛЯ
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);
        film.getLikes().remove(userId); // ДОБАВИТЬ ПРОВЕРКУ И ВАЛИДАЦИЮ ЧТО ФИЛЬМА ЧТО ПОЛЬЗОВАТЕЛЯ
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
