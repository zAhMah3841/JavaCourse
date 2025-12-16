package com.example.call_track.utils.validation.implementation;

import com.example.call_track.utils.validation.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

//Реализует ConstraintValidator, связывая аннотацию PasswordMatches с объектами (Object).
@Component
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    //Два приватных поля для хранения имён сравниваемых полей.
    private String passwordField;
    private String confirmPasswordField;

    @Override //Метод инициализации: получает имена полей из аннотации и сохраняет их.
    public void initialize(PasswordMatches constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmPasswordField = constraintAnnotation.confirmPasswordField();
    }

    @Override //Основной метод проверки.
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        //Создаёт BeanWrapperImpl для доступа к свойствам объекта.
        BeanWrapper beanWrapper = new BeanWrapperImpl(value);

        //Извлекает значения полей password и confirmPassword.
        String password = (String) beanWrapper.getPropertyValue(passwordField);
        String confirmPassword = (String) beanWrapper.getPropertyValue(confirmPasswordField);

        //Если одно из полей null, сразу возвращает false.
        if (password == null || confirmPassword == null) return false;

        boolean isValid = password.equals(confirmPassword); //Сравнивает значения полей.
        if (!isValid) { //Если они не совпадают:
            context.disableDefaultConstraintViolation(); //Отключает стандартное сообщение.

            //Добавляет своё сообщение об ошибке (берётся из аннотации).
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addConstraintViolation();
        }

        return isValid; //Возвращает результат проверки (true или false).
    }
}
