package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.WrongMethodException;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private int idCount = 0;
    @Value("${filmorate.error-message.input-error}")
    private String INPUT_ERROR;
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film find(long id) {
        return films.get(id);
    }

    @Override
    public Film add(Film film) {
        if (film.getId() != 0) {
            throw new WrongMethodException("Если вы хотите обновить фильм, воспользуйтесь методом PUT");
        }

        film.setId(++idCount);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == 0) {
            String message = "Введите id фильма, который вы хотите обновить";
            log.info("{}: {}", INPUT_ERROR, message);
            throw new NotFoundException(message);
        }
        if (!films.containsKey(film.getId())) {
            String message = "Фильм с таким id не найден";
            log.info("{}: {}", INPUT_ERROR, message);
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
