package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    private final Validator validator = new Validator();

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
}