package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film find(long id);

    Film add(Film film);

    Film update(Film film);

    Film like(long id, long userId);

    Film removeLike(long id, long userId);

    Collection<Film> findPopular(long count);
}
