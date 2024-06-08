package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class UserService {
    private final UserStorage storage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage storage) {
        this.storage = storage;
    }

    public Collection<User> findAll() {
        return storage.findAll();
    }

    public User findById(long id) {
        return storage.findById(id);
    }

    public User add(User user) {
        return storage.add(user);
    }

    public User update(User user) {
        return storage.update(user);
    }

    public User addFriend(long id, long friendId) {
        return storage.addFriend(id, friendId);
    }

    public User deleteFriend(long id, long friendId) {
        return storage.deleteFriend(id, friendId);
    }

    public Collection<User> findFriends(long id) {
        return storage.findFriends(id);
    }

    public Collection<User> findMutuals(long id, long otherId) {
        return storage.findMutuals(id, otherId);
    }
}