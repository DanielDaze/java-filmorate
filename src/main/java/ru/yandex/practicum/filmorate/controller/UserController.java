package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int idCount = 0;
    private static final String LOG_ERROR = "User Input Error:";

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User add(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            String message = "Адрес электронной почты введен неверно";
            log.info("{}: {}", LOG_ERROR, message);
            throw new ValidationException(message);
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            String message = "Логин не может быть пустым или содержать пробелы";
            log.info("{}: {}", LOG_ERROR, message);
            throw new ValidationException(message);
        }
        if (user.getBirthday().isAfter(LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault()))) {
            String message = "Дата рождения не может быть в будущем";
            log.info("{}: {}", LOG_ERROR, message);
            throw new ValidationException(message);
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        user.setId(++idCount);
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        if (user.getId() == 0) {
            String message = "Введите свой id";
            log.info("{}: {}", LOG_ERROR, message);
            throw new ValidationException(message);
        }

        if (!users.containsKey(user.getId())) {
            String message = "Пользователь с таким id не найден";
            log.info("{}: {}", LOG_ERROR, message);
            throw new NotFoundException(message);
        }

        User oldUser = users.get(user.getId());

        if (user.getName() == null) {
            user.setName(oldUser.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(oldUser.getEmail());
        }
        if (user.getBirthday() == null) {
            user.setBirthday(oldUser.getBirthday());
        }
        if (user.getLogin() == null) {
            user.setLogin(oldUser.getLogin());
        }

        users.put(user.getId(), user);
        return user;
    }

}
