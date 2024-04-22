package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder
public class Film {
    int id;

    @NotNull
    @NotBlank
    String name;
    String description;

    @PastOrPresent
    LocalDate releaseDate;

    Duration duration;
}
