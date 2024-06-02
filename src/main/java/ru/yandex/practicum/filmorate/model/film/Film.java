package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class Film {
    private long id;

    @NotNull
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;

    @PastOrPresent
    @IsValidReleaseDate
    private LocalDate releaseDate;

    @Positive
    private int duration;

    private Rating mpa;

    private Set<Long> likes = new HashSet<>();

    private Set<Genre> genres = new HashSet<>();

    @Builder
    public Film(long id, int duration, LocalDate releaseDate, String description, String name, Rating mpa) {
        this.id = id;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.description = description;
        this.name = name;
        this.mpa = mpa;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("duration", duration);
        values.put("name", name);
        values.put("description", description);
        values.put("release_date", releaseDate);
        values.put("rating", mpa);
        return values;
    }
}
