package com.example.call_track.utils.validation.implementation;

import com.example.call_track.utils.validation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.*;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

//Реализует интерфейс ConstraintValidator, который связывает аннотацию ValidPassword с типом данных String.
@Component
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    private ValidPassword constraint; //хранит параметры из аннотации (например, минимальная длина).
    //объект Passay, который будет выполнять проверку пароля по заданным правилам.
    private org.passay.PasswordValidator validator;

    @Override //Метод вызывается при инициализации валидатора.
    public void initialize(ValidPassword constraintAnnotation) {
        this.constraint = constraintAnnotation;
        this.validator = createPasswordValidator();
    }

    @Override //Основной метод валидации.
    public boolean isValid(String password, ConstraintValidatorContext context) {
        //Если пароль пустой или null, сразу возвращаем false.
        if (password == null || password.isEmpty()) return false;

        RuleResult result = validator.validate(new PasswordData(password)); //Проверяем пароль с помощью Passay.
        if (result.isValid()) return true; //Если все правила пройдены, возвращаем true.

        List<String> messages = validator.getMessages(result); //Получаем список сообщений об ошибках от Passay.
        String errorMessage = String.join(", ", messages); //Объединяем их в одну строку через запятую.

        context.disableDefaultConstraintViolation(); //Отключаем стандартное сообщение об ошибке.
        context.buildConstraintViolationWithTemplate(errorMessage) //Добавляем своё сообщение в контекст валидации.
                .addConstraintViolation();

        return false; //Возвращаем false, так как пароль не прошёл проверку.
    }

    //Вспомогательный метод для создания валидатора Passay.
    private org.passay.PasswordValidator createPasswordValidator() {
        List<Rule> rules = new ArrayList<>(); //Создаём список правил.

        rules.add(new LengthRule(constraint.minLength(), constraint.maxLength())); //Добавляем правило длины пароля.
        rules.add(new WhitespaceRule()); //Запрещаем пробелы в пароле.

        //Добавляем правила для обязательного наличия хотя бы одной заглавной буквы,
        //строчной буквы и цифры — если это указано в аннотации.
        if (constraint.requireUppercase()) rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        if (constraint.requireLowercase()) rules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
        if (constraint.requireDigits()) rules.add(new CharacterRule(EnglishCharacterData.Digit, 1));

        //Добавляем правило для обязательного наличия хотя бы одного спецсимвола.
        if (constraint.requireSpecialCharacters()) rules.add(
                new CharacterRule(EnglishCharacterData.Special, 1));

        //Если запрещены общие паттерны, добавляем правила:
        if (constraint.forbidCommonPatterns()) {
            //Запрещаем последовательности букв (например, abcde).
            rules.add(new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 5, false));

            //Запрещаем последовательности цифр (12345).
            rules.add(new IllegalSequenceRule(EnglishSequenceData.Numerical, 5, false));

            //Запрещаем последовательности клавиатуры (qwerty).
            rules.add(new IllegalSequenceRule(EnglishSequenceData.USQwerty, 5, false));
        }

        //Создаём и возвращает объект PasswordValidator с заданными правилами.
        return new org.passay.PasswordValidator(rules);
    }
}
