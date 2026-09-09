package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Genre {
    COMEDY(1,"Комедия"),
    DRAMA(2, "Драма"),
    CARTOON(3, "Мультфильм"),
    THRILLER(4, "Триллер"),
    DOCUMENTARY(5, "Документальный"),
    ACTION(6, "Боевик");

    private final int id;
    private final String displayName;

    public static Genre fromId(int id) {
        for (Genre genre : values()) {
            if (genre.id == id) return genre;
        }
        throw new IllegalArgumentException("Invalid genre id: " + id);
    }
}
