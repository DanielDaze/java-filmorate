package ru.yandex.practicum.filmorate.controller;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmControllerTest {
    @Autowired
    TestRestTemplate restTemplate;
    HttpClient client = HttpClient.newHttpClient();
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .create();
    Type filmListType = new TypeToken<ArrayList<Film>>(){}.getType();
    static final URI URL;

    static {
        try {
            URL = new URI("http://localhost:8080/films");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test() {
        restTemplate = new TestRestTemplate();
        HttpEntity<String> request = new HttpEntity<>("{\"name\":\"name\",\"description\":\"desc\",\"duration\":7200,\"releaseDate\":\"2021-10-10\"}");
        ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.GET, request, String.class);
    }

    @Test
    void getPostAndPutTest() throws IOException, InterruptedException {
        Film expectedFilm = Film.builder()
                .id(1)
                .name("name")
                .description("desc")
                .duration(Duration.ofSeconds(7200))
                .releaseDate(LocalDate.of(2021,10,10))
                .build();
        String postBody = "{\"name\":\"name\",\"description\":\"desc\",\"duration\":7200,\"releaseDate\":\"2021-10-10\"}";
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
        ArrayList<Film> films = gson.fromJson(getResponse.body(), filmListType);
        Assertions.assertEquals(expectedFilm, films.getFirst());

        String updatedBody = "{\"id\":1,\"name\":\"upd_name\",\"description\":\"new_desc\",\"duration\":6000,\"releaseDate\":\"2008-06-10\"}";
        Film expectedUpdatedFilm = Film.builder()
                .name("upd_name")
                .id(1)
                .description("new_desc")
                .duration(Duration.ofSeconds(6000))
                .releaseDate(LocalDate.of(2008, 6, 10))
                .build();
        HttpRequest putRequest = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(updatedBody))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
        Film updatedFilm = gson.fromJson(putResponse.body(), Film.class);
        Assertions.assertEquals(expectedUpdatedFilm, updatedFilm);
    }

    @Test
    void noNameTest() throws IOException, InterruptedException {
        String noName = "{\"description\":\"new_desc\",\"duration\":6000,\"releaseDate\":\"2008-06-10\"}";
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(noName))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void wrongDescriptionTest() throws IOException, InterruptedException {
        String wrongDescription = "{\"name\":\"upd_name\",\"description\":\"tellus sem mollis dui, in sodales elit erat vitae risus. " +
                "Duis a mi fringilla mi lacinia mattis. Integer eu lacus. Quisque imperdiet, erat nonummy ultricies ornare, " +
                "elit elit fermentum risus, at fringilla purus mauris a nunc. In at pede. Cras vulputate velit eu sem. " +
                "Pellentesque ut ipsum ac mi eleifend egestas. Sed pharetra, felis eget varius ultrices, mauris ipsum porta elit, a feugiat tellus lorem eu metus. " +
                "In lorem. Donec elementum, lorem ut aliquam iaculis, lacus pede sagittis augue, eu tempor erat neque non quam. " +
                "Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Aliquam fringilla cursus purus. " +
                "Nullam scelerisque neque sed sem egestas blandit. Nam nulla magna, malesuada vel, convallis in, cursus et, eros. Proin ultrices. " +
                "Duis volutpat nunc sit amet metus. Aliquam erat volutpat. Nulla facilisis. Suspendisse commodo tincidunt nibh. Phasellus nulla. " +
                "Integer vulputate, risus a ultricies adipiscing, enim mi tempor lorem, eget mollis\"," +
                "\"duration\":6000,\"releaseDate\":\"2008-06-10\"}";
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(wrongDescription))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    void wrongReleaseDateTest() throws IOException, InterruptedException {
        String wrongFilm = "{\"id\":1,\"name\":\"upd_name\",\"description\":\"new_desc\",\"duration\":6000,\"releaseDate\":\"1894-12-28\"}";
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(wrongFilm))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    void wrongDuration() throws IOException, InterruptedException {
        String wrongFilm = "{\"id\":1,\"name\":\"upd_name\",\"description\":\"new_desc\",\"duration\":-1,\"releaseDate\":\"1894-12-28\"}";
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(wrongFilm))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    void filmNotFoundTest() throws IOException, InterruptedException {
        String wrongFilm = "{\"id\":999,\"name\":\"upd_name\",\"description\":\"new_desc\",\"duration\":6000,\"releaseDate\":\"1894-12-28\"}";
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(wrongFilm))
                .setHeader("Content-Type", "application/json")
                .uri(URL)
                .build();
        HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(500, response.statusCode());
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

    private static class DurationTypeAdapter extends TypeAdapter<Duration> {

        @Override
        public void write(final JsonWriter jsonWriter, final Duration duration) throws IOException {
            jsonWriter.value(String.valueOf((Duration.parse(duration.toString()))));
        }

        @Override public Duration read(final JsonReader jsonReader) throws IOException {
            return Duration.parse(jsonReader.nextString());
        }
    }
}