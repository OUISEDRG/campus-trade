package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.UserDetailsDTO;
import com.example.demo.entity.User;

import java.util.List;
import java.util.Map;

public interface AdminService {
    
    IPage<User> searchUsers(Page<User> page, String keyword);
    
    void updateUserStatus(Long userId, Integer status);
    
    UserDetailsDTO getUserCompleteDetails(Long userId);
    
    Map<String, Object> getDashboardStats();
    
    List<Map<String, Object>> getWeeklyStats();
    
    List<Map<String, Object>> getMonthlyStats();
    
    List<Map<String, Object>> getCategoryStats();
    
    List<Map<String, Object>> getTopSellers();
    
    List<Map<String, Object>> getRecentOrders();
}
