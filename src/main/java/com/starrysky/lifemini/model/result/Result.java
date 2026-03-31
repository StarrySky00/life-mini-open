package com.starrysky.lifemini.model.result;
import com.starrysky.lifemini.common.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 统一返回结果类
 *
 * @author StarrySky
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    @Schema(description = "响应码", example = "200")
    private Integer code;
    @Schema(description = "业务提示信息")
    private String message;
    @Schema(description = "业务数据")
    private T data;


    // 快速返回操作成功响应结果(默认提示信息)
    public static <T> Result<T> success(T data) {
        return new Result<>(200, MessageConstant.OPERATION + MessageConstant.SUCCESS, data);
    }


    // 快速返回操作成功响应结果(带响应数据和自定义提示信息)
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }
    public static <T> Result<T> success(int code,String message, T data) {
        return new Result<>(code, message, data);
    }

    // 快速返回操作成功响应结果(带自定义提示信息)
    public static Result success(String message) {
        return new Result(200, message, null);
    }

    // 快速返回操作成功响应结果
    public static Result success() {
        return new Result(200, MessageConstant.OPERATION + MessageConstant.SUCCESS, null);
    }

    // 快速返回操作失败响应结果(默认提示信息)
    public static Result error() {
        return new Result(400, MessageConstant.OPERATION + MessageConstant.FAILED, null);
    }


    // 快速返回操作失败响应结果(带自定义提示信息)
    public static Result error(String message) {
        return new Result(400, message, null);
    }
    public static Result error(Integer code,String message) {
        return new Result(code, message, null);
    }

    // 没有权限访问资源
    public static Result noPermission() {
        return new Result(401, MessageConstant.NO_PERMISSION, null);
    }

    // 服务端出现异常
    public static Result serverException() {
        return new Result(500, MessageConstant.SERVER + MessageConstant.EXCEPTION, null);
    }

}
