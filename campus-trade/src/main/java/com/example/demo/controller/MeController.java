
package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.Goods;
import com.example.demo.entity.Orders;
import com.example.demo.service.GoodsService;
import com.example.demo.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/me")
public class MeController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrdersService ordersService;

    @GetMapping("/stats")
    public Result getStats(@RequestParam Long userId) {
        Map stats = new HashMap();
        QueryWrapper qw1 = new QueryWrapper();
        qw1.eq("user_id", userId);
        long publishCount = goodsService.count(qw1);
        
        QueryWrapper qw2 = new QueryWrapper();
        qw2.eq("user_id", userId);
        qw2.eq("status", 1);
        long soldCount = goodsService.count(qw2);
        
        QueryWrapper qw3 = new QueryWrapper();
        qw3.eq("buyer_id", userId);
        long boughtCount = ordersService.count(qw3);

        stats.put("publishCount", publishCount);
        stats.put("soldCount", soldCount);
        stats.put("boughtCount", boughtCount);
        return Result.success(stats);
    }

    @GetMapping("/goodsList")
    public Result getGoodsList(@RequestParam Long userId, @RequestParam String type) {
        if ("published".equals(type)) {
            QueryWrapper qw = new QueryWrapper();
            qw.eq("user_id", userId);
            qw.orderByDesc("id");
            List list = goodsService.list(qw);
            return Result.success(list);
            
        } else if ("sold".equals(type)) {
            QueryWrapper qw = new QueryWrapper();
            qw.eq("user_id", userId);
            qw.eq("status", 1);
            qw.orderByDesc("id");
            List list = goodsService.list(qw);
            return Result.success(list);
            
        } else if ("bought".equals(type)) {
            QueryWrapper qw = new QueryWrapper();
            qw.eq("buyer_id", userId);
            qw.orderByDesc("id");
            List ordersList = ordersService.list(qw);
            List goodsIds = new ArrayList();
            for (Object obj : ordersList) {
                Orders order = (Orders) obj;
                goodsIds.add(order.getGoodsId());
            }
            if (goodsIds.isEmpty()) {
                return Result.success(new ArrayList());
            }
            List list = goodsService.listByIds(goodsIds);
            return Result.success(list);
        }
        return Result.error("未知类型");
    }
}
