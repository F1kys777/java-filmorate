package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    InMemoryUserStorage userStorage;

    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Set<Long> getFriends(long userId) {
        User user = userStorage.getUserById(userId);
        return user.getFriends();
    }

    public Set<Long> getCommonFriends(long userId, long otherId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(otherId);
        Set<Long> mutualFriends = new HashSet<>();
        for (Long id : user.getFriends()) {
            if (friend.getFriends().contains(id)) {
                mutualFriends.add(id);
            }
        }
        return mutualFriends;
    }
}
