package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private static final LocalDate EARLIEST_DATE = LocalDate.of(1895, 12, 28);
    private int idCount = 0;
    private static final String LOG_ERROR = "User Input Error:";
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
            return films.values();
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        if (film.getId() != 0) {
            throw new ValidationException("Если вы хотите обновить фильм, воспользуйтесь методом PUT");
        }
        if (film.getDescription().length() > 200) {
            String message = "Описание не должно содержать более 200 символов";
            log.info("{}: {}", LOG_ERROR, message);
            throw new ValidationException(message);
        }
        if (film.getReleaseDate().isBefore(EARLIEST_DATE)) {
            String message = "Дата релиза не может быть раньше 28 декабря 1895 года";
            log.info("{}: {}", LOG_ERROR, message);
            throw new ValidationException(message);
        }
        if (film.getDuration() <= 0) {
            String message = "Продолжительность фильма должна быть положительным числом";
            log.info("{}: {}", LOG_ERROR, message);
            throw new ValidationException(message);
        }

        film.setId(++idCount);
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == 0) {
            String message = "Введите id фильма, который вы хотите обновить";
            log.info("{}: {}", LOG_ERROR, message);
            throw new ValidationException(message);
        }
        if (!films.containsKey(film.getId())) {
            String message = "Фильм с таким id не найден";
            log.info("{}: {}", LOG_ERROR, message);
            throw new NotFoundException(message);
        }

        Film oldFIlm = films.get(film.getId());

        if (film.getName() == null) {
            film.setName(oldFIlm.getName());
        }
        if (film.getDescription() == null) {
            film.setDescription(oldFIlm.getDescription());
        }
        if (film.getDuration() == 0) {
            film.setDuration(oldFIlm.getDuration());
        }
        if (film.getReleaseDate() == null) {
            film.setReleaseDate(oldFIlm.getReleaseDate());
        }

        films.put(film.getId(), film);
        return film;
    }
}