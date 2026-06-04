package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Exchange;
import com.example.demo.entity.Goods;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.mapper.ExchangeMapper;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.ExchangeService;
import com.example.demo.service.MessageService;
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
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private MessageService messageService;

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
        
        // 向目标用户发送交换通知
        User initiator = userMapper.selectById(initiatorId);
        String initiatorName = (initiator != null) ? initiator.getName() : "用户";
        messageService.sendMessageDirect(
            targetUserId,
            "收到交换请求",
            "用户「" + initiatorName + "」希望用「" + initiatorGoods.getTitle() + "」交换您的「" + targetGoods.getTitle() + "」" + (message != null && !message.isEmpty() ? "，留言：" + message : ""),
            "exchange"
        );
        
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
            
            // 发送交换成功通知
            messageService.sendMessageDirect(
                exchange.getInitiatorId(),
                "交换成功",
                "您的交换请求已被接受，商品「" + initiatorGoods.getTitle() + "」和「" + targetGoods.getTitle() + "」已交换归属",
                "exchange"
            );
        } else if (status == 2) {
            // 发送交换被拒绝通知
            Goods initiatorGoods = goodsMapper.selectById(exchange.getInitiatorGoodsId());
            Goods targetGoods = goodsMapper.selectById(exchange.getTargetGoodsId());
            messageService.sendMessageDirect(
                exchange.getInitiatorId(),
                "交换被拒绝",
                "您的交换请求（用「" + initiatorGoods.getTitle() + "」交换「" + targetGoods.getTitle() + "」）已被对方拒绝",
                "exchange"
            );
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