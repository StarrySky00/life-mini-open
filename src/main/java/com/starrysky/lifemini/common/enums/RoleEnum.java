package com.starrysky.lifemini.common.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {

    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }

    /**
     * 根据字符串获取枚举
     * @param roleStr 字符串
     * @return 枚举 or null
     */
    public static RoleEnum fromString(String roleStr) {
        for (RoleEnum e : RoleEnum.values()) {
            if (e.role.equals(roleStr)) {
                return e;
            }
        }
        return null;
    }
}