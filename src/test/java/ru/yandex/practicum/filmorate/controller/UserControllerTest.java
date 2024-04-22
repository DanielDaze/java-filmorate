package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@SpringBootTest
class UserControllerTest {
    HttpClient client = HttpClient.newHttpClient();
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .create();
    Type userListType = new TypeToken<ArrayList<User>>(){}.getType();
    static final URI URL;

    static {
        try {
            URL = new URI("http://localhost:8080/users");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getPostAndPutTest() throws IOException, InterruptedException {
        User expectedUser = User.builder()
                .id(1)
                .name("name")
                .email("name@yandex.ru")
                .login("login")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        String postBody = gson.toJson(expectedUser);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(postBody))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URL)
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        ArrayList<User> users = gson.fromJson(getResponse.body(), userListType);
        Assertions.assertEquals(expectedUser, users.getFirst());

        User expectedUpdatedUser = User.builder()
                .name("upd_name")
                .id(1)
                .email("new_mail@yandex.ru")
                .login("newLogin")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        String updatedBody = gson.toJson(expectedUpdatedUser);
        HttpRequest putRequest = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(updatedBody))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
        User updatedUser = gson.fromJson(putResponse.body(), User.class);
        Assertions.assertEquals(expectedUpdatedUser, updatedUser);
    }

    @Test
    void wrongEmailTest() throws IOException, InterruptedException {
        User user = User.builder()
                .name("name")
                .email("yandex.ru")
                .login("login")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        String postBody = gson.toJson(user);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(postBody))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(400, postResponse.statusCode());

        User userTwo = User.builder()
                .name("name")
                .login("login")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        String postBodyTwo = gson.toJson(userTwo);
        HttpRequest postRequestTwo = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(postBodyTwo))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> postResponseTwo = client.send(postRequestTwo, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(400, postResponseTwo.statusCode());
    }

    @Test
    void wrongLoginTest() throws IOException, InterruptedException {
        User user = User.builder()
                .name("name")
                .email("name@yandex.ru")
                .login("log in")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        String postBody = gson.toJson(user);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(postBody))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(500, postResponse.statusCode());

        User userTwo = User.builder()
                .name("name")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        String postBodyTwo = gson.toJson(userTwo);
        HttpRequest postRequestTwo = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(postBodyTwo))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> postResponseTwo = client.send(postRequestTwo, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(400, postResponseTwo.statusCode());
    }

    @Test
    void wrongBirthdayTest() throws IOException, InterruptedException {
        User user = User.builder()
                .name("name")
                .email("name@yandex.ru")
                .login("login")
                .birthday(LocalDate.of(2300, 8, 8))
                .build();
        String postBody = gson.toJson(user);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(postBody))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(400, postResponse.statusCode());
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
