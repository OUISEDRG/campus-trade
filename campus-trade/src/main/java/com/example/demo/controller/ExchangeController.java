package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Exchange;
import com.example.demo.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exchange")
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;

    @PostMapping("/create")
    public Result<Exchange> createExchange(@RequestBody Map<String, Object> params) {
        Long initiatorId = ((Number) params.get("initiatorId")).longValue();
        Long targetUserId = ((Number) params.get("targetUserId")).longValue();
        Long initiatorGoodsId = ((Number) params.get("initiatorGoodsId")).longValue();
        Long targetGoodsId = ((Number) params.get("targetGoodsId")).longValue();
        String message = (String) params.get("message");
        
        Exchange exchange = exchangeService.createExchange(initiatorId, targetUserId, initiatorGoodsId, targetGoodsId, message);
        return Result.success("交换请求已发送", exchange);
    }

    @PostMapping("/{id}/respond")
    public Result<Exchange> respondExchange(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = (Integer) params.get("status");
        Exchange exchange = exchangeService.respondExchange(id, status);
        String msg = status == 1 ? "已接受交换" : "已拒绝交换";
        return Result.success(msg, exchange);
    }

    @GetMapping("/{id}")
    public Result<Exchange> getExchange(@PathVariable Long id) {
        Exchange exchange = exchangeService.getExchangeById(id);
        if (exchange == null) {
            return Result.error("交换请求不存在");
        }
        return Result.success(exchange);
    }

    @GetMapping("/user/{userId}")
    public Result<List<Exchange>> getExchangesByUser(@PathVariable Long userId) {
        List<Exchange> exchanges = exchangeService.getExchangesByUserId(userId);
        return Result.success(exchanges);
    }

    @GetMapping("/user/{userId}/pending")
    public Result<List<Exchange>> getPendingExchanges(@PathVariable Long userId) {
        List<Exchange> exchanges = exchangeService.getPendingExchanges(userId);
        return Result.success(exchanges);
    }

    @PostMapping("/{id}/cancel")
    public Result<String> cancelExchange(@PathVariable Long id) {
        boolean success = exchangeService.cancelExchange(id);
        return success ? Result.success("已取消") : Result.error("取消失败");
    }
}