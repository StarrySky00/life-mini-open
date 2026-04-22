package com.starrysky.lifemini.controller.user;

import com.starrysky.lifemini.model.entity.ChatMessage;
import com.starrysky.lifemini.model.result.RecordResult;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RequestMapping("/chat")
@RestController
@Slf4j
@Tag(name = "（用户端）智能助手", description = "智能助手对话相关接口")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/location")
    @Operation(summary = "刷新用户位置坐标（进入对话界面时主动获取一次）用户坐标都默认为（112.5,33.0）")
    public Result<Void> takeUserLocation(@RequestParam(name = "用户经度") Double longitude,
                                         @RequestParam(name = "用户纬度") Double latitude) {
        return chatService.saveUserLocation(longitude,latitude);
    }

    @GetMapping("/chat/{id}")
    @Operation(summary = "智能助手",description = "每次点击助手导航栏时主动申请刷新一次位置坐标")
    public Flux<String> AiShops(@RequestParam(name = "content") String content,
                                @PathVariable(name = "id")Long userId) {
        return chatService.searchShops(content,userId);
    }

    @GetMapping("/record")
    @Operation(summary = "查询对话记录(顺序：[旧，-，-，新])")
    public RecordResult<ChatMessage> queryChatRecords(@RequestParam("conversationId") String conversationId,
                                                      @RequestParam(name = "lastId", required = false) Long lastId,
                                                      @RequestParam(name = "pageSize", defaultValue = "20") Long pageSize) {
        return chatService.queryChatRecords(conversationId, lastId, pageSize);
    }
}
