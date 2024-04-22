package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * User.
 */
@Data
@Builder
public class User {
    int id;

    @NotNull
    @NotBlank
    @Email
    String email;

    @NotNull
    @NotBlank
    String login;

    String name;

    @PastOrPresent
    LocalDate birthday;
}
