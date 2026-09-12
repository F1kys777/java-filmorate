package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaRatingDbStorage {
    private static final String FIND_ALL = "SELECT * FROM mpa_ratings ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM mpa_ratings WHERE id = ?";

    private final JdbcTemplate jdbc;
    private final MpaRatingRowMapper mapper;

    public MpaRatingDbStorage(JdbcTemplate jdbc, MpaRatingRowMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public List<MpaRating> findAll() {
        return jdbc.query(FIND_ALL, mapper);
    }

    public Optional<MpaRating> findById(int id) {
        List<MpaRating> list = jdbc.query(FIND_BY_ID, mapper, id);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}
