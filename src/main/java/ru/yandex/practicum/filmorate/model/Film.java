package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;


@Data
public class Film {
    Long id; //генерит прогой
    String name; //передается json - ВАЛИДИРУЕМ(НЕ ПУСТОЕ)
    String description; //передается json - ВАЛИДИРУЕМ(НЕ ДЛИННЕЕ 200 символов)
    LocalDate releaseDate; //передается json - ВАЛИДИРУЕМ(НЕ РАНЕЕ 28.12.1895)
    Duration duration; //передается json - ВАЛИДИРУЕМ(ПОЛОЖИТЕЛЬНОЕ ЧИСЛО)
}
