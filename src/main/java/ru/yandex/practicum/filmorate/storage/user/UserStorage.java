package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User updatedUser);

    User deleteUser(User user);

    Optional<User> getUserById(long userId);

    Collection<User> getAllUsers();

    void addFriend(long userId, long friendId, FriendshipStatus status);

    void confirmFriend(long userId, long friendId);

    void removeFriend(long userId, long friendId);

    List<User> getFriends(long userId);

    List<User> getCommonFriends(long userId, long otherId);

    Optional<Friendship> findFriendship(long userId, long friendId);

    void updateFriendStatus(long userId, long friendId, FriendshipStatus status);
}