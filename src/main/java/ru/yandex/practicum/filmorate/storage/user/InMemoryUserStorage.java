package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public void addUser(User user) {
        long userId = getNextId();
        user.setId(userId);
        user.setEmail(user.getEmail().toLowerCase());
        users.put(userId, user);
        log.debug("Добавление User {}, установлен id {}", user, userId);
    }

    @Override
    public User getUserById(long userId) {
        log.debug("Получение User с id {}", userId);
        return users.get(userId);
    }

    @Override
    public boolean containsUser(long userId) {
        log.debug("Проверка существования User с id {}", userId);
        return users.containsKey(userId);
    }

    @Override
    public void updateUser(long userId, User updatedUser) {
        log.debug("Обновление User с id {}, новые данные {}", userId, updatedUser);
        users.put(userId, updatedUser);
    }

    @Override
    public Collection<User> getAllUsers() {
        log.debug("Передача всех пользователей");
        return users.values();
    }

    @Override
    public void deleteUser(long userId) {
        log.debug("Удаление User с id {}", userId);
        users.remove(userId);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
