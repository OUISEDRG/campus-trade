package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.service.ReviewService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public Result<?> addReview(@RequestBody Review review) {
        // 检查是否已评价
        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<>();
        qw.eq(Review::getOrderId, review.getOrderId())
          .eq(Review::getReviewerId, review.getReviewerId());
        if (reviewService.count(qw) > 0) {
            return Result.error("您已评价过该订单");
        }
        review.setCreateTime(LocalDateTime.now());
        reviewService.save(review);
        return Result.success("评价成功");
    }

    @GetMapping("/user/{userId}")
    public Result<?> getUserReviews(@PathVariable Long userId) {
        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<>();
        qw.eq(Review::getTargetUserId, userId).orderByDesc(Review::getCreateTime);
        List<Review> reviews = reviewService.list(qw);
        
        // 计算平均评分
        double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("reviews", reviews);
        result.put("avgRating", Math.round(avgRating * 10) / 10.0);
        result.put("totalReviews", reviews.size());
        
        return Result.success(result);
    }
}