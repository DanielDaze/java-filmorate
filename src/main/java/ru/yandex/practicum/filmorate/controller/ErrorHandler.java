package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.Map;

@RestControllerAdvice("ru.yandex.practicum.filmorate.controller")
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleNotValidException(MethodArgumentNotValidException e) {
        String message = "Команда не была выполнена, так как данные были введены неправильно";
        log.error("Во время выполнении команды пользователя произошла ошибка, т.к. данные были введены неправильно");
        return Map.of("Ошибка", message);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException e) {
        log.error("Во время выполнении команды пользователя произошла ошибка, т.к. пользователь пытался найти несуществующие данные");
        return Map.of("Ошибка", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleThrowable(Throwable e) {
        log.error("Во время выполнении команды пользователя произошла ошибка на стороне сервера");
        return Map.of("Ошибка", "Что-то пошло не так");
    }
}
