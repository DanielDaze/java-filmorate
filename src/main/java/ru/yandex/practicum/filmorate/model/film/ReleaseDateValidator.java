package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<IsValidReleaseDate, LocalDate> {
    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext context) {
        LocalDate earliestReleaseDate = LocalDate.of(1895, 12, 28);
        return releaseDate.isAfter(earliestReleaseDate) || releaseDate.isEqual(earliestReleaseDate);
    }
}
