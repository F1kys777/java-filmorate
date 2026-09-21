package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage extends BaseRepository<Director> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE id = ?";

    public DirectorDbStorage(JdbcTemplate jdbc, DirectorRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Director> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Director> findById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Director create(Director director) {
        long id = insert(INSERT_QUERY, director.getName());
        director.setId(id);
        return director;
    }

    public Director save(Director director) {
        update(UPDATE_QUERY, director.getName(), director.getId());
        return director;
    }

    public void remove(long id) {
        update(DELETE_QUERY, id);
    }
}