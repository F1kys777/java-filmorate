package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long reviewId;
    private Long filmId;
    private Long userId;
    private Boolean isPositive;
    private String content;
    private Integer useful;
}
