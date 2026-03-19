package com.fnusale.user.controller;

import com.fnusale.common.common.Result;
import com.fnusale.common.util.UserContext;
import com.fnusale.user.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "文件上传", description = "头像、认证图片等文件上传接口")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final OssService ossService;

    @Operation(summary = "上传头像", description = "上传用户头像图片到 OSS")
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getCurrentUserId();
        String url = ossService.uploadAvatar(file, userId);
        
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        
        return Result.success("头像上传成功", result);
    }

    @Operation(summary = "上传认证图片", description = "上传用户认证图片（学生证/校园卡）到 OSS")
    @PostMapping("/auth")
    public Result<Map<String, String>> uploadAuthImage(
            @RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getCurrentUserId();
        String url = ossService.uploadAuthImage(file, userId);
        
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        
        return Result.success("认证图片上传成功", result);
    }
}
