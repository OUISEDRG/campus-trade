
package com.example.demo.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ChatSessionDTO {
    private Long targetUserId;
    private String targetUserName;
    private String targetUserAvatar;
    private Date lastMessageTime;
    private String lastMessage;
    private Integer unreadCount;
    private Long latestGoodsId;
    private String latestGoodsTitle;
}
