package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    void addUser(User user);
    void updateUser(long userId, User updatedUser);
    void deleteUser(long userId);
    User getUserById(long userId);
    Collection<User> getAllUsers();
    boolean containsUser(long userId);
}

