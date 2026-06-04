package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Bargain;
import com.example.demo.entity.BargainRecord;

import java.math.BigDecimal;
import java.util.List;

public interface BargainService extends IService<Bargain> {

    Bargain createBargain(Long goodsId, Long userId, BigDecimal targetPrice);
    
    BargainRecord participateBargain(Long bargainId, Long userId);
    
    Bargain getBargainById(Long bargainId);
    
    List<Bargain> getBargainsByGoodsId(Long goodsId);
    
    List<BargainRecord> getBargainRecords(Long bargainId);
    
    boolean cancelBargain(Long bargainId);
    
    // 商家同意砍价 - 自动更新商品价格为砍价目标价
    Bargain approveBargain(Long bargainId);
    
    // 商家拒绝砍价
    Bargain rejectBargain(Long bargainId);
    
    // 获取商家收到的待处理砍价请求
    List<Bargain> getPendingBargainsBySellerId(Long sellerId);
}