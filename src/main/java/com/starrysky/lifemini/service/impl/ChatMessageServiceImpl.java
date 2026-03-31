package com.starrysky.lifemini.service.impl;

import com.starrysky.lifemini.model.entity.ChatMessage;
import com.starrysky.lifemini.mapper.ChatMessageMapper;
import com.starrysky.lifemini.service.IChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户对话记录表 服务实现类
 * </p>
 *
 * @author StarrySky
 * @since 2026-02-11
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

}
