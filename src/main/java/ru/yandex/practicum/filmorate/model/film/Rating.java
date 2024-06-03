package ru.yandex.practicum.filmorate.model.film;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Rating {
    private final long id;
    private String name;

    @Builder
    public Rating(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
