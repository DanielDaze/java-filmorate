package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder
public class Film {
    private int id;

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
}
