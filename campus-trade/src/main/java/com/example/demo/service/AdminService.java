package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.UserDetailsDTO;
import com.example.demo.entity.User;

public interface AdminService {
    
    IPage<User> searchUsers(Page<User> page, String keyword);
    
    void updateUserStatus(Long userId, Integer status);
    
    UserDetailsDTO getUserCompleteDetails(Long userId);
}
