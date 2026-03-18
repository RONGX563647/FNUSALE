package com.fnusale.common.utils;

import com.fnusale.common.common.Result;
import com.fnusale.common.common.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 返回结果工具类
 */
public class ResultUtils {

    private ResultUtils() {
    }

    /**
     * 成功返回
     */
    public static <T> Result<T> success() {
        return Result.success();
    }

    /**
     * 成功返回带数据
     */
    public static <T> Result<T> success(T data) {
        return Result.success(data);
    }

    /**
     * 成功返回带消息和数据
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.success(message, data);
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> failed() {
        return Result.failed();
    }

    /**
     * 失败返回带消息
     */
    public static <T> Result<T> failed(String message) {
        return Result.failed(message);
    }

    /**
     * 分页结果转换
     */
    public static <T> Result<PageResult<T>> page(IPage<T> page) {
        PageResult<T> pageResult = PageResult.of(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getRecords()
        );
        return success(pageResult);
    }

    /**
     * 分页结果转换
     */
    public static <T> Result<PageResult<T>> page(long pageNum, long pageSize, long total, List<T> list) {
        PageResult<T> pageResult = PageResult.of(pageNum, pageSize, total, list);
        return success(pageResult);
    }
}