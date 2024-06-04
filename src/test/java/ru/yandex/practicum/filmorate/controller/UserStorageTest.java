package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserStorageTest {
    @Autowired
    private UserDbStorage userStorage;

    @Test
    @Order(1)
    void postAndGetTest() {
        User user = User.builder()
            .name("name")
            .login("login")
            .email("login@yandex.ru")
            .birthday(LocalDate.of(1999, 12, 21)).build();

        User expectedUser = User.builder()
                .id(5)
                .name("name")
                .login("login")
                .email("login@yandex.ru")
                .birthday(LocalDate.of(1999, 12, 21)).build();

        Assertions.assertEquals(userStorage.add(user), expectedUser);
    }

    @Test
    @Order(2)
    void updateTest() {
        User updatedUser = User.builder()
                .id(1)
                .name("new_name")
                .login("new_login")
                .email("new@yandex.ru")
                .birthday(LocalDate.of(1998, 11, 20)).build();

        Assertions.assertEquals(userStorage.update(updatedUser), updatedUser);
    }

    @Test
    @Order(3)
    void getAllTest() {
        userStorage.add(User.builder()
                .name("rnd_name")
                .login("rnd_login")
                .email("rnd@yandex.ru")
                .birthday(LocalDate.of(1938, 10, 19)).build());
        Assertions.assertEquals(6, userStorage.findAll().size());
    }

    @Test
    @Order(4)
    void addFriendTest() {
        userStorage.add(User.builder()
                .name("rnd_name")
                .login("rnd_login")
                .email("rnd@yandex.ru")
                .birthday(LocalDate.of(1938, 10, 19)).build());
        userStorage.add(User.builder()
                .name("new_name")
                .login("new_login")
                .email("new@yandex.ru")
                .birthday(LocalDate.of(1998, 11, 20)).build());

        userStorage.addFriend(3, 4);
        Assertions.assertEquals(1, userStorage.find(3).getFriends().size());
    }

    @Test
    @Order(5)
    void commonFriendsTest() {
        userStorage.addFriend(1, 4);
        Assertions.assertEquals(1, userStorage.findMutuals(1, 3).size());
    }

    @Test
    @Order(6)
    void deleteFriendTest() {
        userStorage.deleteFriend(1, 4);
        Assertions.assertEquals(0, userStorage.findFriends(1).size());

    }
}
