package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.model.Genre;

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
    private static final String SELECT_MPA_QUERY = "SELECT mpa_rating_id FROM films WHERE id = ?";
    private static final String SELECT_COMMON_FRIEND_FILM_QUERY = "SELECT f.*, COUNT(l.user_id) AS like_count " +
            "FROM films f JOIN film_likes l ON f.id = l.film_id JOIN film_likes l1 ON f.id = l1.film_id " +
            "JOIN film_likes l2 ON f.id = l2.film_id WHERE l1.user_id = ? AND l2.user_id = ? GROUP BY f.id " +
            "ORDER BY like_count";
    private static final String RECOMMENDATIONS_QUERY = "SELECT f.*" +
            "        FROM films f" +
            "        JOIN film_likes fl ON f.id = fl.film_id" +
            "        JOIN (" +
            "            SELECT fl2.user_id" +
            "            FROM film_likes fl1" +
            "            JOIN film_likes fl2 ON fl1.film_id = fl2.film_id" +
            "            WHERE fl1.user_id = ? AND fl2.user_id <> ?" +
            "            GROUP BY fl2.user_id" +
            "            ORDER BY COUNT(*) DESC" +
            "            LIMIT 10" +
            "        ) sim ON sim.user_id = fl.user_id" +
            "        WHERE NOT EXISTS (" +
            "            SELECT 1 FROM film_likes" +
            "            WHERE user_id = ? AND film_id = f.id" +
            "        )" +
            "        GROUP BY f.id";

    private static final String SELECT_DIRECTORS_QUERY =
            "SELECT d.id, d.name FROM film_director fd JOIN directors d ON fd.director_id = d.id " +
                    "WHERE fd.film_id = ? ORDER BY d.id";
    private static final String DELETE_FILM_DIRECTORS_QUERY = "DELETE FROM film_director WHERE film_id = ?";
    private static final String INSERT_FILM_DIRECTOR_QUERY = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
    private static final String SELECT_FILMS_BY_DIRECTOR_YEAR_QUERY =
            "SELECT f.* FROM films f JOIN film_director fd ON f.id = fd.film_id " +
                    "WHERE fd.director_id = ? ORDER BY f.release_date";
    private static final String SELECT_FILMS_BY_DIRECTOR_LIKES_QUERY =
            "SELECT f.*, COUNT(l.user_id) AS like_count FROM films f " +
                    "JOIN film_director fd ON f.id = fd.film_id " +
                    "LEFT JOIN film_likes l ON f.id = l.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.id " +
                    "ORDER BY like_count DESC";

    private static final String SEARCH_BY_TITLE_QUERY =
            "SELECT f.*, COUNT(l.user_id) AS like_count " +
                    "FROM films f " +
                    "LEFT JOIN film_likes l ON f.id = l.film_id " +
                    "WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "GROUP BY f.id " +
                    "ORDER BY like_count DESC";

    private static final String BASE_FILMS_QUERY =
            "SELECT f.*, COUNT(l.user_id) AS like_count " +
                    "FROM films f " +
                    "LEFT JOIN film_director fd ON f.id = fd.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.id " +
                    "LEFT JOIN film_likes l ON f.id = l.film_id ";

    private static final String SEARCH_BY_DIRECTOR_QUERY =
                    "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "GROUP BY f.id " +
                    "ORDER BY like_count DESC";
    private static final String SEARCH_BY_TITLE_OR_DIRECTOR_QUERY =
                    "WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "   OR LOWER(d.name) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "GROUP BY f.id " +
                    "ORDER BY like_count DESC";

    private final GenreRowMapper genreRowMapper;
    private final DirectorRowMapper directorRowMapper;
    private final MpaRatingDbStorage mpaStorage;


    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper, GenreRowMapper genreRowMapper,
                         DirectorRowMapper directorRowMapper, MpaRatingDbStorage mpaStorage) {
        super(jdbc, mapper);
        this.genreRowMapper = genreRowMapper;
        this.directorRowMapper = directorRowMapper;
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
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                jdbc.update(INSERT_FILM_DIRECTOR_QUERY, id, director.getId());
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

        jdbc.update(DELETE_FILM_DIRECTORS_QUERY, filmId);
        if (updatedFilm.getDirectors() != null && !updatedFilm.getDirectors().isEmpty()) {
            for (Director director : updatedFilm.getDirectors()) {
                jdbc.update(INSERT_FILM_DIRECTOR_QUERY, filmId, director.getId());
            }
        }
        return updatedFilm;
    }


    public Film deleteFilm(Film film) {
        update(DELETE_QUERY, film.getId());
        return film;
    }

    @Override
    public Optional<Film> getFilmById(long filmId) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID_QUERY, filmId);
        filmOpt.ifPresent(this::enrichFilm);
        return filmOpt;
    }

    @Override
    public Collection<Film> getAllFilms() {
        List<Film> films = findMany(FIND_ALL_QUERY);
        films.forEach(this::enrichFilm);
        return films;
    }

    @Override
    public void addLike(long filmId, long userId) {
        jdbc.update(INSERT_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count, Long genreId, Long year) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.*, COUNT(DISTINCT fl.user_id) AS like_count " +
                        "FROM films f " +
                        "LEFT JOIN film_likes fl ON f.id = fl.film_id ");
        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append("JOIN film_genre fg ON f.id = fg.film_id ");
        }
        sql.append("WHERE 1=1 ");
        if (genreId != null) {
            sql.append("AND fg.genre_id = ? ");
            params.add(genreId);
        }
        if (year != null) {
            sql.append("AND YEAR(f.release_date) = ? ");
            params.add(year);
        }
        sql.append("GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id ");
        sql.append("ORDER BY like_count DESC, f.id ASC LIMIT ?");
        params.add(count);

        List<Film> films = jdbc.query(sql.toString(), mapper, params.toArray());
        films.forEach(this::enrichFilm);
        return films;
    }

    @Override
    public List<Film> getFilmsByDirector(long directorId, String sortBy) {
        String query = "likes".equals(sortBy) ? SELECT_FILMS_BY_DIRECTOR_LIKES_QUERY : SELECT_FILMS_BY_DIRECTOR_YEAR_QUERY;
        List<Film> films = jdbc.query(query, mapper, directorId);
        films.forEach(this::enrichFilm);
        return films;
    }

    @Override
    public List<Film> searchFilms(String query, List<String> by) {
        boolean byTitle = by.contains("title");
        boolean byDirector = by.contains("director");

        List<Film> films;
        if (byTitle && byDirector) {
            films = jdbc.query(BASE_FILMS_QUERY + SEARCH_BY_TITLE_OR_DIRECTOR_QUERY, mapper, query, query);
        } else if (byTitle) {
            films = jdbc.query(SEARCH_BY_TITLE_QUERY, mapper, query);
        } else if (byDirector) {
            films = jdbc.query(BASE_FILMS_QUERY + SEARCH_BY_DIRECTOR_QUERY, mapper, query);
        } else {
            return List.of();
        }

        films.forEach(this::enrichFilm);
        return films;
    }

    @Override
    public List<Film> getCommonFriendsFilms(long userId, long friendId) {
        List<Film> films = jdbc.query(SELECT_COMMON_FRIEND_FILM_QUERY, mapper, userId, friendId);
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

    private void enrichFilm(Film film) {
        Integer mpaId = jdbc.queryForObject(SELECT_MPA_QUERY, Integer.class, film.getId());
        if (mpaId != null) {
            mpaStorage.findById(mpaId).ifPresent(film::setMpaRating);
        }
        List<Genre> genres = jdbc.query(SELECT_GENRES_QUERY, genreRowMapper, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
        List<Long> likes = jdbc.queryForList(SELECT_LIKES_QUERY, Long.class, film.getId());
        film.setLikes(new HashSet<>(likes));
        List<Director> directors = jdbc.query(SELECT_DIRECTORS_QUERY, directorRowMapper, film.getId());
        film.setDirectors(new LinkedHashSet<>(directors));
    }


    @Override
    public List<Film> getRecommendations(long userId) {
        List<Film> films = findMany(RECOMMENDATIONS_QUERY, userId, userId, userId);
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
