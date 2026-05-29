package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.dto.UserDetailsDTO;
import com.example.demo.dto.UserStatusDTO;
import com.example.demo.entity.Orders;
import com.example.demo.entity.User;
import com.example.demo.service.AdminService;
import com.example.demo.service.OrdersService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public Result<IPage<User>> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        
        Page<User> page = new Page<>(current, size);
        return Result.success(adminService.searchUsers(page, keyword));
    }

    @PutMapping("/user/status/{id}/{status}")
    public Result<String> changeUserStatus(@PathVariable Long id, @PathVariable Integer status) {
        try {
            adminService.updateUserStatus(id, status);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/toggleUser")
    public Result<String> toggleUserStatus(@RequestBody UserStatusDTO dto) {
        try {
            adminService.updateUserStatus(dto.getUserId(), dto.getStatus());
            String action = dto.getStatus() == 0 ? "解封" : "封禁";
            return Result.success("用户已成功" + action);
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/user/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        boolean success = userService.removeById(id);
        return success ? Result.success("账号已永久删除") : Result.error("删除失败");
    }

    @GetMapping("/stats/daily")
    public Result<List<Map<String, Object>>> getDailyStats() {
        QueryWrapper<Orders> qw = new QueryWrapper<>();
        qw.select("DATE(create_time) as date", "COUNT(*) as count", "SUM(price) as totalAmount")
          .groupBy("DATE(create_time)")
          .orderByDesc("DATE(create_time)")
          .last("LIMIT 7");
        
        List<Map<String, Object>> stats = ordersService.listMaps(qw);
        return Result.success(stats);
    }
    
    @GetMapping("/user/details/{id}")
    public Result<UserDetailsDTO> getUserDetails(@PathVariable Long id) {
        try {
            UserDetailsDTO details = adminService.getUserCompleteDetails(id);
            return Result.success(details);
        } catch (Exception e) {
            return Result.error("获取用户详情失败: " + e.getMessage());
        }
    }
}