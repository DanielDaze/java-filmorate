package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalErrorException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.FriendStatus;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.mappers.FriendStatusRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.util.*;

@Repository("userDbStorage")
@Slf4j
public class  UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;
    private final FriendStatusRowMapper friendStatusRowMapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String UPDATE_QUERY = "UPDATE users SET birthday = ?, name = ?, login = ?, email = ? WHERE user_id = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friend (user_id, second_user_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friend WHERE user_id = ? AND second_user_id = ?";
    private static final String FIND_FRIENDS_QUERY = "SELECT * FROM friend WHERE user_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper userRowMapper, FriendStatusRowMapper friendStatusRowMapper) {
        this.jdbc = jdbc;
        this.userRowMapper = userRowMapper;
        this.friendStatusRowMapper = friendStatusRowMapper;
    }

    public Collection<User> findAll() {
        Collection<User> users = jdbc.query(FIND_ALL_QUERY, userRowMapper);
        Collection<User> usersWithFriends = users.stream().peek(user -> user.setFriends(new HashSet<>(findFriends(user.getId()).stream().map(User::getId).toList()))).toList();
        log.info("Выполняется возврат всех пользователей из БД");
        return usersWithFriends;
    }

    public User find(long id) {
        try {
            User result = jdbc.queryForObject(FIND_BY_ID_QUERY, userRowMapper, id);
            List<Long> friendsIds = jdbc.queryForList("SELECT SECOND_USER_ID FROM FRIEND WHERE user_id = ?", Long.class, id);
            result.setFriends(new HashSet<>(friendsIds));
            log.info("Выполняется возврат пользователя c id {} из БД", id);
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.error("Пользователь с id {} не найден", id);
            throw new NotFoundException("Ошибка: Пользователь с id" + id + "не найден");
        }
    }

    public User add(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        long id = simpleJdbcInsert.executeAndReturnKey(user.toMap()).longValue();
        if (id != 0) {
            log.info("Новый пользователь с id {} сохранен", id);
            return find(id);
        } else {
            throw new InternalErrorException("Не удалось сохранить данные");
        }
    }

    public User update(User user) {
        try {
            find(user.getId());
            int rowsUpdated = jdbc.update(UPDATE_QUERY, user.getBirthday(), user.getName(), user.getLogin(), user.getEmail(), user.getId());
            if (rowsUpdated == 0) {
                throw new InternalErrorException("Не удалось обновить данные");
            }

            log.info("Информация о пользователе с id {} обновлена", user.getId());
            return find(user.getId());
        } catch (EmptyResultDataAccessException e) {
            log.error("Пользователя с id {} еще нет", user.getId());
            throw new NotFoundException("Ошибка: Пользователь с id" + user.getId() + "не найден");
        }
    }

    public User addFriend(long id, long friendId) {
        try {
            find(id);
            find(friendId);
            jdbc.update(ADD_FRIEND_QUERY, id, friendId);
            log.info("Пользователь с id {} добавил в друзья пользователя с id {}", id, friendId);
            return find(id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Ошибка: не удалось сохранить данные");
            throw new NotFoundException("Ошибка: не удалось сохранить данные");
        }
    }

    public User deleteFriend(long id, long friendId) {
        try {
            find(id);
            find(friendId);
            jdbc.update(DELETE_FRIEND_QUERY, id, friendId);
            log.info("Пользователь с id {} удалил из друзей пользователя с id {}", id, friendId);
            return find(id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Ошибка: не удалось удалить данные");
            throw new NotFoundException("Ошибка: не удалось удалить данные");
        }
    }

    public Collection<User> findFriends(long id) {
        try {
            jdbc.queryForObject(FIND_BY_ID_QUERY, userRowMapper, id);
            List<FriendStatus> friendsIds = jdbc.query(FIND_FRIENDS_QUERY, friendStatusRowMapper, id);
            Collection<User> result = friendsIds.stream()
                    .map(friendStatus -> find(friendStatus.getFriendId()))
                    .toList();
            log.info("Выполняется возврат друзей пользователя с id {}", id);
            return result;
        } catch (EmptyResultDataAccessException e) {
            log.error("Ошибка: не удалось найти данные");
            throw new NotFoundException("Ошибка: не удалось найти данные");
        }
    }

    public Collection<User> findMutuals(long id, long otherId) {
        List<FriendStatus> userFriendsIds = jdbc.query(FIND_FRIENDS_QUERY, friendStatusRowMapper, id);
        List<FriendStatus> otherFriendsIds = jdbc.query(FIND_FRIENDS_QUERY, friendStatusRowMapper, otherId);

        List<User> userFriends = userFriendsIds.stream()
                .map(friendStatus -> find(friendStatus.getFriendId()))
                .toList();
        List<User> otherFriends = otherFriendsIds.stream()
                .map(friendStatus -> find(friendStatus.getFriendId()))
                .toList();

        Collection<User> result = userFriends.stream()
                .distinct()
                .filter(otherFriends::contains)
                .toList();
        return result;
    }
}
