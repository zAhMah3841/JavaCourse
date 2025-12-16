package com.example.call_track.utils.validation.implementation;

import com.example.call_track.exception.CustomBusinessException;
import com.example.call_track.utils.validation.ValidPhoneNumber;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

//Определяем класс валидатора, который реализует интерфейс ConstraintValidator
//Указываем, что он будет проверять строки (String), аннотированные @ValidPhoneNumber
@Component
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    private PhoneNumberUtil phoneNumberUtil; //Поле для работы с утилитой Google libphonenumber.

    //Константа: набор допустимых типов телефонных номеров (мобильные, стационарные, VoIP).
    //Используется для фильтрации номеров, которые можно считать "пригодными для звонка".
    private static final Set<PhoneNumberUtil.PhoneNumberType> ALLOWED_CALL_TYPES = Set.of(
            PhoneNumberUtil.PhoneNumberType.MOBILE,
            PhoneNumberUtil.PhoneNumberType.FIXED_LINE,
            PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE,
            PhoneNumberUtil.PhoneNumberType.VOIP);

    @Override //Метод инициализации валидатора.
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.phoneNumberUtil = PhoneNumberUtil.getInstance(); //Получаем singleton-экземпляр PhoneNumberUtil
    }

    @Override //Основной метод валидации.
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(phoneNumber)) return true; //Если строка пустая или null, возвращаем true.
        try {
            //Пытаемся распарсить строку в объект PhoneNumber
            Phonenumber.PhoneNumber parsed = phoneNumberUtil.parse(phoneNumber, "");
            if(!phoneNumberUtil.isValidNumber(parsed)) return false; //Если нет — возвращает false.

            //Определяем тип номера (мобильный, VoIP и т.д.).
            PhoneNumberUtil.PhoneNumberType type = phoneNumberUtil.getNumberType(parsed);
            //Проверяем, входит ли он в список допустимых типов через вспомогательный метод.
            return isCallableNumberType(type);
        } catch (NumberParseException e) {
            return false; //Если номер не удалось распарсить (ошибка формата), возвращает false.
        } catch (Exception e) {
            //Ловит любые другие исключения и выбрасывает своё бизнес-исключение с описанием ошибки.
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Unexpected error validating phone number: " + e.getMessage()).addConstraintViolation();

            return false;
        }
    }

    //Вспомогательный метод: проверяет, входит ли тип номера в список допустимых.
    //Возвращает true, если номер можно использовать для звонков.
    private boolean isCallableNumberType(PhoneNumberUtil.PhoneNumberType type) {
        return ALLOWED_CALL_TYPES.contains(type);
    }
}
