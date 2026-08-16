package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements  FilmStorage{
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public void addFilm(Film film) {
        long filmId = getNextId();
        films.put(filmId, film);
    }

    @Override
    public Film getFilmById(long filmId) {
        return films.get(filmId);
    }

    @Override
    public boolean containsFilm(long filmId) {
        return films.containsKey(filmId);
    }

    @Override
    public void updateFilm(long filmId, Film updatedFilm) {
        films.put(filmId, updatedFilm);
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public void deleteFilm(long filmId) {
        films.remove(filmId);
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
