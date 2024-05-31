package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.user.FriendStatus;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FriendStatusRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository()
public class UserDbStorage extends BaseDbStorage<User> {
    private final FriendStatusRowMapper friendStatusRowMapper;
    private final Class<FriendStatus> friendStatusClass;

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users (birthday, name, login, email) VALUES (?, ?, ?, ?) RETURNING user_id";
    private static final String UPDATE_QUERY = "UPDATE users SET birthday = ?, name = ?, login = ?, email = ? WHERE user_id = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friend (user_id, second_user_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friend WHERE user_id = ? AND second_user_id = ?";
    private static final String FIND_FRIENDS_QUERY = "SELECT * FROM friend WHERE user_id = ? AND confirmed = true";

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper, FriendStatusRowMapper friendStatusRowMapper) {
        super(jdbc, mapper, User.class);
        this.friendStatusRowMapper = friendStatusRowMapper;
        this.friendStatusClass = FriendStatus.class;
    }

    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<User> find(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public User add(User user) throws SQLException {
        long id = insert(INSERT_QUERY, user.getBirthday(), user.getName(), user.getLogin(), user.getEmail());
        user.setId(id);
        return user;
    }

    public User update(User user) throws SQLException {
        update(UPDATE_QUERY, user.getBirthday(), user.getName(), user.getLogin(), user.getEmail(), user.getId());
        return user;
    }

    public User addFriend(long id, long friend_id) throws SQLException {
        insert(ADD_FRIEND_QUERY, id, friend_id);

        Optional<User> maybeUser = find(id);
        Optional<User> maybeFriend = find(friend_id);
        if (maybeUser.isPresent() && maybeFriend.isPresent()) {
            User user = maybeUser.get();
            User friend = maybeFriend.get();

            if (user.getFriends().contains(friend_id) || friend.getFriends().contains(id)) {
                update("UPDATE friend SET confirmed = TRUE WHERE user_id = ? AND second_user_id = ?", id, friend_id);
                update("UPDATE friend SET confirmed = TRUE WHERE user_id = ? AND second_user_id = ?", friend_id, id);
            }
        }
        return findOne(FIND_BY_ID_QUERY, id).get();
    }

    public User deleteFriend(long id, long friendId) throws SQLException {
        int rowsUpdated = jdbc.update(DELETE_FRIEND_QUERY, id, friendId);
        if (rowsUpdated == 0) {
            throw new SQLException("Не удалось обновить данные");
        }

        int rowsUpdatedFriend = jdbc.update("UPDATE friend SET confirmed = FALSE WHERE user_id = ? AND second_user_id = ?", friendId, id);
        if (rowsUpdatedFriend == 0) {
            throw new SQLException("Не удалось обновить данные");
        }

        return findOne(FIND_BY_ID_QUERY, id).get();
    }

    public Collection<User> findFriends(long id) {
        List<FriendStatus> friendsIds = jdbc.queryForList(FIND_FRIENDS_QUERY, friendStatusClass, id);
        Collection<User> result = friendsIds.stream()
                .map(friendStatus -> findOne(FIND_BY_ID_QUERY, friendStatus.getFriendId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return result;
    }

    public Collection<User> findMutuals(long id, long otherId) {
        List<FriendStatus> userFriendsIds = jdbc.queryForList(FIND_FRIENDS_QUERY, friendStatusClass, id);
        List<FriendStatus> otherFriendsIds = jdbc.queryForList(FIND_FRIENDS_QUERY, friendStatusClass, otherId);

        List<User> userFriends = userFriendsIds.stream()
                .map(friendStatus -> findOne(FIND_BY_ID_QUERY, friendStatus.getFriendId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<User> otherFriends = otherFriendsIds.stream()
                .map(friendStatus -> findOne(FIND_BY_ID_QUERY, friendStatus.getFriendId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        Collection<User> result = userFriends.stream()
                .distinct()
                .filter(otherFriends::contains)
                .toList();
        return result;
    }
}
