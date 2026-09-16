package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage {
    private static final String FIND_ALL = "SELECT * FROM genres ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE id = ?";

    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public List<Genre> findAll() {
        return jdbc.query(FIND_ALL, mapper);
    }

    public Optional<Genre> findById(int id) {
        List<Genre> list = jdbc.query(FIND_BY_ID, mapper, id);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}
