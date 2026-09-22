package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(long filmId, Film updatedFilm);

    Film deleteFilm(Film film);

    Optional<Film> getFilmById(long filmId);

    Collection<Film> getAllFilms();

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    List<Film> getPopularFilms(int count);

    List<Film> getCommonFriendsFilms(long id, long friendId);

    List<Film> getFilmsByDirector(long directorId, String sortBy);

    List<Film> searchFilms(String query, List<String> by);
}
