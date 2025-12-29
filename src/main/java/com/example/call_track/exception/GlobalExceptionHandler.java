package com.example.call_track.exception;

import com.example.call_track.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Класс глобального обработчика исключений
@ControllerAdvice
public class GlobalExceptionHandler {
    //Создаем экземпляр логера для записи сообщений о событиях
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //Метод обрабатывающий исключения типа MethodArgumentNotValidException
    //Принимает исключение и обьект запроса, возвращает ResponseError
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        //Извлекаем список ошибок валидации, формируем строки вида "поле: сообщение" и собираем их в список
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        //Логируем предупреждение с URI запроса и списком ошибок.
        LOGGER.warn("Validation failed at {}: {}", request.getRequestURI(), errors);

        //Формируем объект ответа с информацией об ошибке: тип, список ошибок, путь, статус.
        ErrorResponse response = ErrorResponse.builder()
                .error("ValidationError")
                .errors(errors)
                .path(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        //Возвращаем HTTP-ответ со статусом 400 и телом ErrorResponse.
        return ResponseEntity.badRequest().body(response);
    }

    //Метод обрабатывающий исключения типа CustomBusinessException.
    //Принимает бизнес-исключение и объект запроса, возвращает ResponseError
    @ExceptionHandler(CustomBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessExceptions(
            CustomBusinessException ex, HttpServletRequest request) {

        //Логируем бизнес-ошибку как информационное сообщение.
        LOGGER.info("Business exception at {}: {}", request.getRequestURI(), ex.getMessage());

        //Формируем ответ с типом ошибки BusinessError, сообщением и статусом 409 (Conflict).
        ErrorResponse response = ErrorResponse.builder()
                .error("BusinessError")
                .errors(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .status(HttpStatus.CONFLICT.value())
                .build();

        //Возвращаем HTTP-ответ со статусом 409 и телом ErrorResponse.
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        LOGGER.warn("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .error("ValidationError")
                .errors(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    //Метод обрабатывающий исключения типа NoResourceFoundException (например, для favicon.ico)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {

        // Игнорируем ошибки для favicon.ico - это нормальное поведение браузеров
        if (request.getRequestURI().equals("/favicon.ico")) {
            return ResponseEntity.notFound().build();
        }

        // Для других ресурсов логируем как предупреждение
        LOGGER.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    //Метод будет обрабатывать любые другие исключения.
    //Принимает общее исключение и объект запроса, возвращает ResponseError
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {

        //Логируем ошибку как серьёзную (с трассировкой стека).
        LOGGER.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        //ормируем ответ с типом ошибки InternalError, сообщением "Internal server error", статусом 500.
        ErrorResponse response = ErrorResponse.builder()
                .error("InternalError")
                .errors(List.of("Internal server error"))
                .path(request.getRequestURI())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        //Возвращаем HTTP-ответ со статусом 500 и телом ErrorResponse.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
