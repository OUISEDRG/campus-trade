
package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.Orders;
import com.example.demo.entity.User;
import com.example.demo.service.OrdersService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    
    @Autowired
    private UserService userService;

    @PostMapping("/buy")
    public Result buy(@RequestParam Integer goodsId, @RequestParam Long buyerId) {
        try {
            ordersService.buy(goodsId, buyerId);
            return Result.success("购买成功！请去个人中心查看");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/buyerInfo/{goodsId}")
    public Result<Map<String, Object>> getBuyerInfo(@PathVariable Integer goodsId) {
        QueryWrapper<Orders> qw = new QueryWrapper<>();
        qw.eq("goods_id", goodsId);
        Orders order = ordersService.getOne(qw);
        
        if (order == null) {
            return Result.error("未找到对应的订单记录");
        }
        
        User buyer = userService.getById(order.getBuyerId());
        if (buyer == null) {
            return Result.error("买家账户异常或已注销");
        }
        
        Map<String, Object> buyerInfo = new HashMap<>();
        buyerInfo.put("name", buyer.getName() != null ? buyer.getName() : buyer.getUsername());
        buyerInfo.put("phone", buyer.getPhone() != null ? buyer.getPhone() : "未留存联系电话");
        buyerInfo.put("address", buyer.getCollege() != null ? buyer.getCollege() : "未填写详细收货地址");
        buyerInfo.put("orderTime", order.getCreateTime());
        buyerInfo.put("postalCode", "校园面交免邮编");
        
        return Result.success(buyerInfo);
    }
}
