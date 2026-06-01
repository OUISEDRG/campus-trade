
package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.Result;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.mapper.*;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getPassword, password);
        User user = getOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new RuntimeException("账号已被封禁，系统拒绝登录");
        }
        
        return user;
    }

    @Override
    public boolean register(String username, String password, String nickname) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User existingUser = getOne(wrapper);
        
        if (existingUser != null) {
            return false;
        }
        
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setName(nickname);
        
        return save(newUser);
    }
    
    public boolean register(String username, String password, String name, String studentId, String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User existingUser = getOne(wrapper);
        
        if (existingUser != null) {
            return false;
        }
        
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setName(name);
        newUser.setStudentId(studentId);
        newUser.setPhone(phone);
        
        return save(newUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteAccount(Long id, String ip) {
        log.info("【注销账号请求】时间: {}, IP: {}, 目标用户ID: {}", LocalDateTime.now(), ip, id);

        User user = getById(id);
        if (user == null) {
            log.warn("【注销账号失败】该用户不存在或已被清除。用户ID: {}", id);
            return Result.error("用户不存在或已注销");
        }

        try {
            // 1. 逻辑删除该用户发布的所有闲置商品，防止继续产生新交易
            // （Goods 实体加了 @TableLogic，此操作会自动变为 UPDATE goods SET is_deleted=1 WHERE user_id = id）
            goodsMapper.delete(new QueryWrapper<Goods>().eq("user_id", id));

            // 2. 警告：绝对不要去删除 orders 和 comment！
            // 历史交易不可磨灭，留存评价记录

            // 3. 逻辑删除用户自身
            // （自动变为 UPDATE user SET is_deleted=1 WHERE id = id）
            removeById(id);

            log.info("【注销账号成功】用户及其商品已逻辑下线。历史交易已保护。用户ID: {}", id);
            return Result.success("注销成功，您的账号已停用");
        } catch (Exception e) {
            log.error("【注销账号异常】用户ID: {}, 错误详情: {}", id, e.getMessage(), e);
            throw new RuntimeException("账号注销异常，请联系管理员");
        }
    }

    private static final String ADMIN_INVITE_CODE = "CampusTrade@Admin2026";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerUser(RegisterDTO dto) {
        long count = this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new CustomException("该用户名已被注册");
        }

        if (dto.getRole() != null && dto.getRole() == 1) {
            if (!StringUtils.hasText(dto.getInviteCode()) || !ADMIN_INVITE_CODE.equals(dto.getInviteCode())) {
                throw new CustomException("管理员邀请码无效或为空，拒绝授权");
            }
        } else {
            dto.setRole(0);
        }

        User newUser = new User();
        newUser.setUsername(dto.getUsername());
        newUser.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        newUser.setRole(dto.getRole());
        newUser.setStatus(0);
        newUser.setName(dto.getName());
        newUser.setStudentId(dto.getStudentId());
        newUser.setPhone(dto.getPhone());

        this.save(newUser);
        log.info("【用户注册成功】用户名: {}, 角色: {}", dto.getUsername(), dto.getRole() == 1 ? "管理员" : "普通用户");
    }
}
