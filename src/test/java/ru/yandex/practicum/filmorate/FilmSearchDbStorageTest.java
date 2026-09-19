package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, MpaRatingDbStorage.class, UserDbStorage.class, FilmRowMapper.class,
        GenreRowMapper.class, MpaRatingRowMapper.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmSearchDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film makeFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(120);
        film.setMpaRating(new MpaRating(1L, null));
        return filmStorage.addFilm(film);
    }

    private User makeUser(String email, String login) {
        User user = new User();
        user.setName(login);
        user.setEmail(email);
        user.setLogin(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.addUser(user);
    }

    @Test
    void shouldFindFilmsByTitleSubstringCaseInsensitive() {
        makeFilm("Крадущийся тигр, затаившийся дракон");
        makeFilm("Крадущийся в ночи");
        makeFilm("Матрица");

        List<Film> result = filmStorage.searchFilms("крад", List.of("title"));

        assertThat(result).hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Крадущийся тигр, затаившийся дракон", "Крадущийся в ночи");
    }

    @Test
    void shouldReturnEmptyListWhenNoTitleMatches() {
        makeFilm("Матрица");

        List<Film> result = filmStorage.searchFilms("несуществующий текст", List.of("title"));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenSearchingByDirectorOnly() {
        makeFilm("Крадущийся в ночи");

        List<Film> result = filmStorage.searchFilms("крад", List.of("director"));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSearchByTitleWhenCombinedWithDirector() {
        makeFilm("Крадущийся в ночи");
        makeFilm("Матрица");

        List<Film> result = filmStorage.searchFilms("крад", List.of("director", "title"));

        assertThat(result).hasSize(1)
                .extracting(Film::getName)
                .containsExactly("Крадущийся в ночи");
    }

    @Test
    void shouldSortSearchResultsByPopularityDescending() {
        Film lessPopular = makeFilm("Крадущийся в ночи");
        Film morePopular = makeFilm("Крадущийся тигр, затаившийся дракон");
        User user = makeUser("liker@mail.ru", "liker");

        filmStorage.addLike(morePopular.getId(), user.getId());

        List<Film> result = filmStorage.searchFilms("крад", List.of("title"));

        assertThat(result).extracting(Film::getId)
                .containsExactly(morePopular.getId(), lessPopular.getId());
    }
}