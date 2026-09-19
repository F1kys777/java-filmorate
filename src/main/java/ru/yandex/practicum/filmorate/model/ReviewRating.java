package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewRating {
    private Long userId;
    private Long reviewId;
    private Integer rating;
}
