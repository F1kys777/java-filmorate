package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class UpdateFilmRequest {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaRating mpa;
    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<Genre> genres;
    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<Director> directors;
    private Long id;

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
        return ! (releaseDate == null);
    }
}