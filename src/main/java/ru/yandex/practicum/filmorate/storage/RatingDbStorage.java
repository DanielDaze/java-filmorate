package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.Collection;

@Repository
@Slf4j
public class RatingDbStorage {
    private final JdbcTemplate jdbc;
    private final RatingRowMapper ratingRowMapper;

    public RatingDbStorage(JdbcTemplate jdbc, RatingRowMapper ratingRowMapper) {
        this.jdbc = jdbc;
        this.ratingRowMapper = ratingRowMapper;
    }

    public Collection<Rating> findAll() {
        log.info("Выполняется возврат всех рейтингов из БД");
        return jdbc.query("SELECT * FROM RATING", ratingRowMapper);
    }

    public Rating findById(long id) {
        try {
            Rating rating = jdbc.queryForObject("SELECT * FROM RATING WHERE RATING_ID = ?", ratingRowMapper, id);
            log.info("Выполняется возврат рейтинга с id {} из БД", id);
            return rating;
        } catch (EmptyResultDataAccessException e) {
            log.info("Рейтинга с id {} еще нет", id);
            throw new NotFoundException("Рейтинга с таким id еще нет");
        }
    }
}
