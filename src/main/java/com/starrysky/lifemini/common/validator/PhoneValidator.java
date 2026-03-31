package com.starrysky.lifemini.common.validator;

import com.starrysky.lifemini.common.annotation.Phone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3456789]\\d{9}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // 空值放过
        }
        return PHONE_PATTERN.matcher(value).matches();
    }
}