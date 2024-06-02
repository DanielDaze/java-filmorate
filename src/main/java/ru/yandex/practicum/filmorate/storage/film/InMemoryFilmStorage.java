package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.WrongMethodException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Component("inMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    UserService userService;

    private int idCount = 0;
    private static final String INPUT_ERROR = "User Input Error";
    private final Map<Long, Film> films = new HashMap<>();

    @Autowired
    public InMemoryFilmStorage(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Выполняется возврат списка всех фильмов");
        return films.values();
    }

    @Override
    public Film find(long id) {
        if (!films.containsKey(id)) {
            String message = "Фильм c id" + id + " не найден";
            log.info("{}: {}", INPUT_ERROR, message);
            throw new NotFoundException(message);
        }
        log.info("Фильм с id {} найден", id);
        return films.get(id);
    }

    @Override
    public Film add(Film film) {
        if (film.getId() != 0) {
            throw new WrongMethodException("Если вы хотите обновить фильм, воспользуйтесь методом PUT");
        }

        film.setId(++idCount);
        films.put(film.getId(), film);
        log.info("Фильм {} добавлен", film);
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
            String message = "Фильм c id" + film.getId() + " не найден";
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
        log.info("Фильм с id {} обновлен: {}", film.getId(), film);
        return film;
    }

    public Film like(long id, long userId) {
        Film film = find(id);
        User user = userService.find(userId);
        film.getLikes().add(userId);
        update(film);
        log.info("Пользователь {} лайкнул фильм {}", user.getLogin(), film.getName());
        return film;
    }

    public Film removeLike(long id, long userId) {
        Film film = find(id);
        if (!film.getLikes().contains(userId)) {
            log.error("Пользователь с id {} хотел убрать лайк с фильма, который еще не был оценен", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не лайкал этот фильм");
        }

        film.getLikes().remove(userId);
        update(film);
        log.info("Пользователь с id {} снял лайк с фильма {}", userId, film.getName());
        return film;

    }

    public Collection<Film> findPopular(long count) {
        Collection<Film> films = findAll();
        Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());
        Collection<Film> popularFilms = films.stream().sorted(comparator.reversed()).limit(count).toList();
        log.info("Выполняется возврат списка самых популярных фильмов");
        return popularFilms;
    }
}
