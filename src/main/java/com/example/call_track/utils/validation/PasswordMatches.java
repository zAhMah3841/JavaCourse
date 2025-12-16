package com.example.call_track.utils.validation;

import com.example.call_track.utils.validation.implementation.PasswordMatchesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

//Определяет кастомную аннотацию @PasswordMatches.
@Documented
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {
    String message() default "Passwords do not match"; //сообщение об ошибке по умолчанию.

    //стандартные параметры для всех аннотаций валидации.
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    //имена полей, которые нужно сравнивать (по умолчанию "password" и "confirmPassword").
    String passwordField() default "password";
    String confirmPasswordField() default "confirmPassword";
}
