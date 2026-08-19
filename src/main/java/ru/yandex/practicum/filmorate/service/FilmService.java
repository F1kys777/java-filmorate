package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;


import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final Validator validator;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, Validator validator, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.validator = validator;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        log.info("Получение списка всех фильмов");
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(long filmId) {
        log.info("Получение фильма с id {} успешно получен", filmId);
        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
    }

    public Film create(Film film) {
        validator.filmValidation(film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        filmStorage.addFilm(film);
        log.info("Фильм {} с id {} успешно создан", film, film.getId());
        return film;
    }

    public Film update(Film newFilmData) {
        log.debug("Запрос на изменение данных фильма: {}", newFilmData);
        if (newFilmData.getId() == null) {
            log.warn("Попытка обновления фильма без указания id");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (filmStorage.containsFilm(newFilmData.getId())) {
            Film oldFilmData = filmStorage.getFilmById(newFilmData.getId())
                    .orElseThrow(() -> new NotFoundException("Фильм с id=" + newFilmData.getId() + " не найден"));
            log.debug("Найден существующий фильм с id {}", oldFilmData.getId());

            if (newFilmData.getDescription() != null) {
                validator.descriptionLength(newFilmData.getDescription());
                oldFilmData.setDescription(newFilmData.getDescription());
                log.debug("Описание фильма {} обновлено", oldFilmData.getId());
            }

            if (newFilmData.getName() != null) {
                oldFilmData.setName(newFilmData.getName());
                log.debug("Название фильма {} обновлено", oldFilmData.getId());
            }

            if (newFilmData.getReleaseDate() != null) {
                validator.filmDate(newFilmData.getReleaseDate());
                oldFilmData.setReleaseDate(newFilmData.getReleaseDate());
                log.debug("Дата выхода фильма {} обновлена", oldFilmData.getId());
            }

            if (newFilmData.getDuration() != null) {
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
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже ставил лайк этому фильму");
        }
        log.info("Фильм с id {} получил лайк от пользователя {}", film, userId);
        film.getLikes().add(userId);
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        log.info("Пользователя с id {} удалил лайк с фильма {}", userId, filmId);
        film.getLikes().remove(userId);
    }

    public List<Film> getPopularFilms(int count) {
        validator.filmCountValidation(count);
        log.info("Получение списка из {} популярных фильмов", count);
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
