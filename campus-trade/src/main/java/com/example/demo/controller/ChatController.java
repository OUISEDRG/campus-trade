package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.ChatSessionDTO;
import com.example.demo.entity.ChatMessage;
import com.example.demo.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;

    @GetMapping("/history/{userId1}/{userId2}")
    public Result<List<ChatMessage>> getChatHistory(@PathVariable Long userId1, @PathVariable Long userId2) {
        try {
            List<ChatMessage> messages = chatMessageService.getChatHistory(userId1, userId2);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取聊天记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/unread/{userId}")
    public Result<List<ChatMessage>> getUnreadMessages(@PathVariable Long userId) {
        try {
            List<ChatMessage> messages = chatMessageService.getUnreadMessages(userId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取未读消息失败: " + e.getMessage());
        }
    }

    @GetMapping("/sessions/{userId}")
    public Result<List<ChatSessionDTO>> getChatSessions(@PathVariable Long userId) {
        try {
            List<ChatSessionDTO> sessions = chatMessageService.getChatSessions(userId);
            return Result.success(sessions);
        } catch (Exception e) {
            return Result.error("获取会话列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/markRead")
    public Result<String> markMessagesAsRead(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            @SuppressWarnings("unchecked")
            List<Long> senderIds = (List<Long>) request.get("senderIds");
            
            chatMessageService.batchMarkAsRead(userId, senderIds);
            return Result.success("消息已标记为已读");
        } catch (Exception e) {
            return Result.error("标记已读失败: " + e.getMessage());
        }
    }

    @GetMapping("/online")
    public Result<Map<String, Object>> getOnlineStatus(@RequestParam List<Long> userIds) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Boolean> onlineStatus = new HashMap<>();
        
        for (Long userId : userIds) {
            onlineStatus.put(String.valueOf(userId), com.example.demo.config.ChatEndpoint.isOnline(userId));
        }
        
        result.put("onlineStatus", onlineStatus);
        result.put("totalOnline", com.example.demo.config.ChatEndpoint.getOnlineUsers().size());
        
        return Result.success(result);
    }
}
