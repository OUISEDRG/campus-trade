
package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.Result;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.entity.User;

public interface UserService extends IService<User> {
    User login(String username, String password);
    boolean register(String username, String password, String nickname);
    boolean register(String username, String password, String name, String studentId, String phone);
    Result<String> deleteAccount(Long id, String ip);
    
    void registerUser(RegisterDTO dto);
}
