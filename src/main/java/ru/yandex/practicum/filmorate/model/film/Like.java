package ru.yandex.practicum.filmorate.model.film;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Like {
    private long filmId;
    private long userId;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("film_id", filmId);
        values.put("user_id", userId);
        return values;
    }
}
