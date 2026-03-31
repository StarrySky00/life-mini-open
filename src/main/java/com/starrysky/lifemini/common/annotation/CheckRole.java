package com.starrysky.lifemini.common.annotation;

import com.starrysky.lifemini.common.enums.RoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 验权<br/> USER_ROLE <br/>  ADMIN_ROLE
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckRole {
    RoleEnum value();
}
