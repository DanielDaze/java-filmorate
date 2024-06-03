package ru.yandex.practicum.filmorate.model.film;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Genre implements Comparable<Genre> {
    private final long id;
    private String name;

    @Builder
    public Genre(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int compareTo(Genre o) {
        return (int) (this.getId() - o.getId());
    }
}
