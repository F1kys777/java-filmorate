package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Optional;

public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, releaseDate, duration, genres, mpaRatingId)" +
            "VALUES (?, ?, ?, ?, ?, ?) returning id";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, releaseDate = ?" +
            " duration = ? genres = ? mpaRatingId = ? WHERE id = ?";

    /*
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Genre> genres;
    private Long mpaRatingId;
    */

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    public void addFilm(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getGenres(),
                film.getMpaRatingId()
        );
        film.setId(id);
        return film;
    }

    public Film updateFilm(long filmId, Film updatedFilm) {
        update(
                UPDATE_QUERY,
                updatedFilm.getName(),
                updatedFilm.getDescription(),
                updatedFilm.getReleaseDate(),
                updatedFilm.getDuration(),
                updatedFilm.getGenres(),
                updatedFilm.getMpaRatingId(),
                updatedFilm.getId()
        );
        return updatedFilm;
    }


    public Film deleteFilm(Film film) {
        update(
                DELETE_QUERY,
                film.getId()
        );
        return film;
    }

    public Optional<Film> getFilmById(long filmId) {
        return findOne(FIND_BY_ID_QUERY, filmId);
    }

    public Collection<Film> getAllFilms(){
        return findMany(FIND_ALL_QUERY);
    }
}
