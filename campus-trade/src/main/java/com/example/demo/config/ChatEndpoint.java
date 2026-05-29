package com.example.demo.config;

import com.example.demo.entity.ChatMessage;
import com.example.demo.service.ChatMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/ws/chat/{userId}")
@Component
public class ChatEndpoint {

    private static final Map<String, Session> ONLINE_USERS = new ConcurrentHashMap<>();
    
    private static ChatMessageService chatMessageService;
    
    @Autowired
    public void setChatMessageService(ChatMessageService service) {
        ChatEndpoint.chatMessageService = service;
        log.info("【WebSocket初始化】ChatMessageService 注入成功！");
    }
    
    public ChatEndpoint() {
        log.info("【WebSocket】ChatEndpoint 构造方法被调用！");
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        ONLINE_USERS.put(userId, session);
        log.info("【WebSocket连接】用户 {} 上线，当前在线人数: {}", userId, ONLINE_USERS.size());
        
        if (chatMessageService == null) {
            log.error("【严重问题！】WebSocket中ChatMessageService为NULL！");
        } else {
            log.info("【WebSocket连接】ChatMessageService已就绪，用户ID: {}", userId);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ChatMessage chatMsg = mapper.readValue(message, ChatMessage.class);
            chatMsg.setSenderId(Long.valueOf(userId));
            chatMsg.setCreateTime(new Date());

            String receiverIdStr = String.valueOf(chatMsg.getReceiverId());
            Session receiverSession = ONLINE_USERS.get(receiverIdStr);

            if (receiverSession != null && receiverSession.isOpen()) {
                chatMsg.setStatus(1);
                receiverSession.getBasicRemote().sendText(mapper.writeValueAsString(chatMsg));
                log.info("【WebSocket消息推送】发送给在线用户 {}: {}", receiverIdStr, chatMsg.getContent());
            } else {
                chatMsg.setStatus(0);
                log.info("【WebSocket消息】接收者 {} 不在线，消息将标记为未读", receiverIdStr);
            }
            
            if (chatMessageService == null) {
                log.error("【致命错误！】chatMessageService为NULL！无法保存消息！");
                return;
            }
            
            if (chatMsg.getGoodsId() == null) {
                log.info("【WebSocket消息诊断】goodsId为空，将设置为0");
                chatMsg.setGoodsId(0L);
            }
            
            log.info("【WebSocket消息诊断】准备保存消息 - senderId: {}, receiverId: {}, content: {}, goodsId: {}, status: {}", 
                    chatMsg.getSenderId(), chatMsg.getReceiverId(), chatMsg.getContent(), chatMsg.getGoodsId(), chatMsg.getStatus());
            
            chatMessageService.saveMessage(chatMsg);
            log.info("【WebSocket消息保存成功】发送方: {}, 接收方: {}, 内容: {}, 状态: {}", 
                    chatMsg.getSenderId(), chatMsg.getReceiverId(), chatMsg.getContent(), chatMsg.getStatus());
            
            session.getBasicRemote().sendText(mapper.writeValueAsString(chatMsg));
            
        } catch (Exception e) {
            log.error("【WebSocket消息处理异常】", e);
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        ONLINE_USERS.remove(userId);
        log.info("用户 {} 下线", userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 错误", error);
    }
    
    public static boolean isOnline(Long userId) {
        return ONLINE_USERS.containsKey(String.valueOf(userId));
    }
    
    public static Map<String, Session> getOnlineUsers() {
        return ONLINE_USERS;
    }
}
