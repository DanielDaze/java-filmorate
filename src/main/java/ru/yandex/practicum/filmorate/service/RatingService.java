package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.storage.RatingDbStorage;

import java.util.Collection;

@Service
public class RatingService {
    private final RatingDbStorage storage;

    @Autowired
    public RatingService(RatingDbStorage storage) {
        this.storage = storage;
    }

    public Collection<Rating> findAll() {
        return storage.findAll();
    }

    public Rating find(long id) {
        return storage.find(id);
    }

}
