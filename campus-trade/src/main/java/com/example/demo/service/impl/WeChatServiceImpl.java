package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.WeChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatServiceImpl implements WeChatService {

    @Value("${wechat.appid:wx1234567890abcdef}")
    private String appId;
    
    @Value("${wechat.secret:secret1234567890abcdef1234567890abcdef}")
    private String appSecret;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private final SecureRandom random = new SecureRandom();

    @Override
    public String getQRCodeUrl(String redirectUri) {
        String state = new BigInteger(130, random).toString(32);
        return String.format(
            "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
            appId,
            redirectUri,
            state
        );
    }

    @Override
    public Map<String, String> getAccessToken(String code) {
        String url = String.format(
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
            appId,
            appSecret,
            code
        );
        
        try {
            String response = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            
            Map<String, String> tokenInfo = new HashMap<>();
            tokenInfo.put("access_token", (String) result.get("access_token"));
            tokenInfo.put("openid", (String) result.get("openid"));
            tokenInfo.put("refresh_token", (String) result.get("refresh_token"));
            
            return tokenInfo;
        } catch (Exception e) {
            throw new RuntimeException("获取access_token失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken, String openid) {
        String url = String.format(
            "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s",
            accessToken,
            openid
        );
        
        try {
            String response = restTemplate.getForObject(url, String.class);
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public User loginOrRegister(Map<String, Object> userInfo) {
        String openid = (String) userInfo.get("openid");
        
        User existingUser = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
            .eq("openid", openid));
        
        if (existingUser != null) {
            return existingUser;
        }
        
        User newUser = new User();
        newUser.setUsername("wx_" + openid.substring(0, 16));
        newUser.setName((String) userInfo.get("nickname"));
        newUser.setAvatar((String) userInfo.get("headimgurl"));
        newUser.setRole(0);
        newUser.setStatus(0);
        
        userMapper.insert(newUser);
        return newUser;
    }

    @Override
    public String getOpenId(String code) {
        Map<String, String> tokenInfo = getAccessToken(code);
        return tokenInfo.get("openid");
    }
}