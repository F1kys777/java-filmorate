package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements  FilmStorage{
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public void addFilm(Film film) {
        long filmId = getNextId();
        film.setId(filmId);
        log.debug("Добавление Film {}, установлен id {}", film, filmId);
        films.put(filmId, film);
    }

    @Override
    public Film getFilmById(long filmId) {
        log.debug("Получение Film с id {}", filmId);
        return films.get(filmId);
    }

    @Override
    public boolean containsFilm(long filmId) {
        log.debug("Проверка существования Film с id {}", filmId);
        return films.containsKey(filmId);
    }

    @Override
    public void updateFilm(long filmId, Film updatedFilm) {
        log.debug("Обновление Film с id {}, новые данные {}", filmId, updatedFilm);
        films.put(filmId, updatedFilm);
    }

    @Override
    public Collection<Film> getAllFilms() {
        log.debug("Передача всех фильмов");
        return films.values();
    }

    @Override
    public void deleteFilm(long filmId) {
        log.debug("Удаление Film с id {}", filmId);
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
