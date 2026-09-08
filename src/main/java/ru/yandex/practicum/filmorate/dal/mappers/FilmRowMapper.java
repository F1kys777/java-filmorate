package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setDuration(resultSet.getInt("duration"));

        int mpaRatingId = resultSet.getInt("mpa_rating_id");
        MpaRating mpaRating = MpaRating.fromId(mpaRatingId);
        film.setMpaRating(mpaRating);

        LocalDate releaseDate = resultSet.getObject("release_date", LocalDate.class);
        film.setReleaseDate(releaseDate);

        return film;
    }
}
