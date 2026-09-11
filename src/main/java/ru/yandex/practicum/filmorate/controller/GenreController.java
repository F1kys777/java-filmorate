package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.film.Genre;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/genres")
@Slf4j
public class GenreController {

    @GetMapping
    public List<GenreDto> getAllGenres() {
        log.debug("Запрос на получение всех жанров");
        return Arrays.stream(Genre.values())
                .sorted(Comparator.comparingInt(Genre::getId))
                .map(GenreDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public GenreDto getGenreById(@PathVariable int id) {
        log.debug("Запрос на получение жанра с id {}", id);
        try {
            return new GenreDto(Genre.fromId(id));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Жанр с id=" + id + " не найден");
        }
    }
}