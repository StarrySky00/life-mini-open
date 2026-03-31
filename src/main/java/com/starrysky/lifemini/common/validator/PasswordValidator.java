package com.starrysky.lifemini.common.validator;

import com.starrysky.lifemini.common.annotation.Password;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final Pattern PASSWORD_PATTERN= Pattern.compile("^[a-zA-Z0-9._]{6,20}$");
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value==null || value.trim().isEmpty()){
            return true;
        }
        return PASSWORD_PATTERN.matcher(value).matches();
    }
}
