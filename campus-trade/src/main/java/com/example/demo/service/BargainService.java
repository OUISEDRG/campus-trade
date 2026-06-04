package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Bargain;
import com.example.demo.entity.BargainRecord;

import java.math.BigDecimal;
import java.util.List;

public interface BargainService extends IService<Bargain> {

    Bargain createBargain(Long goodsId, Long userId, BigDecimal targetPrice, Integer maxParticipants);
    
    BargainRecord participateBargain(Long bargainId, Long userId);
    
    Bargain getBargainById(Long bargainId);
    
    List<Bargain> getBargainsByGoodsId(Long goodsId);
    
    List<BargainRecord> getBargainRecords(Long bargainId);
    
    boolean cancelBargain(Long bargainId);
}