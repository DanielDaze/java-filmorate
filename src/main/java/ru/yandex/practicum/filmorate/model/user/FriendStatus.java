package ru.yandex.practicum.filmorate.model.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendStatus {
    long userId;
    long friendId;
    boolean confirmed;
}
