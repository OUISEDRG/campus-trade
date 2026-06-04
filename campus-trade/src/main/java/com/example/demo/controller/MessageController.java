package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.MessageRecord;
import com.example.demo.entity.MessageTemplate;
import com.example.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/template")
    public Result<MessageTemplate> addTemplate(@RequestBody MessageTemplate template) {
        MessageTemplate saved = messageService.addTemplate(template);
        return Result.success("模板添加成功", saved);
    }

    @PutMapping("/template")
    public Result<MessageTemplate> updateTemplate(@RequestBody MessageTemplate template) {
        MessageTemplate updated = messageService.updateTemplate(template);
        return Result.success("模板更新成功", updated);
    }

    @GetMapping("/template/{code}")
    public Result<MessageTemplate> getTemplate(@PathVariable String code) {
        MessageTemplate template = messageService.getTemplateByCode(code);
        if (template == null) {
            return Result.error("模板不存在");
        }
        return Result.success(template);
    }

    @GetMapping("/templates")
    public Result<List<MessageTemplate>> getAllTemplates() {
        List<MessageTemplate> templates = messageService.getAllTemplates();
        return Result.success(templates);
    }

    @DeleteMapping("/template/{id}")
    public Result<String> deleteTemplate(@PathVariable Long id) {
        messageService.deleteTemplate(id);
        return Result.success("模板已删除");
    }

    @PostMapping("/send")
    public Result<MessageRecord> sendMessage(@RequestBody Map<String, Object> params) {
        Long userId = ((Number) params.get("userId")).longValue();
        String templateCode = (String) params.get("templateCode");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) params.get("variables");
        
        MessageRecord record = messageService.sendMessage(userId, templateCode, variables);
        return Result.success("消息已发送", record);
    }

    @PostMapping("/send-direct")
    public Result<MessageRecord> sendMessageDirect(@RequestBody Map<String, Object> params) {
        Long userId = ((Number) params.get("userId")).longValue();
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        
        MessageRecord record = messageService.sendMessageDirect(userId, title, content);
        return Result.success("消息已发送", record);
    }

    @GetMapping("/user/{userId}")
    public Result<List<MessageRecord>> getMessages(@PathVariable Long userId) {
        List<MessageRecord> messages = messageService.getMessagesByUserId(userId);
        return Result.success(messages);
    }

    @GetMapping("/user/{userId}/unread")
    public Result<List<MessageRecord>> getUnreadMessages(@PathVariable Long userId) {
        List<MessageRecord> messages = messageService.getUnreadMessages(userId);
        return Result.success(messages);
    }

    @PostMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id) {
        boolean success = messageService.markAsRead(id);
        return success ? Result.success("已标记为已读") : Result.error("操作失败");
    }

    @PostMapping("/user/{userId}/read-all")
    public Result<String> markAllAsRead(@PathVariable Long userId) {
        int count = messageService.markAllAsRead(userId);
        return Result.success("已标记 " + count + " 条消息为已读");
    }
}