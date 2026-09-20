package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@Slf4j
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAll() {
        log.debug("Запрос на получение всех режиссёров");
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable long id) {
        log.debug("Запрос на получение режиссёра с id {}", id);
        return directorService.getById(id);
    }

    @PostMapping
    public Director create(@RequestBody(required = false) Director director) {
        log.debug("Создание режиссёра {}", director);
        if (director == null) {
            director = new Director();
        }
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        log.debug("Обновление режиссёра {}", director);
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        log.debug("Удаление режиссёра с id {}", id);
        directorService.delete(id);
    }
}