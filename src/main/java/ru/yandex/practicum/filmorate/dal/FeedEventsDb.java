package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FeedEventsRowMapper;
import ru.yandex.practicum.filmorate.dto.FeedEventsDto;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FeedEventsDb {
    private final JdbcTemplate jdbc;
    private final FeedEventsRowMapper feedEventsRowMapper;

    private static final String INSERT_EVENTS = "INSERT INTO FeedEvents(timestamp, user_id, event_type, " +
            "operation, entity_id) VALUES (?, ?, ?, ?, ?)";

    private static final String GET_EVENTS = """
    SELECT fe.*
        FROM FeedEvents fe
        WHERE fe.user_id = ?
        ORDER BY fe.timestamp ASC, fe.event_id ASC""";

    public void insertEvents(Long timestamp, Long userId, String eventType, String operation, Long entityId) {
        jdbc.update(INSERT_EVENTS, timestamp, userId, eventType, operation, entityId);
    }

    public List<FeedEventsDto> getEvents(Long userId) {
        return jdbc.query(GET_EVENTS, feedEventsRowMapper, userId).stream().map(feedEventsRowMapper::mapToFeedEventsDto)
                .toList();
    }
}
