package com.example.call_track.utils.validation;

import com.example.call_track.utils.validation.implementation.PhoneNumberValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

//Определяет собственную аннотацию @ValidPhoneNumber.
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    //Сообщение об ошибке, которое будет возвращено при невалидном номере.
    //Можно переопределить при использовании аннотации.
    String message() default "Invalid phone number";

    Class<?>[] groups() default {}; //Позволяет объединять проверки в группы.
    Class<? extends Payload>[] payload() default {}; //Дополнительные метаданные для аннотации.
}