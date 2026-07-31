package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
public class Validator {
    private static final LocalDate correctDate = LocalDate.of(1895, 12, 28);

    public void filmValidation(String name, String description, LocalDate releaseDate, Integer duration) {
        emptyCheck(name);
        descriptionLength(description);
        filmDate(releaseDate);
        positiveCheck(duration);
    }

    public void userValidation(String email, String login, LocalDate birthday){
        emailCheck(email);
        loginCheck(login);
        birthDayCheck(birthday);
    }

    public String emptyCheck(String value){
        if (value == null || value.isBlank()) {
            log.warn("Ошибка валидации: значение не может быть пустым");
            throw new ValidationException("Значение не может быть пустым");
        }
        return value;
    }

    public Integer positiveCheck(Integer duration) {
        if (duration == null) {
            log.warn("Ошибка валидации: продолжительность не указана");
            throw new ValidationException("Продолжительность фильма должна быть указана");
        }
        if (duration <= 0) {
            log.warn("Ошибка валидации: продолжительность должна быть положительным числом, получено {}", duration);
            throw new ValidationException("Продолжительность фильма должна быть положительным числом!");
        }
        return duration;
    }

    public String loginCheck(String login) {
        emptyCheck(login);
        if(login.contains(" ")) {
            log.warn("Ошибка валидации: логин '{}' содержит пробелы", login);
            throw new ValidationException("Login не должен содержать пробелов!");
        }
        return login;
    }

    public String emailCheck(String email) {
        emptyCheck(email);
        if(!email.contains("@")) {
            log.warn("Ошибка валидации: email '{}' не содержит символ @", email);
            throw new ValidationException("Email должен содержать символ @ !");
        }
        return email;
    }

    public LocalDate filmDate(LocalDate releaseDate) {
        if(correctDate.isAfter(releaseDate)) {
            log.warn("Ошибка валидации: дата создания {} ранее 28.12.1895", releaseDate);
            throw new ValidationException("Дата создания фильма не может быть раньше 28.12.1895!");
        }
        return releaseDate;
    }

    public LocalDate birthDayCheck(LocalDate birthday) {
        Instant instant = Instant.now();
        LocalDate localDate = instant.atZone(ZoneId.of("Europe/Moscow")).toLocalDate();

        if(localDate.isBefore(birthday)) {
            log.warn("Ошибка валидации: дата рождения {} в будущем", birthday);
            throw new ValidationException("Дата рождения не может в будущем!");
        }
        return birthday;
    }

    public String descriptionLength(String description) {
        if (description.length() > 200) {
            log.warn("Ошибка валидации: описание превышает 200 символов ({} символов)", description.length());
            throw new ValidationException("Максимальная длина описания не может превышать 200 символов!");
        }
        return description;
    }
}
