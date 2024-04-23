package ru.yandex.practicum.filmorate.model.user;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LoginValidator implements ConstraintValidator<IsValidLogin, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !(value == null || value.contains(" ") || value.isBlank());
    }
}
