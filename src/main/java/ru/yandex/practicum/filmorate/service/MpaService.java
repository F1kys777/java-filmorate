package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {
    private final MpaRatingDbStorage mpaStorage;

    public List<MpaRating> getAll() {
        log.info("Получение списка всех рейтингов MPA");
        return mpaStorage.findAll();
    }

    public MpaRating getById(int id) {
        log.info("Получение рейтинга MPA с id {}", id);
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id=" + id + " не найден"));
    }
}