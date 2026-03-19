package com.fnusale.im.controller;

import com.fnusale.common.common.Result;
import com.fnusale.common.vo.im.MessageVO;
import com.fnusale.common.vo.im.QuickReplyListVO;
import com.fnusale.im.dto.ImageMessageDTO;
import com.fnusale.im.dto.QuickReplyCreateDTO;
import com.fnusale.im.dto.TextMessageDTO;
import com.fnusale.im.dto.VoiceMessageDTO;
import com.fnusale.im.service.ImMessageService;
import com.fnusale.im.service.ImQuickReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天消息控制器
 */
@Tag(name = "聊天消息管理", description = "发送消息、消息记录等接口")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class ImMessageController {

    private final ImMessageService messageService;
    private final ImQuickReplyService quickReplyService;

    @Operation(summary = "发送文字消息", description = "发送文字消息")
    @PostMapping("/text")
    public Result<Map<String, Object>> sendTextMessage(
            @Valid @RequestBody TextMessageDTO dto) {
        MessageVO message = messageService.sendTextMessage(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("messageId", message.getMessageId());
        result.put("sendTime", message.getSendTime());
        return Result.success("发送成功", result);
    }

    @Operation(summary = "发送图片消息", description = "发送图片消息")
    @PostMapping("/image")
    public Result<Map<String, Object>> sendImageMessage(
            @Valid @RequestBody ImageMessageDTO dto) {
        MessageVO message = messageService.sendImageMessage(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("messageId", message.getMessageId());
        result.put("sendTime", message.getSendTime());
        return Result.success("发送成功", result);
    }

    @Operation(summary = "发送语音消息", description = "发送语音消息")
    @PostMapping("/voice")
    public Result<Map<String, Object>> sendVoiceMessage(
            @Valid @RequestBody VoiceMessageDTO dto) {
        MessageVO message = messageService.sendVoiceMessage(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("messageId", message.getMessageId());
        result.put("sendTime", message.getSendTime());
        return Result.success("发送成功", result);
    }

    @Operation(summary = "撤回消息", description = "撤回已发送的消息")
    @DeleteMapping("/{messageId}")
    public Result<Void> recallMessage(
            @Parameter(description = "消息ID") @PathVariable Long messageId) {
        messageService.recallMessage(messageId);
        return Result.success("撤回成功", null);
    }

    @Operation(summary = "获取快捷回复列表", description = "获取系统预设的快捷回复模板")
    @GetMapping("/quick-reply/list")
    public Result<QuickReplyListVO> getQuickReplyList() {
        return Result.success(quickReplyService.getQuickReplyList());
    }

    @Operation(summary = "添加快捷回复", description = "添加自定义快捷回复")
    @PostMapping("/quick-reply")
    public Result<Void> addQuickReply(
            @Valid @RequestBody QuickReplyCreateDTO dto) {
        quickReplyService.addQuickReply(dto);
        return Result.success("添加成功", null);
    }

    @Operation(summary = "删除快捷回复", description = "删除自定义快捷回复")
    @DeleteMapping("/quick-reply/{id}")
    public Result<Void> deleteQuickReply(
            @Parameter(description = "快捷回复ID") @PathVariable Long id) {
        quickReplyService.deleteQuickReply(id);
        return Result.success("删除成功", null);
    }
}