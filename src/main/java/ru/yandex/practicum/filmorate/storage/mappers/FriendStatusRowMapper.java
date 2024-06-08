package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.user.FriendStatus;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendStatusRowMapper implements RowMapper<FriendStatus> {
    @Override
    public FriendStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
        return FriendStatus.builder()
                .userId(rs.getLong("user_id"))
                .friendId(rs.getLong("second_user_id"))
                .confirmed(rs.getBoolean("confirmed"))
                .build();
    }
}
