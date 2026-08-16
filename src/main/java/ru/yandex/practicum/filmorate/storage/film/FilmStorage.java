package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    void addFilm(Film film);
    void updateFilm(long filmId, Film updatedFilm);
    void deleteFilm(long filmId);
    Film getFilmById(long filmId);
    Collection<Film> getAllFilms();
    boolean containsFilm(long filmId);
}
