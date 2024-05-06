package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private int idCount = 0;
    @Value("${filmorate.error-message.input-error}")
    private String INPUT_ERROR;

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User find(long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            String message = "Пользователь с id " + id + " не найден";
            log.info("{}: {}", INPUT_ERROR, message);
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
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            String message = "Пользователь с id " + user.getId() + " не найден";
            log.info("{}: {}", INPUT_ERROR, message);
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
        return user;
    }
}
