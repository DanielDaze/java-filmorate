package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage storage;

    @Autowired
    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public Collection<User> findAll() {
        return storage.findAll();
    }

    public User find(long id) {
        return storage.find(id);
    }

    public User add(User user) {
        return storage.add(user);
    }

    public User update(User user) {
        return storage.update(user);
    }

    public User addFriend(long id, long friendId) {
        User user = find(id);
        User friend = find(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        update(user);
        update(friend);
        log.info("Пользователь {} добавил в друзья пользователя {}", user.getLogin(), friend.getLogin());
        return user;
    }

    public User deleteFriend(long id, long friendId) {
        User user = find(id);
        User friend = find(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        update(user);
        update(friend);
        log.info("Пользователь {} удалил из друзей пользователя {}", user.getLogin(), friend.getLogin());
        return user;
    }

    public Collection<User> findFriends(long id) {
        User neededUser = find(id);
        Collection<User> users = findAll();
        Collection<User> friends = users.stream().filter(curUser -> curUser.getFriends().contains(id)).toList();
        log.info("Выполняется возврат списка друзей пользователя {}", neededUser.getLogin());
        return friends;
    }

    public Collection<User> findMutuals(long id, long otherId) {
        Collection<User> users = findAll();
        User user = find(id);
        User otherUser = find(otherId);
        List<Long> mutualIds = user.getFriends().stream().filter(otherUser.getFriends()::contains).toList();
        Collection<User> mutualFriends = users.stream().filter(curUser -> mutualIds.contains(curUser.getId())).toList();
        log.info("Выполняется возврат общих друзей пользователей {} и {}", user.getLogin(), otherUser.getLogin());
        return mutualFriends;
    }
}
