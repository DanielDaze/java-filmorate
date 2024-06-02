package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;

@Repository
@Slf4j
public class GenreDbStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreRowMapper;

    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper genreRowMapper) {
        this.jdbc = jdbc;
        this.genreRowMapper = genreRowMapper;
    }

    public Collection<Genre> findAll() {
        log.info("Выполняется возврат всех жанров из БД");
        return jdbc.query("SELECT * FROM GENRE", genreRowMapper);
    }

    public Genre find(long id) {
        try {
            Genre genre = jdbc.queryForObject("SELECT * FROM GENRE WHERE GENRE.GENRE_ID = ?", genreRowMapper, id);
            log.info("Выполняется возврат Жанра с id {}", id);
            return genre;
        } catch (EmptyResultDataAccessException e) {
            log.info("Жанра с id {} еще нет", id);
            throw new NotFoundException("Жанра с таким id еще нет");
        }
    }

}
