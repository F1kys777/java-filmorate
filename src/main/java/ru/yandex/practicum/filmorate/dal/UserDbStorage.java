package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.Friendship;
import ru.yandex.practicum.filmorate.storage.user.FriendshipStatus;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(name, email, login, birthday)" +
            "VALUES (?, ?, ?, ?) returning id";
    private static final String UPDATE_QUERY = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE id = ?";
    private static final String FRIENDSHIP_QUERY = "SELECT friend_id FROM friendship WHERE user_id = ? AND status = 'CONFIRMED'";
    private static final String FRIEND_ADD_QUERY = "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)";
    private static final String FRIENDSHIP_UPDATE_QUERY = "UPDATE friendship SET status = 'CONFIRMED' WHERE user_id = ? AND friend_id = ?";
    private static final String FRIEND_REMOVE_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS_QUERY = "SELECT u.* FROM users u JOIN friendship f ON u.id = f.friend_id WHERE f.user_id = ? AND f.status = 'CONFIRMED'";
    private static final String COMMON_FRIENDS_QUERY = "SELECT u.* FROM users u JOIN friendship f1 ON u.id = f1.friend_id JOIN friendship f2 ON u.id = f2.friend_id WHERE f1.user_id = ? AND f2.user_id = ? AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'";
    private static final String FIND_FRIENDSHIP_QUERY = "SELECT * FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String FRIENDSHIP_STATUS_UPDATE = "UPDATE friendship SET status = ? WHERE user_id = ? AND friend_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Optional<Friendship> findFriendship(long userId, long friendId) {
        List<Friendship> list = jdbc.query(FIND_FRIENDSHIP_QUERY,
                (resultSet, rowNum) -> new Friendship(
                        resultSet.getLong("user_id"),
                        resultSet.getLong("friend_id"),
                        FriendshipStatus.valueOf(resultSet.getString("status"))
                ), userId, friendId);
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(0));
        }
    }

    public User addUser(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    public User updateUser(User updatedUser) {
        update(
                UPDATE_QUERY,
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getLogin(),
                updatedUser.getBirthday(),
                updatedUser.getId()
        );
        return updatedUser;
    }

    public User deleteUser(User user) {
        update(
                DELETE_QUERY,
                user.getId()
        );
        return user;
    }

    public Optional<User> getUserById(long userId) {
        Optional<User> userOpt = findOne(FIND_BY_ID_QUERY, userId);
        userOpt.ifPresent(user -> {
            List<Long> friendIds = jdbc.queryForList(FRIENDSHIP_QUERY, Long.class, userId);
            user.setFriends(new HashSet<>(friendIds));
        });
        return userOpt;
    }

    public List<User> getAllUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    public void addFriend(long userId, long friendId, FriendshipStatus status) {
        jdbc.update(FRIEND_ADD_QUERY, userId, friendId, status.name());
    }

    public void updateFriendStatus(long userId, long friendId, FriendshipStatus status) {
        jdbc.update(FRIENDSHIP_STATUS_UPDATE, status.name(), userId, friendId);
    }

    public void confirmFriend(long userId, long friendId) {
        update(FRIENDSHIP_UPDATE_QUERY, userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        update(FRIEND_REMOVE_QUERY, userId, friendId);
    }

    public List<User> getFriends(long userId) {
        return findMany(FIND_FRIENDS_QUERY, userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        return findMany(COMMON_FRIENDS_QUERY, userId, otherId);
    }
}
