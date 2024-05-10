package ru.yandex.practicum.filmorate.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 */
@Data
public class User {
    private long id;

    @NotNull
    @NotBlank
    @Email
    private String email;

    @IsValidLogin
    private String login;

    private String name;

    @PastOrPresent
    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();

    @Builder
    public User(long id, LocalDate birthday, String name, String login, String email) {
        this.id = id;
        this.birthday = birthday;
        this.name = name;
        this.login = login;
        this.email = email;
    }
}
