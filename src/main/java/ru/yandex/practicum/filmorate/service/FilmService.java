package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage storage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage storage, UserService userService) {
        this.storage = storage;
        this.userService = userService;
    }

    public Collection<Film> findAll() {
        return storage.findAll();
    }

    public Film find(long id) {
        return storage.find(id);
    }

    public Film add(Film film) {
        return storage.add(film);
    }

    public Film update(Film film) {
        return storage.update(film);
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
