package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Bargain;
import com.example.demo.entity.BargainRecord;
import com.example.demo.entity.Goods;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.mapper.BargainMapper;
import com.example.demo.mapper.BargainRecordMapper;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.BargainService;
import com.example.demo.service.GoodsService;
import com.example.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class BargainServiceImpl extends ServiceImpl<BargainMapper, Bargain> implements BargainService {

    @Autowired
    private BargainMapper bargainMapper;
    
    @Autowired
    private BargainRecordMapper bargainRecordMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private GoodsService goodsService;
    
    @Autowired
    private MessageService messageService;
    
    private final Random random = new Random();

    @Override
    @Transactional
    public Bargain createBargain(Long goodsId, Long userId, BigDecimal targetPrice) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new CustomException("商品不存在");
        }
        
        if (targetPrice.compareTo(goods.getPrice()) >= 0) {
            throw new CustomException("目标价格必须低于原价");
        }
        
        Bargain bargain = new Bargain();
        bargain.setGoodsId(goodsId);
        bargain.setUserId(userId);
        bargain.setOriginalPrice(goods.getPrice());
        bargain.setCurrentPrice(goods.getPrice());
        bargain.setTargetPrice(targetPrice);
        bargain.setStatus(0);
        bargain.setStartTime(LocalDateTime.now());
        bargain.setEndTime(LocalDateTime.now().plusDays(1));
        bargain.setCreateTime(LocalDateTime.now());
        bargain.setIsDeleted(0);
        
        bargainMapper.insert(bargain);
        
        // 向商家发送砍价通知
        User user = userMapper.selectById(userId);
        String username = (user != null) ? user.getName() : "用户";
        messageService.sendMessageDirect(
            goods.getUserId(),
            "收到砍价请求",
            "用户「" + username + "」在商品「" + goods.getTitle() + "」发起了砍价，目标价格为 ¥" + targetPrice + "（原价 ¥" + goods.getPrice() + "）",
            "bargain"
        );
        
        return bargain;
    }

    @Override
    @Transactional
    public BargainRecord participateBargain(Long bargainId, Long userId) {
        Bargain bargain = bargainMapper.selectById(bargainId);
        if (bargain == null || bargain.getIsDeleted() == 1) {
            throw new CustomException("砍价活动不存在");
        }
        
        if (bargain.getStatus() != 0) {
            throw new CustomException("砍价活动已结束或已取消");
        }
        
        if (bargain.getCurrentParticipants() >= bargain.getMaxParticipants()) {
            throw new CustomException("参与人数已满");
        }
        
        QueryWrapper<BargainRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bargain_id", bargainId).eq("user_id", userId);
        if (bargainRecordMapper.selectCount(queryWrapper) > 0) {
            throw new CustomException("您已参与过此砍价活动");
        }
        
        BigDecimal range = bargain.getCurrentPrice().subtract(bargain.getTargetPrice());
        BigDecimal minCut = range.multiply(new BigDecimal("0.05"));
        BigDecimal maxCut = range.multiply(new BigDecimal("0.2"));
        
        double randomValue = random.nextDouble();
        BigDecimal cutAmount = minCut.add(maxCut.subtract(minCut).multiply(BigDecimal.valueOf(randomValue)));
        cutAmount = cutAmount.setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal afterPrice = bargain.getCurrentPrice().subtract(cutAmount);
        if (afterPrice.compareTo(bargain.getTargetPrice()) < 0) {
            afterPrice = bargain.getTargetPrice();
            cutAmount = bargain.getCurrentPrice().subtract(afterPrice);
        }
        
        bargain.setCurrentPrice(afterPrice);
        bargain.setCurrentParticipants(bargain.getCurrentParticipants() + 1);
        
        if (afterPrice.compareTo(bargain.getTargetPrice()) == 0) {
            bargain.setStatus(1);
        }
        
        bargainMapper.updateById(bargain);
        
        BargainRecord record = new BargainRecord();
        record.setBargainId(bargainId);
        record.setUserId(userId);
        record.setCutAmount(cutAmount);
        record.setAfterPrice(afterPrice);
        record.setCreateTime(LocalDateTime.now());
        
        bargainRecordMapper.insert(record);
        return record;
    }

    @Override
    public Bargain getBargainById(Long bargainId) {
        return bargainMapper.selectById(bargainId);
    }

    @Override
    public List<Bargain> getBargainsByGoodsId(Long goodsId) {
        QueryWrapper<Bargain> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("goods_id", goodsId).eq("is_deleted", 0);
        return bargainMapper.selectList(queryWrapper);
    }

    @Override
    public List<BargainRecord> getBargainRecords(Long bargainId) {
        QueryWrapper<BargainRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bargain_id", bargainId).orderByDesc("create_time");
        return bargainRecordMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean cancelBargain(Long bargainId) {
        Bargain bargain = bargainMapper.selectById(bargainId);
        if (bargain == null) {
            return false;
        }
        bargain.setStatus(2);
        bargain.setIsDeleted(1);
        return bargainMapper.updateById(bargain) > 0;
    }

    @Override
    @Transactional
    public Bargain approveBargain(Long bargainId) {
        Bargain bargain = bargainMapper.selectById(bargainId);
        if (bargain == null || bargain.getIsDeleted() == 1) {
            throw new CustomException("砍价活动不存在");
        }
        
        // 更新商品价格为砍价目标价
        Goods goods = goodsMapper.selectById(bargain.getGoodsId());
        if (goods != null) {
            goods.setPrice(bargain.getTargetPrice());
            goodsMapper.updateById(goods);
        }
        
        bargain.setStatus(1);
        bargainMapper.updateById(bargain);
        
        // 发送通知
        User user = userMapper.selectById(bargain.getUserId());
        String username = (user != null) ? user.getName() : "用户";
        messageService.sendMessageDirect(
            bargain.getUserId(),
            "砍价通过",
            "您在商品「" + (goods != null ? goods.getTitle() : "") + "」的砍价请求已被通过，商品价格已更新为 ¥" + bargain.getTargetPrice(),
            "bargain"
        );
        
        return bargain;
    }

    @Override
    @Transactional
    public Bargain rejectBargain(Long bargainId) {
        Bargain bargain = bargainMapper.selectById(bargainId);
        if (bargain == null || bargain.getIsDeleted() == 1) {
            throw new CustomException("砍价活动不存在");
        }
        
        bargain.setStatus(2);
        bargainMapper.updateById(bargain);
        
        // 发送通知
        Goods goods = goodsMapper.selectById(bargain.getGoodsId());
        messageService.sendMessageDirect(
            bargain.getUserId(),
            "砍价被拒绝",
            "您在商品「" + (goods != null ? goods.getTitle() : "") + "」的砍价请求已被拒绝",
            "bargain"
        );
        
        return bargain;
    }

    @Override
    public List<Bargain> getPendingBargainsBySellerId(Long sellerId) {
        // 先查询该商家所有商品
        QueryWrapper<Goods> goodsQuery = new QueryWrapper<>();
        goodsQuery.eq("user_id", sellerId).eq("is_deleted", 0);
        List<Goods> goodsList = goodsMapper.selectList(goodsQuery);
        
        if (goodsList.isEmpty()) {
            return List.of();
        }
        
        List<Integer> goodsIds = goodsList.stream()
                .map(Goods::getId)
                .collect(Collectors.toList());
        
        // 再查询这些商品下的待处理砍价
        QueryWrapper<Bargain> bargainQuery = new QueryWrapper<>();
        bargainQuery.in("goods_id", goodsIds).eq("status", 0).eq("is_deleted", 0);
        return bargainMapper.selectList(bargainQuery);
    }
}