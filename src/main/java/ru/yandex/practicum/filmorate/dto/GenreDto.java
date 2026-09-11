package ru.yandex.practicum.filmorate.dto;

import lombok.Getter;
import ru.yandex.practicum.filmorate.storage.film.Genre;

@Getter
public class GenreDto {
    private final int id;
    private final String name;

    public GenreDto(Genre genre) {
        this.id = genre.getId();
        this.name = genre.getDisplayName();
    }
}
