package com.starrysky.lifemini.controller.user;

import com.starrysky.lifemini.model.dto.LocationDTO;
import com.starrysky.lifemini.model.entity.ChatMessage;
import com.starrysky.lifemini.model.result.RecordResult;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author StarrySky
 */
@RequiredArgsConstructor
@RequestMapping("/chat")
@RestController
@Slf4j
@Tag(name = "（用户端）智能助手", description = "智能助手对话相关接口")
public class ChatController {

    private final ChatService chatService;

/*  @GetMapping("/chat/{id}")
    @Operation(summary = "智能助手", description = "每次点击助手导航栏时主动申请刷新一次位置坐标")
    public Flux<String> aiShops(@RequestParam(name = "content") String content,
                                @PathVariable(name = "id") Long userId) {
        //return chatService.searchShops(content, userId);
        return null;
    }*/

    @GetMapping("/chat/ai")
    @Operation(summary = "智能 助手", description = "每次点击助手导航栏时主动申请刷新一次位置坐标")
    public Flux<String> chat(@RequestParam("content") String content,
                             @RequestParam(value = "lon", required = false) Double lon,
                             @RequestParam(value = "lat", required = false) Double lat
    ) {
        return chatService.chat(content, lon, lat);
    }

    // 2. 修改 Controller
    @PostMapping("/location")
    @Operation(summary = "刷新用户位置坐标")
    public Result<Void> takeUserLocation(@RequestBody LocationDTO locationDTO) {
        // 通过 dto 获取参数
        return chatService.saveUserLocation(locationDTO.getLongitude(), locationDTO.getLatitude());
    }

    @GetMapping("/record")
    @Operation(summary = "查询对话记录(顺序：[旧，-，-，新])")
    public RecordResult<ChatMessage> queryChatRecords(@RequestParam("conversationId") String conversationId,
                                                      @RequestParam(name = "lastId", required = false) Long lastId,
                                                      @RequestParam(name = "pageSize", defaultValue = "20") Long pageSize) {
        return chatService.queryChatRecords(conversationId, lastId, pageSize);
    }

}