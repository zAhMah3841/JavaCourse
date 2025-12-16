package com.example.call_track.utils.validation;

import com.example.call_track.utils.validation.implementation.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

//Определяет кастомную аннотацию @ValidPassword.
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    //Сообщение об ошибке по умолчанию, если пароль не прошёл проверку.
    //Может быть переопределено при использовании аннотации.
    String message() default "Invalid Password";

    Class<?>[] groups() default {}; //позволяет группировать проверки.
    Class<? extends Payload>[] payload() default {}; //используется для передачи метаданных

    //Минимальная и максимальная длина пароля.
    //Значения по умолчанию: от 8 до 64 символов.
    int minLength() default 8;
    int maxLength() default 64;

    //Флаги, определяющие требования к паролю:
    boolean requireUppercase() default true; //Должна быть хотя бы одна заглавная буква.
    boolean requireLowercase() default true; //Должна быть хотя бы одна строчная буква.
    boolean requireDigits() default true; //Должна быть хотя бы одна цифра.
    boolean requireSpecialCharacters() default true; //Должен быть хотя бы один спецсимвол.

    //Флаг, запрещающий использование распространённых последовательностей (например, 12345, abcdef, qwerty).
    boolean forbidCommonPatterns() default true;
}
