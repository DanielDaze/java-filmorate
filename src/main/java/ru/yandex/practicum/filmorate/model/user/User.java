package ru.yandex.practicum.filmorate.model.user;

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
    private int id;

    @NotNull
    @NotBlank
    @Email
    private String email;

    @IsValidLogin
    private String login;

    private String name;

    @PastOrPresent
    private LocalDate birthday;
}
