package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Bargain;
import com.example.demo.entity.BargainRecord;
import com.example.demo.service.BargainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bargain")
public class BargainController {

    @Autowired
    private BargainService bargainService;

    @PostMapping("/create")
    public Result<Bargain> createBargain(@RequestBody Map<String, Object> params) {
        Long goodsId = ((Number) params.get("goodsId")).longValue();
        Long userId = ((Number) params.get("userId")).longValue();
        BigDecimal targetPrice = new BigDecimal(params.get("targetPrice").toString());
        Integer maxParticipants = (Integer) params.get("maxParticipants");
        
        Bargain bargain = bargainService.createBargain(goodsId, userId, targetPrice, maxParticipants);
        return Result.success("创建成功", bargain);
    }

    @PostMapping("/participate")
    public Result<BargainRecord> participateBargain(@RequestBody Map<String, Object> params) {
        Long bargainId = ((Number) params.get("bargainId")).longValue();
        Long userId = ((Number) params.get("userId")).longValue();
        
        BargainRecord record = bargainService.participateBargain(bargainId, userId);
        return Result.success("参与成功", record);
    }

    @GetMapping("/{id}")
    public Result<Bargain> getBargain(@PathVariable Long id) {
        Bargain bargain = bargainService.getBargainById(id);
        if (bargain == null) {
            return Result.error("砍价活动不存在");
        }
        return Result.success(bargain);
    }

    @GetMapping("/goods/{goodsId}")
    public Result<List<Bargain>> getBargainsByGoods(@PathVariable Long goodsId) {
        List<Bargain> bargains = bargainService.getBargainsByGoodsId(goodsId);
        return Result.success(bargains);
    }

    @GetMapping("/{id}/records")
    public Result<List<BargainRecord>> getBargainRecords(@PathVariable Long id) {
        List<BargainRecord> records = bargainService.getBargainRecords(id);
        return Result.success(records);
    }

    @PostMapping("/{id}/cancel")
    public Result<String> cancelBargain(@PathVariable Long id) {
        boolean success = bargainService.cancelBargain(id);
        return success ? Result.success("已取消") : Result.error("取消失败");
    }
}