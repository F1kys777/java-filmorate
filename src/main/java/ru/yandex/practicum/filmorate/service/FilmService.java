package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;


import java.util.*;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final Validator validator;
    private final UserStorage userStorage;
    private final MpaRatingDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    public FilmService(FilmStorage filmStorage, Validator validator, UserStorage userStorage,
                       MpaRatingDbStorage mpaStorage, GenreDbStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.validator = validator;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Collection<FilmDto> findAll() {
        log.info("Получение списка всех фильмов");
        return FilmMapper.mapToListFilmDto(filmStorage.getAllFilms());
    }

    public FilmDto getFilmById(long filmId) {
        log.info("Получение фильма с id {} успешно получен", filmId);
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto create(NewFilmRequest request) {
        validator.filmValidation(request.getName(), request.getDescription(),
                request.getReleaseDate(), request.getDuration());

        Film film = FilmMapper.mapToFilm(request);

        if (request.getMpa() == null || request.getMpa().getId() == null) {
            throw new ValidationException("Рейтинг MPA должен быть указан");
        }
        MpaRating mpa = mpaStorage.findById(request.getMpa().getId().intValue())
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA не найден"));

        Set<Genre> genres = new LinkedHashSet<>();
        if (request.getGenres() != null) {
            for (Genre genre : request.getGenres()) {
                Genre full = genreStorage.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id=" + genre.getId() + " не найден"));
                genres.add(full);
            }
        }

        film.setMpaRating(mpa);
        film.setGenres(genres);

        Film saved = filmStorage.addFilm(film);
        log.info("Фильм {} с id {} успешно создан", film, film.getId());
        return FilmMapper.mapToFilmDto(saved);
    }

    public FilmDto update(long filmId, UpdateFilmRequest request) {
        log.debug("Запрос на изменение данных фильма: id {} , name {}", filmId, request.getName());

        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));

        FilmMapper.updateFilmFields(film, request);

        if (request.getMpa() != null) {
            film.setMpaRating(request.getMpa());
        }
        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            film.setGenres(request.getGenres());
        }

        Film updated = filmStorage.updateFilm(filmId, film);
        return FilmMapper.mapToFilmDto(updated);
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
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        log.info("Пользователя с id {} удалил лайк с фильма {}", userId, filmId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<FilmDto> getPopularFilms(int count) {
        validator.filmCountValidation(count);
        log.info("Получение списка из {} популярных фильмов", count);
        List<Film> films = filmStorage.getPopularFilms(count);
        return FilmMapper.mapToListFilmDto(films);
    }

    public List<FilmDto> getCommonFriendsFilms(long userId, long friendId) {
        log.debug("Получение списка общих фильмов {} и {}", userId, friendId);
        List<Film> films = filmStorage.getCommonFriendsFilms(userId,friendId);
        return FilmMapper.mapToListFilmDto(films);
    }
}
