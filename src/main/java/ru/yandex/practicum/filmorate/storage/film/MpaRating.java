package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MpaRating {
    G(1, "G", "G — у фильма нет возрастных ограничений"),
    PG(2, "PG", "PG — детям рекомендуется смотреть фильм с родителями"),
    PG_13(3, "PG-13", "PG-13 — детям до 13 лет просмотр не желателен"),
    R(4, "R", "R — лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
    NC_17(5, "NC-17", "NC-17 — лицам до 18 лет просмотр запрещён");

    private final int id;
    private final String code;      // короткое имя
    private final String description;

    public static MpaRating fromId(int id) {
        for (MpaRating rating : values()) {
            if (rating.id == id) return rating;
        }
        throw new IllegalArgumentException("Invalid MPA rating id: " + id);
    }
}
