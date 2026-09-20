package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.dal.DirectorDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorStorage;
    private final Validator validator;

    public List<Director> getAll() {
        log.info("Получение списка всех режиссёров");
        return directorStorage.findAll();
    }

    public Director getById(long id) {
        log.info("Получение режиссёра с id {}", id);
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + id + " не найден"));
    }

    public Director create(Director director) {
        validator.emptyCheck(director.getName());
        Director created = directorStorage.create(director);
        log.info("Режиссёр '{}' успешно создан с id {}", created.getName(), created.getId());
        return created;
    }

    public Director update(Director director) {
        if (director.getId() == null) {
            throw new ValidationException("Id режиссёра должен быть указан");
        }
        getById(director.getId());
        validator.emptyCheck(director.getName());
        Director updated = directorStorage.save(director);
        log.info("Режиссёр с id {} успешно обновлён", updated.getId());
        return updated;
    }

    public void delete(long id) {
        getById(id);
        directorStorage.remove(id);
        log.info("Режиссёр с id {} удалён", id);
    }
}