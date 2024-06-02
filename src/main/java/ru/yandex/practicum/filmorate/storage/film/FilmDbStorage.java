package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalErrorException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Like;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.*;

@Repository("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    JdbcTemplate jdbc;
    UserService userService;
    FilmRowMapper filmRowMapper;
    RatingRowMapper ratingRowMapper;
    GenreRowMapper genreRowMapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc, UserService userService, FilmRowMapper filmRowMapper, RatingRowMapper ratingRowMapper, GenreRowMapper genreRowMapper) {
        this.jdbc = jdbc;
        this.userService = userService;
        this.filmRowMapper = filmRowMapper;
        this.ratingRowMapper = ratingRowMapper;
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public Collection<Film> findAll() {
        Collection<Film> films = jdbc.query("SELECT * FROM FILM", filmRowMapper);
        Collection<Film> filmsWithRatingAndLikes = films.stream()
                .peek(film -> film.setMpa(jdbc.queryForObject("SELECT * FROM RATING WHERE RATING_ID = ?",
                        ratingRowMapper, jdbc.queryForObject("SELECT RATING_ID FROM FILM WHERE FILM_ID = ?", Long.class, film.getId()))))
                .peek(film -> film.setLikes(new HashSet<>(jdbc.queryForList("SELECT * FROM LIKES WHERE FILM_ID = ?", Long.class, film.getId()))))
                .toList();

        Collection<Film> result = filmsWithRatingAndLikes.stream().peek(film -> film.setGenres(new HashSet<>(jdbc.query("SELECT g.* FROM GENRE g INNER JOIN FILM_GENRE fg ON g.GENRE_ID = fg.GENRE_ID WHERE fg.FILM_ID = ?",
                genreRowMapper, film.getId())))).toList();
        log.info("Выполняется возврат всех фильмов из БД");
        return result;
    }

    @Override
    public Film find(long id) {
        try {
            Film film = jdbc.queryForObject("SELECT * FROM FILM WHERE FILM_ID = ?", filmRowMapper, id);
            film.setMpa(jdbc.queryForObject("SELECT * FROM RATING WHERE RATING_ID = ?",
                    ratingRowMapper, jdbc.queryForObject("SELECT RATING_ID FROM FILM WHERE FILM_ID = ?", Long.class, id)));
            film.setLikes(new HashSet<>(jdbc.queryForList("SELECT * FROM LIKES WHERE FILM_ID = ?", Long.class, id)));
            film.setGenres(new HashSet<>(jdbc.query("SELECT g.* FROM GENRE g INNER JOIN FILM_GENRE fg ON g.GENRE_ID = fg.GENRE_ID WHERE fg.FILM_ID = ?",
                    genreRowMapper, id)));
            log.info("Выполняется возврат фильма с id {} из БД", id);
            return film;
        } catch (EmptyResultDataAccessException e) {
            log.error("Пользователь попытался найти несуществующий фильм");
            throw new NotFoundException("Не удалось найти фильм");
        }
    }

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc).withTableName("film").usingGeneratedKeyColumns("film_id");
        long id = simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue();
        if (id != 0) {
            log.info("Новый фильм с id {} сохранен", id);
            return find(id);
        } else {
            log.error("Произошла ошибка при попытке сохранить фильм");
            throw new InternalErrorException("Не удалось сохранить данные");
        }
    }

    @Override
    public Film update(Film film) {
        try {
            find(film.getId());
            int rowsUpdated = jdbc.update("UPDATE FILM SET DURATION = ?, RELEASE_DATE = ?, DESCRIPTION = ?, NAME = ?, RATING_ID = ? WHERE FILM_ID = ?", film.getDuration(), film.getReleaseDate(), film.getDescription(), film.getName(), film.getId());
            if (rowsUpdated == 0) {
                throw new InternalErrorException("Не удалось обновить данные");
            }

            log.info("Информация о фильме с id {} обновлена", film.getId());
            return find(film.getId());
        } catch (EmptyResultDataAccessException e) {
            log.error("Фильма с id {} еще нет", film.getId());
            throw new NotFoundException("Ошибка: Фильм с id" + film.getId() + "не найден");
        }
    }

    @Override
    public Film like(long id, long userId) {
        try {
            find(id);
            userService.find(userId);
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc).withTableName("likes");
            long likeId = simpleJdbcInsert.executeAndReturnKey(Like.builder().filmId(id).userId(userId).build().toMap()).longValue();

            if (likeId != 0) {
                log.info("Пользователь с id {} лайкнул фильм с id {}", userId, id);
                return find(id);
            } else {
                log.error("Произошла ошибка при попыткеп пользователя с id {} лайкнуть фильм с id {}", userId, id);
                throw new InternalErrorException("Не удалось лайкнуть фильм");
            }
        } catch (EmptyResultDataAccessException e) {
            log.error("Фильм или пользователь не были найдены при попытке лайкнуть фильм");
            throw new NotFoundException("Фильм или пользователь не были найдены");
        }
    }

    @Override
    public Film removeLike(long id, long userId) {
        try {
            find(id);
            userService.find(userId);
            jdbc.update("DELETE FROM LIKES WHERE FILM_ID = ? AND USER_ID = ?", id, userId);
            log.info("Пользователь с id {} удалил лайк с фильма с id {}", userId, id);
            return find(id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Ошибка: не удалось удалить данные");
            throw new NotFoundException("Ошибка: не удалось удалить данные");
        }
    }

    @Override
    public Collection<Film> findPopular(long count) {
        Collection<Film> films = findAll();
        Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());
        Collection<Film> popularFilms = films.stream().sorted(comparator.reversed()).limit(count).toList();
        log.info("Выполняется возврат самых популярных фильмов");
        return popularFilms;
    }
}
