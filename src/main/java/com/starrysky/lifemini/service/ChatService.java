package com.starrysky.lifemini.service;

import com.starrysky.lifemini.model.entity.ChatMessage;
import com.starrysky.lifemini.model.result.PageResult;
import com.starrysky.lifemini.model.result.RecordResult;
import com.starrysky.lifemini.model.result.Result;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {
    //对话
    Flux<String> searchShops(String content,Long userId);

    //查询对话历史
    RecordResult<ChatMessage> queryChatRecords(String conversationId, Long lastId, Long pageSize);

    //获取用户经纬度
    Result<Void> saveUserLocation(Double longitude, Double latitude);
}
