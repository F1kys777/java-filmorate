package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    void addFilm(Film film);

    void updateFilm(long filmId, Film updatedFilm);

    void deleteFilm(long filmId);

    Optional<Film> getFilmById(long filmId);

    Collection<Film> getAllFilms();

    boolean containsFilm(long filmId);
}
