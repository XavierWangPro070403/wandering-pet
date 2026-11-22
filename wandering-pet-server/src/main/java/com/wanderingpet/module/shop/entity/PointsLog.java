package com.wanderingpet.module.shop.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 积分流水 */
@Data
@TableName("points_log")
public class PointsLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 积分变动（正获得/负消耗）；列名 point_change（change 为 MySQL 关键字） */
    private Integer pointChange;

    /** 变动后余额 */
    private Integer balance;

    /** EARN-消费获得 EXCHANGE-兑换优惠券 */
    private String type;

    private String remark;

    private Long orderId;

    private Long couponId;

    private LocalDateTime createTime;
}
