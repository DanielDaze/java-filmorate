package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalErrorException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.WrongArgumentException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Like;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.*;

@Repository("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final UserService userService;
    private final GenreService genreService;
    private final FilmRowMapper filmRowMapper;
    private final RatingRowMapper ratingRowMapper;
    private final GenreRowMapper genreRowMapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc, UserService userService, GenreService genreService, FilmRowMapper filmRowMapper, RatingRowMapper ratingRowMapper, GenreRowMapper genreRowMapper) {
        this.jdbc = jdbc;
        this.userService = userService;
        this.genreService = genreService;
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
                .peek(film -> film.setLikes(new HashSet<>(jdbc.queryForList("SELECT USER_ID FROM LIKES WHERE FILM_ID = ?", Long.class, film.getId()))))
                .toList();

        Collection<Film> result = filmsWithRatingAndLikes.stream().peek(film -> film.setGenres(new HashSet<>(jdbc.query("SELECT g.* FROM GENRE g INNER JOIN FILM_GENRE fg ON g.GENRE_ID = fg.GENRE_ID WHERE fg.FILM_ID = ?",
                genreRowMapper, film.getId())))).toList();
        log.info("Выполняется возврат всех фильмов из БД");
        return result;
    }

    @Override
    public Film findById(long id) {
        try {
            Film film = jdbc.queryForObject("SELECT * FROM FILM WHERE FILM_ID = ?", filmRowMapper, id);
            film.setMpa(jdbc.queryForObject("SELECT * FROM RATING WHERE RATING_ID = ?",
                    ratingRowMapper, jdbc.queryForObject("SELECT RATING_ID FROM FILM WHERE FILM_ID = ?", Long.class, id)));
            film.setLikes(new HashSet<>(jdbc.queryForList("SELECT USER_ID FROM LIKES WHERE FILM_ID = ?", Long.class, id)));
            film.setGenres(new TreeSet<>(jdbc.query("SELECT g.* FROM GENRE g INNER JOIN FILM_GENRE fg ON g.GENRE_ID = fg.GENRE_ID WHERE fg.FILM_ID = ?",
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
        try {
            jdbc.queryForObject("SELECT * FROM RATING WHERE RATING_ID = ?", ratingRowMapper, film.getMpa().getId());

            Set<Genre> genres = film.getGenres();
            for (Genre genre : genres) {
                jdbc.queryForObject("SELECT * FROM GENRE WHERE GENRE.GENRE_ID = ?", genreRowMapper, genre.getId());
            }
        } catch (EmptyResultDataAccessException e) {
            log.info("Рейтинга с id {} еще нет", film.getMpa().getId());
            throw new WrongArgumentException("Рейтинга с таким id еще нет");
        }

        try {
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc).withTableName("film").usingGeneratedKeyColumns("film_id");
            long id = simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue();

            SimpleJdbcInsert insertGenres = new SimpleJdbcInsert(jdbc).withTableName("film_genre");
            Set<Genre> genres = film.getGenres();
            for (Genre genre : genres) {
                genreService.findById(genre.getId());
                Map<String, Object> genreMap = new HashMap<>();
                genreMap.put("film_id", id);
                genreMap.put("genre_id", genre.getId());
                insertGenres.execute(genreMap);
            }

            if (id != 0) {
                log.info("Новый фильм с id {} сохранен", id);
                return findById(id);
            } else {
                log.error("Произошла ошибка при попытке сохранить фильм");
                throw new InternalErrorException("Не удалось сохранить данные");
            }
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Нужного элемента нет в базе данных");
        }
    }

    @Override
    public Film update(Film film) {
        try {
            findById(film.getId());
            int rowsUpdated = jdbc.update("UPDATE FILM SET DURATION = ?, RELEASE_DATE = ?, DESCRIPTION = ?, NAME = ?, RATING_ID = ? WHERE FILM_ID = ?", film.getDuration(), film.getReleaseDate(), film.getDescription(), film.getName(), film.getMpa().getId(), film.getId());
            if (rowsUpdated == 0) {
                throw new InternalErrorException("Не удалось обновить данные");
            }

            log.info("Информация о фильме с id {} обновлена", film.getId());
            return findById(film.getId());
        } catch (EmptyResultDataAccessException e) {
            log.error("Фильма с id {} еще нет", film.getId());
            throw new NotFoundException("Ошибка: Фильм с id" + film.getId() + "не найден");
        }
    }

    @Override
    public Film like(long id, long userId) {
        try {
            findById(id);
            userService.findById(userId);
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc).withTableName("likes");
            long updated = simpleJdbcInsert.execute(Like.builder().filmId(id).userId(userId).build().toMap());

            if (updated != 0) {
                log.info("Пользователь с id {} лайкнул фильм с id {}", userId, id);
                return findById(id);
            } else {
                log.error("Произошла ошибка при попытке пользователя с id {} лайкнуть фильм с id {}", userId, id);
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
            findById(id);
            userService.findById(userId);
            jdbc.update("DELETE FROM LIKES WHERE FILM_ID = ? AND USER_ID = ?", id, userId);
            log.info("Пользователь с id {} удалил лайк с фильма с id {}", userId, id);
            return findById(id);
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
