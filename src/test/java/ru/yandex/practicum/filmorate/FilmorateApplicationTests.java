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
    void emptyCheck_null() {
        assertThrows(ValidationException.class, () -> validator.emptyCheck(null));
    }

    @Test
    void emptyCheck_emptyString() {
        assertThrows(ValidationException.class, () -> validator.emptyCheck(""));
    }

    @Test
    void emptyCheck_blankString() {
        assertThrows(ValidationException.class, () -> validator.emptyCheck("   "));
    }

    @Test
    void emptyCheck_valid() {
        assertEquals("abc", validator.emptyCheck("abc"));
    }

    @Test
    void descriptionLength_valid() {
        String desc = "a".repeat(199);
        assertEquals(desc, validator.descriptionLength(desc));
    }

    @Test
    void descriptionLength_validВorder() {
        String desc = "a".repeat(200);
        assertEquals(desc, validator.descriptionLength(desc));
    }

    @Test
    void descriptionLength_invalid() {
        assertThrows(ValidationException.class, () -> validator.descriptionLength("a".repeat(201)));
    }

    @Test
    void filmDate_correctDate_ok() {
        LocalDate date = LocalDate.of(1895, 12, 28);
        assertEquals(date, validator.filmDate(date));
    }

    @Test
    void filmDate_laterDate_ok() {
        LocalDate date = LocalDate.of(2000, 1, 1);
        assertEquals(date, validator.filmDate(date));
    }

    @Test
    void filmDate_earlierDate_throws() {
        LocalDate date = LocalDate.of(1895, 12, 27);
        assertThrows(ValidationException.class, () -> validator.filmDate(date));
    }

    @Test
    void positiveCheck_valid() {
        Integer value = 10;
        assertEquals(value, validator.positiveCheck(value));
    }

    @Test
    void positiveCheck_null() {
        assertThrows(ValidationException.class, () -> validator.positiveCheck(null));
    }

    @Test
    void positiveCheck_zero() {
        assertThrows(ValidationException.class, () -> validator.positiveCheck(0));
    }

    @Test
    void positiveCheck_negative() {
        assertThrows(ValidationException.class, () -> validator.positiveCheck(-5));
    }

    @Test
    void loginCheck_valid() {
        assertEquals("user123", validator.loginCheck("user123"));
    }

    @Test
    void loginCheck_withSpace() {
        assertThrows(ValidationException.class, () -> validator.loginCheck("user 123"));
    }

    @Test
    void loginCheck_empty() {
        assertThrows(ValidationException.class, () -> validator.loginCheck(""));
    }

    @Test
    void emailCheck_valid() {
        assertEquals("user@mail.ru", validator.emailCheck("user@mail.ru"));
    }

    @Test
    void emailCheck_invalid() {
        assertThrows(ValidationException.class, () -> validator.emailCheck("usermail.ru"));
    }

    @Test
    void emailCheck_empty() {
        assertThrows(ValidationException.class, () -> validator.emailCheck(""));
    }

    @Test
    void birthDayCheck_valid() {
        LocalDate past = LocalDate.now(ZoneId.of("Europe/Moscow")).minusDays(1);
        assertEquals(past, validator.birthDayCheck(past));
    }

    @Test
    void birthDayCheck_validNow() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
        assertEquals(today, validator.birthDayCheck(today));
    }

    @Test
    void birthDayCheck_invalid() {
        LocalDate future = LocalDate.now(ZoneId.of("Europe/Moscow")).plusDays(1);
        assertThrows(ValidationException.class, () -> validator.birthDayCheck(future));
    }

    @Test
    void filmValidation_valid() {
        assertDoesNotThrow(() -> validator.filmValidation(
                "Film", "Desc", LocalDate.of(2000, 1, 1), 90
        ));
    }

    @Test
    void filmValidation_emptyName() {
        assertThrows(ValidationException.class, () -> validator.filmValidation(
                "", "Desc", LocalDate.of(2000, 1, 1), 90
        ));
    }

    @Test
    void filmValidation_longDescription() {
        assertThrows(ValidationException.class, () -> validator.filmValidation(
                "Film", "a".repeat(201), LocalDate.of(2000, 1, 1), 90
        ));
    }

    @Test
    void filmValidation_oldDate() {
        assertThrows(ValidationException.class, () -> validator.filmValidation(
                "Film", "Desc", LocalDate.of(1800, 1, 1), 90
        ));
    }

    @Test
    void filmValidation_negativeDuration() {
        assertThrows(ValidationException.class, () -> validator.filmValidation(
                "Film", "Desc", LocalDate.of(2000, 1, 1), -10
        ));
    }

    @Test
    void userValidation_valid() {
        assertDoesNotThrow(() -> validator.userValidation(
                "user@mail.ru", "login", LocalDate.now(ZoneId.of("Europe/Moscow")).minusYears(20)
        ));
    }

    @Test
    void userValidation_invalidEmail() {
        assertThrows(ValidationException.class, () -> validator.userValidation(
                "usermail.ru", "login", LocalDate.now(ZoneId.of("Europe/Moscow")).minusYears(20)
        ));
    }

    @Test
    void userValidation_loginWithSpace() {
        assertThrows(ValidationException.class, () -> validator.userValidation(
                "user@mail.ru", "login with space", LocalDate.now(ZoneId.of("Europe/Moscow")).minusYears(20)
        ));
    }

    @Test
    void userValidation_futureBirthday() {
        assertThrows(ValidationException.class, () -> validator.userValidation(
                "user@mail.ru", "login", LocalDate.now(ZoneId.of("Europe/Moscow")).plusDays(1)
        ));
    }
}
