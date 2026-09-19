package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FeedEventsDto;
import ru.yandex.practicum.filmorate.model.FeedEvents;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedEventsRowMapper implements RowMapper<FeedEvents> {
    @Override
    public FeedEvents mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeedEvents feedEvents = new FeedEvents();
        feedEvents.setEventId(rs.getLong("event_id"));
        feedEvents.setTimestamp(rs.getLong("timestamp"));
        feedEvents.setUserId(rs.getLong("user_id"));
        feedEvents.setEventType(rs.getString("event_type"));
        feedEvents.setOperation(rs.getString("operation"));
        feedEvents.setEntityId(rs.getLong("entity_id"));
        return feedEvents;
    }

    public FeedEventsDto mapToFeedEventsDto(FeedEvents feedEvents) {
        FeedEventsDto dto = new FeedEventsDto();
        dto.setEventId(feedEvents.getEventId());
        dto.setTimestamp(feedEvents.getTimestamp());
        dto.setUserId(feedEvents.getUserId());
        dto.setEventType(feedEvents.getEventType());
        dto.setOperation(feedEvents.getOperation());
        dto.setEntityId(feedEvents.getEntityId());
        return dto;
    }
}
