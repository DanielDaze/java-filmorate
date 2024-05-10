package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.user.User;

import java.util.Collection;

public interface UserStorage {
    public Collection<User> findAll();

    public User find(long id);

    public User add(User user);

    public User update(User user);
}
