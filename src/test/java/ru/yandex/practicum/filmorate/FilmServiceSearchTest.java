/*package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.Validator;
import ru.yandex.practicum.filmorate.dal.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilmServiceSearchTest {

    @Mock
    private FilmStorage filmStorage;
    @Mock
    private UserStorage userStorage;
    @Mock
    private MpaRatingDbStorage mpaStorage;
    @Mock
    private GenreDbStorage genreStorage;
    @Mock
    private DirectorDbStorage directorStorage;

    private FilmService filmService;

    private FilmService createService() {
        return new FilmService(filmStorage, new Validator(), userStorage, mpaStorage, genreStorage, directorStorage);
    }

    @Test
    void shouldThrowWhenQueryIsBlank() {
        filmService = createService();
        assertThrows(ValidationException.class, () -> filmService.searchFilms(" ", "title"));
    }

    @Test
    void shouldThrowWhenQueryIsNull() {
        filmService = createService();
        assertThrows(ValidationException.class, () -> filmService.searchFilms(null, "title"));
    }

    @Test
    void shouldDefaultByToTitleWhenNotProvided() {
        filmService = createService();
        when(filmStorage.searchFilms(eq("крад"), any())).thenReturn(List.of());

        filmService.searchFilms("крад", null);

        verify(filmStorage).searchFilms("крад", List.of("title"));
    }

    @Test
    void shouldParseCommaSeparatedByValues() {
        filmService = createService();
        when(filmStorage.searchFilms(eq("крад"), any())).thenReturn(List.of());

        filmService.searchFilms("крад", "director,title");

        verify(filmStorage).searchFilms("крад", List.of("director", "title"));
    }

    @Test
    void shouldBeCaseInsensitiveAndTrimByValues() {
        filmService = createService();
        when(filmStorage.searchFilms(eq("крад"), any())).thenReturn(List.of());

        filmService.searchFilms("крад", " TITLE , Director ");

        verify(filmStorage).searchFilms("крад", List.of("title", "director"));
    }

    @Test
    void shouldThrowWhenByValueIsInvalid() {
        filmService = createService();
        assertThrows(ValidationException.class, () -> filmService.searchFilms("крад", "actor"));
    }

    @Test
    void shouldMapStorageResultToDto() {
        filmService = createService();
        Film film = new Film();
        film.setId(1L);
        film.setName("Крадущийся в ночи");
        when(filmStorage.searchFilms(eq("крад"), any())).thenReturn(List.of(film));

        List<FilmDto> result = filmService.searchFilms("крад", "title");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Крадущийся в ночи");
    }
}*/