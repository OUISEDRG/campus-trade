package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.User;
import com.example.demo.service.WeChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wechat")
public class WeChatController {

    @Autowired
    private WeChatService weChatService;

    @GetMapping("/qrcode-url")
    public Result<Map<String, String>> getQRCodeUrl(@RequestParam String redirectUri) {
        String encodedUri = Base64.getUrlEncoder().encodeToString(redirectUri.getBytes());
        String qrCodeUrl = weChatService.getQRCodeUrl(encodedUri);
        
        Map<String, String> result = new HashMap<>();
        result.put("url", qrCodeUrl);
        return Result.success(result);
    }

    @GetMapping("/callback")
    public Result<Map<String, Object>> handleCallback(@RequestParam String code) {
        try {
            Map<String, String> tokenInfo = weChatService.getAccessToken(code);
            String accessToken = tokenInfo.get("access_token");
            String openid = tokenInfo.get("openid");
            
            Map<String, Object> userInfo = weChatService.getUserInfo(accessToken, openid);
            User user = weChatService.loginOrRegister(userInfo);
            
            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("openid", openid);
            
            return Result.success("登录成功", result);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> weChatLogin(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        
        try {
            Map<String, String> tokenInfo = weChatService.getAccessToken(code);
            String accessToken = tokenInfo.get("access_token");
            String openid = tokenInfo.get("openid");
            
            Map<String, Object> userInfo = weChatService.getUserInfo(accessToken, openid);
            User user = weChatService.loginOrRegister(userInfo);
            
            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("openid", openid);
            
            return Result.success("登录成功", result);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }
}