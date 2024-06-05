package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.user.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> findAll();

    User findById(long id);

    User add(User user);

    User update(User user);

    User addFriend(long id, long friendId);

    User deleteFriend(long id, long friendId);

    Collection<User> findFriends(long id);

    Collection<User> findMutuals(long id, long otherId);
}
