package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.dal.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dal.FeedEventsDb;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private static final Set<String> ALLOWED_SORT_BY = Set.of("year", "likes");
    private static final Set<String> ALLOWED_SEARCH_BY = Set.of("title", "director");

    private final FilmStorage filmStorage;
    private final Validator validator;
    private final UserStorage userStorage;
    private final MpaRatingDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;
    private final DirectorDbStorage directorStorage;
    private final UserDbStorage userDbStorage;
    private final FeedEventsDb feedEventsDb;
    private final FilmDbStorage filmDbStorage;

    public FilmService(FilmStorage filmStorage, Validator validator, UserStorage userStorage,
                       MpaRatingDbStorage mpaStorage, GenreDbStorage genreStorage,
                       DirectorDbStorage directorStorage,UserDbStorage userDbStorage, FeedEventsDb feedEventsDb,FilmDbStorage filmDbStorage) {
        this.filmStorage = filmStorage;
        this.validator = validator;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
        this.userDbStorage = userDbStorage;
        this.feedEventsDb = feedEventsDb;
        this.filmDbStorage = filmDbStorage;
    }

    public Collection<FilmDto> findAll() {
        log.info("Получение списка всех фильмов");
        return FilmMapper.mapToListFilmDto(filmStorage.getAllFilms());
    }

    /*public Collection<FilmDto> getPopularGenreAndYear(Long count, Long genre, Long year) {
        log.info("Получение популярных фильмов по годам и жанру");
        return filmDbStorage.getPopularGenreAndYear(count, genre, year).stream().map(FilmMapper::mapToFilmDto).toList();
    }*/

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
        film.setDirectors(resolveDirectors(request.getDirectors()));

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
        if (request.getGenres() != null) {
            film.setGenres(request.getGenres());
        }
        if (request.getDirectors() != null) {
            film.setDirectors(resolveDirectors(request.getDirectors()));
        } else {
            film.setDirectors(new LinkedHashSet<>());
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
        feedEventsDb.insertEvents(System.currentTimeMillis(), userId, "LIKE", "ADD", filmId);
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        log.info("Пользователя с id {} удалил лайк с фильма {}", userId, filmId);
        filmStorage.removeLike(filmId, userId);
        feedEventsDb.insertEvents(System.currentTimeMillis(), userId, "LIKE", "REMOVE", filmId);
    }

    public List<FilmDto> getPopularFilms(int count, Long genreId, Long year) {
        validator.filmCountValidation(count);
        log.info("Получение списка из {} популярных фильмов", count);
        List<Film> films = filmStorage.getPopularFilms(count, genreId, year);
        return FilmMapper.mapToListFilmDto(films);
    }

    public List<FilmDto> getCommonFriendsFilms(long userId, long friendId) {
        log.debug("Получение списка общих фильмов {} и {}", userId, friendId);
        List<Film> films = filmStorage.getCommonFriendsFilms(userId,friendId);
        return FilmMapper.mapToListFilmDto(films);
    }

    public List<FilmDto> getFilmsByDirector(long directorId, String sortBy) {
        directorStorage.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + directorId + " не найден"));

        if (!ALLOWED_SORT_BY.contains(sortBy)) {
            throw new ValidationException("Параметр sortBy должен быть 'year' или 'likes'");
        }

        log.info("Получение фильмов режиссёра id={} с сортировкой {}", directorId, sortBy);
        List<Film> films = filmStorage.getFilmsByDirector(directorId, sortBy);
        return FilmMapper.mapToListFilmDto(films);
    }

    public List<FilmDto> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Параметр query не может быть пустым");
        }

        List<String> byValues = parseBy(by);
        log.info("Поиск фильмов: query='{}', by={}", query, byValues);

        List<Film> films = filmStorage.searchFilms(query, byValues);
        return FilmMapper.mapToListFilmDto(films);
    }

    private Set<Director> resolveDirectors(Set<Director> requested) {
        Set<Director> resolved = new LinkedHashSet<>();
        if (requested == null) {
            return resolved;
        }
        for (Director director : requested) {
            Director full = directorStorage.findById(director.getId())
                    .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + director.getId() + " не найден"));
            resolved.add(full);
        }
        return resolved;
    }

    private List<String> parseBy(String by) {
        if (by == null || by.isBlank()) {
            return List.of("title");
        }

        List<String> values = Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());

        if (values.isEmpty()) {
            return List.of("title");
        }

        for (String value : values) {
            if (!ALLOWED_SEARCH_BY.contains(value)) {
                throw new ValidationException("Недопустимое значение параметра by: " + value
                        + ". Допустимые значения: title, director");
            }
        }
        return values;
    }

    public void remove(long filmId) {
        log.debug("Удаление фильма с id {}", filmId);
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
        filmStorage.deleteFilm(film);
    }

    public List<FilmDto> getRecommendations(long userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        log.info("Получение рекомендаций для пользователя {}", userId);
        List<Film> films = filmStorage.getRecommendations(userId);
        return FilmMapper.mapToListFilmDto(films);
    }
}
