package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.dto.UserDetailsDTO;
import com.example.demo.dto.UserStatusDTO;
import com.example.demo.entity.Bargain;
import com.example.demo.entity.Goods;
import com.example.demo.entity.Orders;
import com.example.demo.entity.User;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.AdminService;
import com.example.demo.service.BargainService;
import com.example.demo.service.OrdersService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
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
    @Autowired
    private BargainService bargainService;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private UserMapper userMapper;

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

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = adminService.getDashboardStats();
        return Result.success(stats);
    }

    @GetMapping("/dashboard/weekly")
    public Result<List<Map<String, Object>>> getWeeklyStats() {
        List<Map<String, Object>> stats = adminService.getWeeklyStats();
        return Result.success(stats);
    }

    @GetMapping("/dashboard/monthly")
    public Result<List<Map<String, Object>>> getMonthlyStats() {
        List<Map<String, Object>> stats = adminService.getMonthlyStats();
        return Result.success(stats);
    }

    @GetMapping("/dashboard/categories")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        List<Map<String, Object>> stats = adminService.getCategoryStats();
        return Result.success(stats);
    }

    @GetMapping("/dashboard/top-sellers")
    public Result<List<Map<String, Object>>> getTopSellers() {
        List<Map<String, Object>> sellers = adminService.getTopSellers();
        return Result.success(sellers);
    }

    @GetMapping("/dashboard/recent-orders")
    public Result<List<Map<String, Object>>> getRecentOrders() {
        List<Map<String, Object>> orders = adminService.getRecentOrders();
        return Result.success(orders);
    }

    @GetMapping("/bargains/pending")
    public Result<List<Map<String, Object>>> getPendingBargains() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Get all goods that are in active bargain status
        QueryWrapper<Bargain> qw = new QueryWrapper<>();
        qw.eq("status", 0).eq("is_deleted", 0).orderByDesc("create_time");
        List<Bargain> bargains = bargainService.list(qw);
        
        for (Bargain b : bargains) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("goodsId", b.getGoodsId());
            map.put("userId", b.getUserId());
            map.put("originalPrice", b.getOriginalPrice());
            map.put("targetPrice", b.getTargetPrice());
            map.put("createTime", b.getCreateTime());
            
            // Get goods title
            Goods goods = goodsMapper.selectById(b.getGoodsId());
            if (goods != null) {
                map.put("goodsTitle", goods.getTitle());
            }
            
            // Get buyer name
            User buyer = userMapper.selectById(b.getUserId());
            if (buyer != null) {
                map.put("buyerName", buyer.getUsername());
            }
            
            result.add(map);
        }
        
        return Result.success(result);
    }
}