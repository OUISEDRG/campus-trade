package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Exchange;

import java.util.List;

public interface ExchangeService extends IService<Exchange> {

    Exchange createExchange(Long initiatorId, Long targetUserId, Long initiatorGoodsId, Long targetGoodsId, String message);
    
    Exchange respondExchange(Long exchangeId, Integer status);
    
    Exchange getExchangeById(Long exchangeId);
    
    List<Exchange> getExchangesByUserId(Long userId);
    
    List<Exchange> getPendingExchanges(Long userId);
    
    boolean cancelExchange(Long exchangeId);
}