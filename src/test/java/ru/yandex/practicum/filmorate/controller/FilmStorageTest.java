package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FilmStorageTest {
    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;
    @Autowired
    private UserDbStorage userDbStorage;

    @Test
    @Order(1)
    void postAndGetTest() {
        Film film = Film.builder()
                        .name("name")
                                .description("desc")
                                        .duration(120)
                                                .releaseDate(LocalDate.of(2000, 10, 10))
                                                        .mpa(new Rating(1))
                                                            .build();
        film.getGenres().add(new Genre(1));

        Film expectedFilm = Film.builder()
                .id(1)
                .name("name")
                .description("desc")
                .duration(120)
                .releaseDate(LocalDate.of(2000, 10, 10))
                .mpa(new Rating(1, "G"))
                .build();
        expectedFilm.getGenres().add(new Genre(1, "Комедия"));

        Film returnedfilm = filmStorage.add(film);
        Assertions.assertEquals(expectedFilm, returnedfilm);
    }

    @Test
    @Order(2)
    void updateTest() {
        Film film = Film.builder()
                .id(1)
                .name("name")
                .description("desc")
                .duration(120)
                .releaseDate(LocalDate.of(2000, 10, 10))
                .mpa(new Rating(2))
                .build();
        Film expectedFilm = Film.builder()
                .id(1)
                .name("name")
                .description("desc")
                .duration(120)
                .releaseDate(LocalDate.of(2000, 10, 10))
                .mpa(new Rating(2, "PG"))
                .build();
        expectedFilm.getGenres().add(new Genre(1, "Комедия"));
        Assertions.assertEquals(expectedFilm, filmStorage.update(film));
    }

    @Test
    @Order(3)
    void getAllTest() {
        Assertions.assertEquals(1, filmStorage.findAll().size());
    }

    @Test
    @Order(4)
    void addLikeTest() {
        User user = User.builder()
                .name("name")
                .login("login")
                .email("login@yandex.ru")
                .birthday(LocalDate.of(1999, 12, 21)).build();
        userStorage.add(user);

        filmStorage.like(1, 1);
        Assertions.assertEquals(1, filmStorage.find(1).getLikes().size());
    }

    @Test
    @Order(5)
    void removeLikeTest() {
        filmStorage.removeLike(1, 1);
        Assertions.assertEquals(0, filmStorage.find(1).getLikes().size());
    }

    @Test
    @Order(6)
    void getPopularTest() {
        User user = User.builder()
                .name("name")
                .login("login")
                .email("login@yandex.ru")
                .birthday(LocalDate.of(1999, 12, 21)).build();
        userStorage.add(user);
        userStorage.add(user);
        userStorage.add(user);

        Film film = Film.builder()
                .name("name")
                .description("desc")
                .duration(120)
                .releaseDate(LocalDate.of(2000, 10, 10))
                .mpa(new Rating(1))
                .build();
        film.getGenres().add(new Genre(1));
        filmStorage.add(film);
        filmStorage.add(film);
        filmStorage.add(film);

        filmStorage.like(1, 1);
        filmStorage.like(1, 2);
        filmStorage.like(1, 3);
        filmStorage.like(1, 4);

        filmStorage.like(2, 1);
        filmStorage.like(2, 2);
        filmStorage.like(2, 3);

        filmStorage.like(3, 1);
        filmStorage.like(3, 2);

        Collection<Film> popularFilms = filmStorage.findPopular(3);
        List<Film> filmsList = new ArrayList<>(popularFilms);
        Assertions.assertEquals(4, filmsList.get(0).getLikes().size());
        Assertions.assertEquals(3, filmsList.get(1).getLikes().size());
    }

}
