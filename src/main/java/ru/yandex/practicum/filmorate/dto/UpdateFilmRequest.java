package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.storage.film.Genre;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateFilmRequest {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Genre> genres;
    private Long mpaRatingId;

    public boolean hasName() {
        return ! (name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return ! (description == null || description.isBlank());
    }

    public boolean hasDuration() {
        return ! (duration == null);
    }

    public boolean hasReleaseDate() {
        return releaseDate != null;
    }

    public boolean hasGenres() {
        return ! (genres == null);
    }

    public boolean hasMpaRatingId() {
        return mpaRatingId != null;
    }
}
