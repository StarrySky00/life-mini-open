package com.starrysky.lifemini.common.annotation;

import com.starrysky.lifemini.common.validator.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)

public @interface Password {
    String message() default "密码必须由字母数字下划线.构成，长度6-20";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
