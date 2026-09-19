package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import ru.yandex.practicum.filmorate.validator.Create;

@Data
public class NewReviewRequest {
    @NotNull(groups = Create.class, message = "Необходимо указать id фильма")
    private Long filmId;
    @NotNull(groups = Create.class, message = "Необходимо указать id пользователя")
    private Long userId;
    @NotNull(groups = Create.class, message = "Необходимо указать, положительный ли отзыв")
    private Boolean isPositive;
    @NotBlank(groups = Create.class, message = "Необходимо оставить комментарий")
    private String content;
}
