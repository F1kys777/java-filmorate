package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.film.MpaRating;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaController {

    @GetMapping
    public List<MpaDto> getAllMpaRatings() {
        log.debug("Запрос на получение всех рейтингов");
        return Arrays.stream(MpaRating.values())
                .sorted(Comparator.comparingLong(MpaRating::getId))
                .map(MpaDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MpaDto getMpaRatingById(@PathVariable int id) {
        log.debug("Запрос на получение рейтинга с id {}", id);
        try {
            return new MpaDto(MpaRating.fromId(id));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Рейтинг с id=" + id + " не найден");
        }
    }
}
