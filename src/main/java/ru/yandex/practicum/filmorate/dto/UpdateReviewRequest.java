package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.Update;

@Data
public class UpdateReviewRequest {
    @NotNull(groups = Update.class, message = "Необходимо указать id отзыва")
    private Long reviewId;
    @NotNull(groups = Update.class, message = "Необходимо указать id фильма")
    private Long filmId;
    @NotNull(groups = Update.class, message = "Необходимо указать id пользователя")
    private Long userId;
    private Boolean isPositive;
    @NotBlank(groups = Update.class, message = "Отзыв не может быть пустым")
    private String content;
}
