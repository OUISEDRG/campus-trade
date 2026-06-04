package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Exchange;
import com.example.demo.entity.Goods;
import com.example.demo.exception.CustomException;
import com.example.demo.mapper.ExchangeMapper;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExchangeServiceImpl extends ServiceImpl<ExchangeMapper, Exchange> implements ExchangeService {

    @Autowired
    private ExchangeMapper exchangeMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    @Transactional
    public Exchange createExchange(Long initiatorId, Long targetUserId, Long initiatorGoodsId, Long targetGoodsId, String message) {
        if (initiatorId.equals(targetUserId)) {
            throw new CustomException("不能与自己交换");
        }
        
        Goods initiatorGoods = goodsMapper.selectById(initiatorGoodsId);
        if (initiatorGoods == null) {
            throw new CustomException("发起方商品不存在");
        }
        if (!initiatorGoods.getUserId().equals(initiatorId)) {
            throw new CustomException("您不是该商品的所有者");
        }
        
        Goods targetGoods = goodsMapper.selectById(targetGoodsId);
        if (targetGoods == null) {
            throw new CustomException("目标商品不存在");
        }
        if (!targetGoods.getUserId().equals(targetUserId)) {
            throw new CustomException("目标用户不是该商品的所有者");
        }
        
        Exchange exchange = new Exchange();
        exchange.setInitiatorId(initiatorId);
        exchange.setTargetUserId(targetUserId);
        exchange.setInitiatorGoodsId(initiatorGoodsId);
        exchange.setTargetGoodsId(targetGoodsId);
        exchange.setStatus(0);
        exchange.setMessage(message);
        exchange.setCreateTime(LocalDateTime.now());
        
        exchangeMapper.insert(exchange);
        return exchange;
    }

    @Override
    @Transactional
    public Exchange respondExchange(Long exchangeId, Integer status) {
        Exchange exchange = exchangeMapper.selectById(exchangeId);
        if (exchange == null) {
            throw new CustomException("交换请求不存在");
        }
        
        if (exchange.getStatus() != 0) {
            throw new CustomException("交换请求已处理");
        }
        
        exchange.setStatus(status);
        exchange.setRespondTime(LocalDateTime.now());
        
        exchangeMapper.updateById(exchange);
        
        if (status == 1) {
            Goods initiatorGoods = goodsMapper.selectById(exchange.getInitiatorGoodsId());
            Goods targetGoods = goodsMapper.selectById(exchange.getTargetGoodsId());
            
            initiatorGoods.setUserId(exchange.getTargetUserId());
            targetGoods.setUserId(exchange.getInitiatorId());
            
            goodsMapper.updateById(initiatorGoods);
            goodsMapper.updateById(targetGoods);
        }
        
        return exchange;
    }

    @Override
    public Exchange getExchangeById(Long exchangeId) {
        return exchangeMapper.selectById(exchangeId);
    }

    @Override
    public List<Exchange> getExchangesByUserId(Long userId) {
        QueryWrapper<Exchange> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(qw -> qw.eq("initiator_id", userId).or().eq("target_user_id", userId))
                   .orderByDesc("create_time");
        return exchangeMapper.selectList(queryWrapper);
    }

    @Override
    public List<Exchange> getPendingExchanges(Long userId) {
        QueryWrapper<Exchange> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_user_id", userId).eq("status", 0)
                   .orderByDesc("create_time");
        return exchangeMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean cancelExchange(Long exchangeId) {
        Exchange exchange = exchangeMapper.selectById(exchangeId);
        if (exchange == null) {
            return false;
        }
        exchange.setStatus(3);
        return exchangeMapper.updateById(exchange) > 0;
    }
}