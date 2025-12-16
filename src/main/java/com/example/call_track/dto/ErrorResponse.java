package com.example.call_track.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private String error; //Тип ошибки (например, ValidationError, BusinessError).
    private List<String> errors; //Список конкретных сообщений об ошибках.
    private String path; //URI запроса, где произошла ошибка.
    private int status; //HTTP-статус ошибки.

    @Builder.Default
    private long timestamp = System.currentTimeMillis(); //Время возникновения ошибки.
}
