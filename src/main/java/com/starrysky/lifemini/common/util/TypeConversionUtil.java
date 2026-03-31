package com.starrysky.lifemini.common.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TypeConversionUtil {
    /**
     * 类型转换
     *
     * @param obj
     * @param defaultValue 默认返回值
     * @return 转换为Integer
     */
    public static Integer toInt(Object obj, int defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                log.warn("无法解析为整数：{}。。。", str);
                return defaultValue;
            }
        }
        if (obj instanceof Number num) {
            return num.intValue();
        }
        return defaultValue;
    }

    /**
     * obj 转换为Long,失败返回默认值
     * @param obj
     * @param defaultValue
     * @return
     */
    public static Long toLong(Object obj, Long defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        try {
            return toLong(obj);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Object 转 Long类型（支持Number）
     *
     * @param obj 需要转换的值
     * @return 转换成功 返回值：Long <br> 转换失败
     */
    public static Long toLong(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("转换失败：目标对象为空");
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("转换失败：字符串 [" + obj + "] 无法解析为 Long");
            }
        }
        throw new IllegalArgumentException("转换失败：不支持的类型 [" + obj.getClass().getName() + "]");
    }

    /**
     * 转换为Double
     *
     * @param value
     * @return
     */
    public static Double ToDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
