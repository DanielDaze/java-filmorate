package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.user.User;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {
    @Autowired
    private TestRestTemplate rest;

    @LocalServerPort
    private int port;

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .create();

    @Test
    void getPostAndPutTest() {
        User expectedUser = User.builder()
                .id(1)
                .name("name")
                .email("name@yandex.ru")
                .login("login")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();

        User postedUser = User.builder()
                .name("name")
                .email("name@yandex.ru")
                .login("login")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();

        ResponseEntity<String> response = rest.postForEntity("http://localhost:" + port + "/users", new HttpEntity<>(postedUser), String.class);

        Assertions.assertEquals(expectedUser, gson.fromJson(response.getBody(), User.class));

        User expectedUpdatedUser = User.builder()
                .name("upd_name")
                .id(1)
                .email("new_mail@yandex.ru")
                .login("newLogin")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ResponseEntity<String> updateResponse = rest.exchange("http://localhost:" + port + "/users", HttpMethod.PUT, new HttpEntity<>(expectedUpdatedUser), String.class);

        Assertions.assertEquals(expectedUpdatedUser, gson.fromJson(updateResponse.getBody(), User.class));
    }

    @Test
    void wrongEmailTest() {
        User user = User.builder()
                .name("name")
                .email("yandex.ru")
                .login("login")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        ResponseEntity<String> response = rest.postForEntity("http://localhost:" + port + "/users", new HttpEntity<>(user), String.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        User userTwo = User.builder()
                .name("name")
                .login("login")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        ResponseEntity<String> responseTwo = rest.postForEntity("http://localhost:" + port + "/users", new HttpEntity<>(userTwo), String.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseTwo.getStatusCode());
    }

    @Test
    void wrongLoginTest() {
        User user = User.builder()
                .name("name")
                .email("name@yandex.ru")
                .login("log in")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        ResponseEntity<String> response = rest.postForEntity("http://localhost:" + port + "/users", new HttpEntity<>(user), String.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        User userTwo = User.builder()
                .name("name")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        ResponseEntity<String> responseTwo = rest.postForEntity("http://localhost:" + port + "/users", new HttpEntity<>(userTwo), String.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseTwo.getStatusCode());
    }

    @Test
    void wrongBirthdayTest() {
        User user = User.builder()
                .name("name")
                .email("name@yandex.ru")
                .login("login")
                .birthday(LocalDate.of(2300, 8, 8))
                .build();
        ResponseEntity<String> response = rest.postForEntity("http://localhost:" + port + "/users", new HttpEntity<>(user), String.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private static class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {
        private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public void write(final JsonWriter jsonWriter, final LocalDate localDate) throws IOException {
            jsonWriter.value(localDate.format(dtf));
        }

        @Override
        public LocalDate read(final JsonReader jsonReader) throws IOException {
            return LocalDate.parse(jsonReader.nextString(), dtf);
        }
    }
}
