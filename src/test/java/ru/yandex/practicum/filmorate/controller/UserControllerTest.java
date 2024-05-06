package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
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
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@AutoConfigureMockMvc
@ContextConfiguration(classes = {UserControllerTest.class, UserController.class, UserService.class, UserStorage.class, InMemoryUserStorage.class})
@WebMvcTest(controllers = UserController.class)
@Import(UserController.class)
@ExtendWith(SpringExtension.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
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
    void getPostAndPutTest() throws Exception {
        User expectedUser = User.builder()
                .id(1)
                .name("name")
                .email("name@yandex.ru")
                .login("login")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        String postBody = gson.toJson(expectedUser);
        mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(postBody))
                .andReturn();

        MvcResult getResult = mockMvc.perform(MockMvcRequestBuilders.get(URL)).andReturn();
        ArrayList<User> users = gson.fromJson(getResult.getResponse().getContentAsString(), userListType);
        Assertions.assertEquals(expectedUser, users.getFirst());

        User expectedUpdatedUser = User.builder()
                .name("upd_name")
                .id(1)
                .email("new_mail@yandex.ru")
                .login("newLogin")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        String updatedBody = gson.toJson(expectedUpdatedUser);
        MvcResult putResult = mockMvc.perform(MockMvcRequestBuilders.put(URL)
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(updatedBody))
                 .andReturn();
        User updatedUser = gson.fromJson(putResult.getResponse().getContentAsString(), User.class);
        Assertions.assertEquals(expectedUpdatedUser, updatedUser);
    }

    @Test
    void wrongEmailTest() throws Exception {
        User user = User.builder()
                .name("name")
                .email("yandex.ru")
                .login("login")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        String postBody = gson.toJson(user);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody))
                .andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());

        User userTwo = User.builder()
                .name("name")
                .login("login")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        String postBodyTwo = gson.toJson(userTwo);
        MvcResult resultTwo = mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody))
                .andReturn();
        Assertions.assertEquals(400, resultTwo.getResponse().getStatus());
    }

    @Test
    void wrongLoginTest() throws Exception {
        User user = User.builder()
                .name("name")
                .email("name@yandex.ru")
                .login("log in")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        String postBody = gson.toJson(user);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody))
                .andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());

        User userTwo = User.builder()
                .name("name")
                .birthday(LocalDate.of(1999, 8, 8))
                .build();
        String postBodyTwo = gson.toJson(userTwo);
        MvcResult resultTwo = mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBodyTwo))
                .andReturn();
        Assertions.assertEquals(400, resultTwo.getResponse().getStatus());
    }

    @Test
    void wrongBirthdayTest() throws Exception {
        User user = User.builder()
                .name("name")
                .email("name@yandex.ru")
                .login("login")
                .birthday(LocalDate.of(2300, 8, 8))
                .build();
        String postBody = gson.toJson(user);
        MvcResult result =  mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postBody))
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
}
