package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage storage;

    @Autowired
    public FilmService(FilmStorage storage) {
        this.storage = storage;
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
        film.getLikes().add(userId);
        storage.update(film);
        return film;
    }

    public Film removeLike(long id, long userId) {
        Film film = find(id);
        film.getLikes().remove(userId);
        update(film);
        return film;
    }

    public Collection<Film> findPopular(long count) {
        Collection<Film> films = findAll();
        Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());
        Collection<Film> popularFilms = films.stream().sorted(comparator.reversed()).limit(count).toList();
        return popularFilms;
    }
}
