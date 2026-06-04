package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.dto.UserLoginDTO;
import com.example.demo.dto.UserLoginResponseDTO;
import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<User>> getAllUsers() {
        List<User> list = userService.list();
        return Result.success(list);
    }

    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Validated @RequestBody UserLoginDTO loginDTO) {
        log.info("【登录请求】用户名: {}", loginDTO.getUsername());
        
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, loginDTO.getUsername());
            
            User user = userService.getOne(queryWrapper);

            if (user == null) {
                log.warn("【登录失败】账号不存在: {}", loginDTO.getUsername());
                return Result.error("账号不存在哦！");
            }
            
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean passwordMatch = false;

            // 判断存储的密码格式：BCrypt 以 $2a$ / $2b$ / $2y$ 开头
            String storedPassword = user.getPassword();
            if (storedPassword != null && storedPassword.startsWith("$2")) {
                // BCrypt 加密的密码，直接比对
                passwordMatch = encoder.matches(loginDTO.getPassword(), storedPassword);
            } else {
                // MD5 加密的旧密码，用 MD5 比对兼容老用户
                String md5Password = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
                passwordMatch = storedPassword != null && storedPassword.equals(md5Password);
                if (passwordMatch) {
                    // 自动升级为 BCrypt
                    user.setPassword(encoder.encode(loginDTO.getPassword()));
                    userService.updateById(user);
                    log.info("【密码升级】用户 {} 的密码已从 MD5 升级为 BCrypt", loginDTO.getUsername());
                }
            }

            if (!passwordMatch) {
                log.warn("【登录失败】密码错误: {}", loginDTO.getUsername());
                return Result.error("密码写错啦！");
            }
            
            if (user.getStatus() != null && user.getStatus() == 1) {
                log.warn("【登录失败】账号已被封禁: {}", loginDTO.getUsername());
                return Result.error("账号已被封禁，系统拒绝登录");
            }
            
            UserLoginResponseDTO responseDTO = new UserLoginResponseDTO();
            BeanUtils.copyProperties(user, responseDTO);
            
            log.info("【登录成功】用户: {}, ID: {}", loginDTO.getUsername(), user.getId());
            return Result.success(responseDTO);
        } catch (Exception e) {
            log.error("【登录异常】用户名: {}, 错误: {}", loginDTO.getUsername(), e.getMessage(), e);
            return Result.error("登录异常，请稍后重试");
        }
    }

    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, registerDTO.getUsername());
        User existUser = userService.getOne(queryWrapper);

        if (existUser != null) {
            return Result.error("哎呀，这个账号已经被别人注册啦，换一个试试吧！");
        }

        User newUser = new User();
        newUser.setUsername(registerDTO.getUsername());
        
        newUser.setPassword(new BCryptPasswordEncoder().encode(registerDTO.getPassword()));
        
        newUser.setName(registerDTO.getName());
        newUser.setStudentId(registerDTO.getStudentId());
        newUser.setPhone(registerDTO.getPhone());
        newUser.setStatus(0);
        
        userService.save(newUser);
        return Result.success("太棒了，欢迎加入平台！注册成功！");
    }

    @PostMapping("/registerV2")
    public Result<String> registerV2(@RequestBody RegisterDTO dto) {
        log.info("【注册请求】用户名: {}, 角色: {}", dto.getUsername(), dto.getRole());
        try {
            userService.registerUser(dto);
            return Result.success("注册成功！");
        } catch (Exception e) {
            log.error("【注册异常】用户名: {}, 错误: {}", dto.getUsername(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/update")
    public Result<User> updateUser(@RequestBody User user) {
        User existUser = userService.getById(user.getId());
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        
        boolean success = userService.updateById(user);
        
        if (success) {
            User updatedUser = userService.getById(user.getId());
            return Result.success("修改成功", updatedUser);
        } else {
            return Result.error("修改失败");
        }
    }

    @DeleteMapping("/deleteAccount/{id}")
    public Result<String> deleteAccount(@PathVariable Long id, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        try {
            return userService.deleteAccount(id, ip);
        } catch (Exception e) {
            return Result.error("注销失败，请稍后重试");
        }
    }
}
