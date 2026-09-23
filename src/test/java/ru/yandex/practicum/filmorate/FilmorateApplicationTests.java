/*package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.user.FriendshipStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, MpaRatingDbStorage.class, DirectorDbStorage.class,
        UserRowMapper.class, FilmRowMapper.class, GenreRowMapper.class, MpaRatingRowMapper.class,
        DirectorRowMapper.class
})
class FilmorateApplicationTests {

    private final Validator validator = new Validator();
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private User user;
    private Film film;

    @Test
    void emptyCheck_null_throws() {
        assertThrows(ValidationException.class, () -> validator.emptyCheck(null));
    }

    @Test
    void emptyCheck_emptyString_throws() {
        assertThrows(ValidationException.class, () -> validator.emptyCheck(""));
    }

    @Test
    void emptyCheck_blankString_throws() {
        assertThrows(ValidationException.class, () -> validator.emptyCheck("   "));
    }

    @Test
    void emptyCheck_valid_doesNotThrow() {
        assertDoesNotThrow(() -> validator.emptyCheck("abc"));
    }

    @Test
    void descriptionLength_valid_under200() {
        assertDoesNotThrow(() -> validator.descriptionLength("a".repeat(199)));
    }

    @Test
    void descriptionLength_valid_exactly200() {
        assertDoesNotThrow(() -> validator.descriptionLength("a".repeat(200)));
    }

    @Test
    void descriptionLength_invalid_over200() {
        assertThrows(ValidationException.class, () -> validator.descriptionLength("a".repeat(201)));
    }

    @Test
    void filmDate_correctDate_ok() {
        assertDoesNotThrow(() -> validator.filmDate(LocalDate.of(1895, 12, 28)));
    }

    @Test
    void filmDate_laterDate_ok() {
        assertDoesNotThrow(() -> validator.filmDate(LocalDate.of(2000, 1, 1)));
    }

    @Test
    void filmDate_earlierDate_throws() {
        assertThrows(ValidationException.class, () -> validator.filmDate(LocalDate.of(1895, 12, 27)));
    }

    @Test
    void positiveCheck_valid_positive() {
        assertDoesNotThrow(() -> validator.positiveCheck(10));
    }

    @Test
    void positiveCheck_null_throws() {
        assertThrows(ValidationException.class, () -> validator.positiveCheck(null));
    }

    @Test
    void positiveCheck_zero_throws() {
        assertThrows(ValidationException.class, () -> validator.positiveCheck(0));
    }

    @Test
    void positiveCheck_negative_throws() {
        assertThrows(ValidationException.class, () -> validator.positiveCheck(-5));
    }

    @Test
    void loginCheck_valid_doesNotThrow() {
        assertDoesNotThrow(() -> validator.loginCheck("user123"));
    }

    @Test
    void loginCheck_withSpace_throws() {
        assertThrows(ValidationException.class, () -> validator.loginCheck("user 123"));
    }

    @Test
    void loginCheck_empty_throws() {
        assertThrows(ValidationException.class, () -> validator.loginCheck(""));
    }

    @Test
    void emailCheck_valid_doesNotThrow() {
        assertDoesNotThrow(() -> validator.emailCheck("user@mail.ru"));
    }

    @Test
    void emailCheck_invalid_noAt_throws() {
        assertThrows(ValidationException.class, () -> validator.emailCheck("usermail.ru"));
    }

    @Test
    void emailCheck_empty_throws() {
        assertThrows(ValidationException.class, () -> validator.emailCheck(""));
    }

    @Test
    void birthDayCheck_past_doesNotThrow() {
        LocalDate past = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
        assertDoesNotThrow(() -> validator.birthDayCheck(past));
    }

    @Test
    void birthDayCheck_today_doesNotThrow() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
        assertDoesNotThrow(() -> validator.birthDayCheck(today));
    }

    @Test
    void birthDayCheck_future_throws() {
        LocalDate future = LocalDate.now(ZoneId.of("Europe/Moscow")).plusDays(1);
        assertThrows(ValidationException.class, () -> validator.birthDayCheck(future));
    }

    @Test
    void filmValidation_valid_doesNotThrow() {
        assertDoesNotThrow(() -> validator.filmValidation(
                "Film", "Desc", LocalDate.of(2000, 1, 1), 90
        ));
    }

    @Test
    void filmValidation_emptyName_throws() {
        assertThrows(ValidationException.class, () -> validator.filmValidation(
                "", "Desc", LocalDate.of(2000, 1, 1), 90
        ));
    }

    @Test
    void filmValidation_longDescription_throws() {
        assertThrows(ValidationException.class, () -> validator.filmValidation(
                "Film", "a".repeat(201), LocalDate.of(2000, 1, 1), 90
        ));
    }

    @Test
    void filmValidation_oldDate_throws() {
        assertThrows(ValidationException.class, () -> validator.filmValidation(
                "Film", "Desc", LocalDate.of(1800, 1, 1), 90
        ));
    }

    @Test
    void filmValidation_negativeDuration_throws() {
        assertThrows(ValidationException.class, () -> validator.filmValidation(
                "Film", "Desc", LocalDate.of(2000, 1, 1), -10
        ));
    }

    @Test
    void userValidation_valid_doesNotThrow() {
        assertDoesNotThrow(() -> validator.userValidation(
                "user@mail.ru", "login", LocalDate.now(ZoneId.of("Europe/Moscow")).minusYears(20)
        ));
    }

    @Test
    void userValidation_invalidEmail_throws() {
        assertThrows(ValidationException.class, () -> validator.userValidation(
                "usermail.ru", "login", LocalDate.now(ZoneId.of("Europe/Moscow")).minusYears(20)
        ));
    }

    @Test
    void userValidation_loginWithSpace_throws() {
        assertThrows(ValidationException.class, () -> validator.userValidation(
                "user@mail.ru", "login with space", LocalDate.now(ZoneId.of("Europe/Moscow")).minusYears(20)
        ));
    }

    @Test
    void userValidation_futureBirthday_throws() {
        assertThrows(ValidationException.class, () -> validator.userValidation(
                "user@mail.ru", "login", LocalDate.now(ZoneId.of("Europe/Moscow")).plusDays(1)
        ));
    }

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Alice");
        user.setEmail("alice@mail.ru");
        user.setLogin("alice");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user = userStorage.addUser(user);

        film = new Film();
        film.setName("Inception");
        film.setDescription("Dreams");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        film.setMpaRating(new MpaRating(3L, null));
        film.setGenres(new LinkedHashSet<>(List.of(new Genre(4, null))));
        film = filmStorage.addFilm(film);
    }

    @Test
    public void testFindUserById() {
        Optional<User> userOptional = userStorage.getUserById(user.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", user.getId())
                );
    }

    @Test
    public void testFindUserByIdNotFound() {
        Optional<User> userOptional = userStorage.getUserById(9999L);
        assertThat(userOptional).isEmpty();
    }

    @Test
    public void testAddUser() {
        User newUser = new User();
        newUser.setName("Bob");
        newUser.setEmail("bob@mail.ru");
        newUser.setLogin("bob");
        newUser.setBirthday(LocalDate.of(1991, 2, 2));

        User saved = userStorage.addUser(newUser);

        assertThat(saved).hasFieldOrPropertyWithValue("email", "bob@mail.ru");
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    public void testUpdateUser() {
        user.setName("Alice Updated");
        user.setEmail("new@mail.ru");
        userStorage.updateUser(user);

        Optional<User> updated = userStorage.getUserById(user.getId());

        assertThat(updated)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u).hasFieldOrPropertyWithValue("name", "Alice Updated");
                    assertThat(u).hasFieldOrPropertyWithValue("email", "new@mail.ru");
                });
    }

    @Test
    public void testDeleteUser() {
        userStorage.deleteUser(user);
        Optional<User> deleted = userStorage.getUserById(user.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    public void testGetAllUsers() {
        Collection<User> users = userStorage.getAllUsers();
        assertThat(users).hasSize(1);
    }

    @Test
    public void testAddFriend() {
        User friend = makeUser("bob@mail.ru", "bob");
        User savedFriend = userStorage.addUser(friend);

        userStorage.addFriend(user.getId(), savedFriend.getId(), FriendshipStatus.CONFIRMED);

        Optional<User> reloaded = userStorage.getUserById(user.getId());
        assertThat(reloaded)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u.getFriends()).contains(savedFriend.getId())
                );
    }

    @Test
    public void testRemoveFriend() {
        User friend = makeUser("bob@mail.ru", "bob");
        User savedFriend = userStorage.addUser(friend);

        userStorage.addFriend(user.getId(), savedFriend.getId(), FriendshipStatus.CONFIRMED);
        userStorage.removeFriend(user.getId(), savedFriend.getId());

        Optional<User> reloaded = userStorage.getUserById(user.getId());
        assertThat(reloaded)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u.getFriends()).doesNotContain(savedFriend.getId())
                );
    }

    @Test
    public void testGetFriends() {
        User friend = makeUser("bob@mail.ru", "bob");
        User savedFriend = userStorage.addUser(friend);

        userStorage.addFriend(user.getId(), savedFriend.getId(), FriendshipStatus.CONFIRMED);

        List<User> friends = userStorage.getFriends(user.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0)).hasFieldOrPropertyWithValue("id", savedFriend.getId());
    }

    @Test
    public void testGetCommonFriends() {
        User friend1 = makeUser("bob@mail.ru", "bob");
        User savedFriend1 = userStorage.addUser(friend1);

        User friend2 = makeUser("carol@mail.ru", "carol");
        User savedFriend2 = userStorage.addUser(friend2);

        userStorage.addFriend(user.getId(), savedFriend2.getId(), FriendshipStatus.CONFIRMED);
        userStorage.addFriend(savedFriend1.getId(), savedFriend2.getId(), FriendshipStatus.CONFIRMED);

        List<User> common = userStorage.getCommonFriends(user.getId(), savedFriend1.getId());
        assertThat(common).hasSize(1);
        assertThat(common.get(0)).hasFieldOrPropertyWithValue("id", savedFriend2.getId());
    }

    @Test
    public void testFindFilmById() {
        Optional<Film> filmOptional = filmStorage.getFilmById(film.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", film.getId())
                );
    }

    @Test
    public void testFindFilmByIdNotFound() {
        Optional<Film> filmOptional = filmStorage.getFilmById(9999L);
        assertThat(filmOptional).isEmpty();
    }

    @Test
    public void testAddFilm() {
        Film newFilm = new Film();
        newFilm.setName("Matrix");
        newFilm.setDescription("Neo");
        newFilm.setReleaseDate(LocalDate.of(1999, 3, 31));
        newFilm.setDuration(136);
        newFilm.setMpaRating(new MpaRating(4L, null));
        newFilm.setGenres(new LinkedHashSet<>(List.of(new Genre(6, null))));

        Film saved = filmStorage.addFilm(newFilm);

        assertThat(saved).hasFieldOrPropertyWithValue("name", "Matrix");
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    public void testUpdateFilm() {
        film.setName("Updated");
        film.setMpaRating(new MpaRating(4L, null));
        film.setGenres(new LinkedHashSet<>(List.of(new Genre(6, null))));
        filmStorage.updateFilm(film.getId(), film);

        Optional<Film> updated = filmStorage.getFilmById(film.getId());

        assertThat(updated)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getName()).isEqualTo("Updated");
                    assertThat(f.getMpaRating().getId()).isEqualTo(4L);
                    assertThat(f.getGenres())
                            .extracting(Genre::getId)
                            .containsExactly(6);
                });
    }

    @Test
    public void testDeleteFilm() {
        filmStorage.deleteFilm(film);
        Optional<Film> deleted = filmStorage.getFilmById(film.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    public void testGetAllFilms() {
        filmStorage.addFilm(makeFilm("Matrix"));

        Collection<Film> films = filmStorage.getAllFilms();
        assertThat(films).hasSize(2);
    }

    @Test
    public void testAddLike() {
        filmStorage.addLike(film.getId(), user.getId());

        Optional<Film> reloaded = filmStorage.getFilmById(film.getId());
        assertThat(reloaded)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f.getLikes()).contains(user.getId())
                );
    }

    @Test
    public void testRemoveLike() {
        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        Optional<Film> reloaded = filmStorage.getFilmById(film.getId());
        assertThat(reloaded)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f.getLikes()).doesNotContain(user.getId())
                );
    }

    @Test
    public void testGetPopularFilms() {
        Film second = filmStorage.addFilm(makeFilm("Matrix"));
        User secondUser = userStorage.addUser(makeUser("bob@mail.ru", "bob"));

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.addLike(second.getId(), user.getId());
        filmStorage.addLike(second.getId(), secondUser.getId());

        List<Film> popular = filmStorage.getPopularFilms(10);

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0)).hasFieldOrPropertyWithValue("id", second.getId());
    }

    private User makeUser(String email, String login) {
        User u = new User();
        u.setName(login);
        u.setEmail(email);
        u.setLogin(login);
        u.setBirthday(LocalDate.of(1990, 1, 1));
        return u;
    }

    private Film makeFilm(String name) {
        Film f = new Film();
        f.setName(name);
        f.setDescription("desc");
        f.setReleaseDate(LocalDate.of(1999, 3, 31));
        f.setDuration(136);
        f.setMpaRating(new MpaRating(4L, null));
        f.setGenres(new LinkedHashSet<>(List.of(new Genre(6, null))));
        return f;
    }
}
*/