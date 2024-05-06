package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
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

    private Set<Long> likes = new HashSet<>();

    @Builder
    public Film(long id, int duration, LocalDate releaseDate, String description, String name) {
        this.id = id;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.description = description;
        this.name = name;
    }
}
