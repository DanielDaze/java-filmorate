package ru.yandex.practicum.filmorate.model.film;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Rating {
    private long id;
    private String name;
}
