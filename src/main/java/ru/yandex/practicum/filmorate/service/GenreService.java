package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.Collection;

@Service
public class GenreService {
    private final GenreDbStorage storage;

    @Autowired
    public GenreService(GenreDbStorage storage) {
        this.storage = storage;
    }

    public Collection<Genre> findAll() {
        return storage.findAll();
    }

    public Genre find(long id) {
        return storage.find(id);
    }
}
