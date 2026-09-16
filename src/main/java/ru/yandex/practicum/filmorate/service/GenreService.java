package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreStorage;

    public List<Genre> getAll() {
        log.info("Получение списка всех жанров");
        return genreStorage.findAll();
    }

    public Genre getById(int id) {
        log.info("Получение жанра с id {}", id);
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
    }
}