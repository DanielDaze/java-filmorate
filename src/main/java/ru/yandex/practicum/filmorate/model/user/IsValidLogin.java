package ru.yandex.practicum.filmorate.model.user;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoginValidator.class)
public @interface IsValidLogin {
    String message() default "Логин не может быть пустым или содержать пробелы";
    Class<?>[] groups() default {};
    Class<? extends Payload> [] payload() default {};
}
