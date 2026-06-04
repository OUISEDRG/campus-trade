package com.example.demo.service;

import com.example.demo.entity.User;

import java.util.Map;

public interface WeChatService {

    String getQRCodeUrl(String redirectUri);
    
    Map<String, String> getAccessToken(String code);
    
    Map<String, Object> getUserInfo(String accessToken, String openid);
    
    User loginOrRegister(Map<String, Object> userInfo);
    
    String getOpenId(String code);
}