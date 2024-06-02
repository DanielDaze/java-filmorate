package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("inMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private int idCount = 0;
    private static final String INPUT_ERROR = "User Input Error";

    @Override
    public Collection<User> findAll() {
        log.info("Выполняется возврат списка всех пользователей");
        return users.values();
    }

    @Override
    public User find(long id) {
        if (users.containsKey(id)) {
            log.info("Пользователь с id {} найден", id);
            return users.get(id);
        } else {
            String message = "Пользователь с id " + id + " не найден";
            log.error("{}: {}", INPUT_ERROR, message);
            throw new NotFoundException(message);
        }
    }

    @Override
    public User add(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        user.setId(++idCount);
        users.put(user.getId(), user);
        log.info("Пользователь {} добавлен", user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            String message = "Пользователь с id " + user.getId() + " не найден";
            log.error("{}: {}", INPUT_ERROR, message);
            throw new NotFoundException(message);
        }

        User oldUser = users.get(user.getId());

        if (user.getName() == null) {
            user.setName(oldUser.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(oldUser.getEmail());
        }
        if (user.getBirthday() == null) {
            user.setBirthday(oldUser.getBirthday());
        }
        if (user.getLogin() == null) {
            user.setLogin(oldUser.getLogin());
        }
        if (user.getFriends() == null) {
            user.setFriends(oldUser.getFriends());
        }

        users.put(user.getId(), user);
        log.info("Пользователь с id {} обновлен: {}", user.getId(), user);
        return user;
    }

    @Override
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
