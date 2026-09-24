package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class FeedEventsDto {
    private Long eventId;
    private Long timestamp;
    private Long userId;
    private String eventType;
    private String operation;
    private Long entityId;
}
