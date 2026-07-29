package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    Long id;  //генерит прогой
    String name; //передается json - ВАЛИДИРУЕМ(пустое - устанавливаем логин вместо имени)
    String email; //передается json - ВАЛИДИРУЕМ(не пустая + содержит @)
    String login; //передается json - ВАЛИДИРУЕМ(не пустой + не содержит пробел)
    LocalDate birthday; //передается json, в формате гггг:мм:дд - ВАЛИДИРУЕМ(не будущее)
}
