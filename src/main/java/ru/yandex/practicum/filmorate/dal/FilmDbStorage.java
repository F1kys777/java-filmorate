package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES_QUERY = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String SELECT_GENRES_QUERY = "SELECT g.id, g.name FROM genres g JOIN film_genre fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String SELECT_LIKES_QUERY = "SELECT user_id FROM film_likes WHERE film_id = ?";
    private static final String SELECT_POPULAR_QUERY = "SELECT f.*, COUNT(l.user_id) AS like_count FROM films f LEFT JOIN film_likes l ON f.id = l.film_id GROUP BY f.id ORDER BY like_count DESC LIMIT ?";
    private static final String SELECT_MPA_QUERY = "SELECT mpa_rating_id FROM films WHERE id = ?";

    private final GenreRowMapper genreRowMapper;
    private final MpaRatingDbStorage mpaStorage;


    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper, GenreRowMapper genreRowMapper,
                         MpaRatingDbStorage mpaStorage) {
        super(jdbc, mapper);
        this.genreRowMapper = genreRowMapper;
        this.mpaStorage = mpaStorage;
    }

    public Film addFilm(Film film) {

        Long mpaRatingId;
        if (film.getMpaRating() != null) {
            mpaRatingId = film.getMpaRating().getId();
        } else {
            mpaRatingId = null;
        }
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaRatingId
        );
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                jdbc.update(INSERT_GENRE_QUERY, id, genre.getId());
            }
        }
        return film;
    }

    public Film updateFilm(long filmId, Film updatedFilm) {

        Long mpaRatingId;
        if (updatedFilm.getMpaRating() != null) {
            mpaRatingId = updatedFilm.getMpaRating().getId();
        } else {
            mpaRatingId = null;
        }

        update(
                UPDATE_QUERY,
                updatedFilm.getName(),
                updatedFilm.getDescription(),
                updatedFilm.getReleaseDate(),
                updatedFilm.getDuration(),
                mpaRatingId,
                updatedFilm.getId()
        );

        jdbc.update(DELETE_GENRES_QUERY, filmId);
        if (updatedFilm.getGenres() != null && !updatedFilm.getGenres().isEmpty()) {
            for (Genre genre : updatedFilm.getGenres()) {
                jdbc.update(INSERT_GENRE_QUERY, filmId, genre.getId());
            }
        }
        return updatedFilm;
    }


    public Film deleteFilm(Film film) {
        update(
                DELETE_QUERY,
                film.getId()
        );
        return film;
    }

    @Override
    public Optional<Film> getFilmById(long filmId) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID_QUERY, filmId);
        filmOpt.ifPresent(film -> {
            Integer mpaId = jdbc.queryForObject(
                    SELECT_MPA_QUERY, Integer.class, filmId);
            if (mpaId != null) {
                mpaStorage.findById(mpaId).ifPresent(film::setMpaRating);
            }
            List<Genre> genres = jdbc.query(SELECT_GENRES_QUERY, genreRowMapper, filmId);
            film.setGenres(new LinkedHashSet<>(genres));
            List<Long> likes = jdbc.queryForList(SELECT_LIKES_QUERY, Long.class, filmId);
            film.setLikes(new HashSet<>(likes));
        });
        return filmOpt;
    }

    @Override
    public Collection<Film> getAllFilms() {
        List<Film> films = findMany(FIND_ALL_QUERY);
        for (Film film : films) {
            Integer mpaId = jdbc.queryForObject(SELECT_MPA_QUERY, Integer.class, film.getId());
            if (mpaId != null) {
                mpaStorage.findById(mpaId).ifPresent(film::setMpaRating);
            }
            List<Genre> genres = jdbc.query(SELECT_GENRES_QUERY, genreRowMapper, film.getId());
            film.setGenres(new LinkedHashSet<>(genres));
            List<Long> likes = jdbc.queryForList(SELECT_LIKES_QUERY, Long.class, film.getId());
            film.setLikes(new HashSet<>(likes));
        }
        return films;
    }

    public void addLike(long filmId, long userId) {
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_LIKE_QUERY,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, filmId);
            ps.setLong(2, userId);
            return ps;
        });
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        List<Film> films = jdbc.query(SELECT_POPULAR_QUERY, mapper, count);
        for (Film film : films) {
            Integer mpaId = jdbc.queryForObject(SELECT_MPA_QUERY, Integer.class, film.getId());
            if (mpaId != null) {
                mpaStorage.findById(mpaId).ifPresent(film::setMpaRating);
            }
            List<Genre> genres = jdbc.query(SELECT_GENRES_QUERY, genreRowMapper, film.getId());
            film.setGenres(new LinkedHashSet<>(genres));
            List<Long> likes = jdbc.queryForList(SELECT_LIKES_QUERY, Long.class, film.getId());
            film.setLikes(new HashSet<>(likes));
        }
        return films;
    }
}
