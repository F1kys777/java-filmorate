package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class FeedEvents {
    private Long eventId;
    private Long timestamp;
    private Long userId;
    private String eventType;
    private String operation;
    private Long entityId;
}
