package ru.yandex.practicum.filmorate.controller;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@AutoConfigureMockMvc
@ContextConfiguration(classes = {FilmControllerTest.class})
@WebMvcTest(controllers = FilmController.class)
@Import(FilmController.class)
@ExtendWith(SpringExtension.class)
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
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
    void getPostAndPutTest() throws Exception {
        Film expectedFilm = Film.builder()
                .id(1)
                .name("name")
                .description("desc")
                .duration(120)
                .releaseDate(LocalDate.of(2021,10,10))
                .build();
        String postBody = "{\"name\":\"name\",\"description\":\"desc\",\"duration\":120,\"releaseDate\":\"2021-10-10\"}";
        mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody))
                        .andReturn();

        MvcResult getResult = mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andReturn();

        ArrayList<Film> films = gson.fromJson(getResult.getResponse().getContentAsString(), filmListType);
        Assertions.assertEquals(expectedFilm, films.getFirst());

        String updatedBody = "{\"id\":1,\"name\":\"upd_name\",\"description\":\"new_desc\",\"duration\":210,\"releaseDate\":\"2008-06-10\"}";
        Film expectedUpdatedFilm = Film.builder()
                .name("upd_name")
                .id(1)
                .description("new_desc")
                .duration(210)
                .releaseDate(LocalDate.of(2008, 6, 10))
                .build();
        MvcResult putResult = mockMvc.perform(MockMvcRequestBuilders.put(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedBody))
                .andReturn();
        Film updatedFilm = gson.fromJson(putResult.getResponse().getContentAsString(), Film.class);
        Assertions.assertEquals(expectedUpdatedFilm, updatedFilm);
    }

    @Test
    void noNameTest() throws Exception {
        String wrongFilm = "{\"description\":\"new_desc\",\"duration\":6000,\"releaseDate\":\"2008-06-10\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(wrongFilm))
                .andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    void wrongDescriptionTest() throws Exception {
        String wrongFilm = "{\"name\":\"upd_name\",\"description\":\"tellus sem mollis dui, in sodales elit erat vitae risus. " +
                "Duis a mi fringilla mi lacinia mattis. Integer eu lacus. Quisque imperdiet, erat nonummy ultricies ornare, " +
                "elit elit fermentum risus, at fringilla purus mauris a nunc. In at pede. Cras vulputate velit eu sem. " +
                "Pellentesque ut ipsum ac mi eleifend egestas. Sed pharetra, felis eget varius ultrices, mauris ipsum porta elit, a feugiat tellus lorem eu metus. " +
                "In lorem. Donec elementum, lorem ut aliquam iaculis, lacus pede sagittis augue, eu tempor erat neque non quam. " +
                "Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Aliquam fringilla cursus purus. " +
                "Nullam scelerisque neque sed sem egestas blandit. Nam nulla magna, malesuada vel, convallis in, cursus et, eros. Proin ultrices. " +
                "Duis volutpat nunc sit amet metus. Aliquam erat volutpat. Nulla facilisis. Suspendisse commodo tincidunt nibh. Phasellus nulla. " +
                "Integer vulputate, risus a ultricies adipiscing, enim mi tempor lorem, eget mollis\"," +
                "\"duration\":6000,\"releaseDate\":\"2008-06-10\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongFilm))
                .andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    void wrongReleaseDateTest() throws Exception {
        String wrongFilm = "{\"id\":1,\"name\":\"upd_name\",\"description\":\"new_desc\",\"duration\":6000,\"releaseDate\":\"1894-12-28\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongFilm))
                .andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    void wrongDuration() throws Exception {
        String wrongFilm = "{\"id\":1,\"name\":\"upd_name\",\"description\":\"new_desc\",\"duration\":-1,\"releaseDate\":\"1894-12-28\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongFilm))
                .andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    void filmNotFoundTest() throws Exception {
        String wrongFilm = "{\"id\":999,\"name\":\"upd_name\",\"description\":\"new_desc\",\"duration\":6000,\"releaseDate\":\"1894-12-28\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongFilm))
                .andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());
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