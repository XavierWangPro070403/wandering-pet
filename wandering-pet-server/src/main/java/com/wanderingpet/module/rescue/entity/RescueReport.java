package com.wanderingpet.module.rescue.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 流浪动物上报线索
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rescue_report")
public class RescueReport extends BaseEntity {

    private String reportNo;

    private Long userId;

    private Long categoryId;

    private String description;

    /** 现场图片 JSON 数组 */
    private String images;

    private String province;

    private String city;

    private String district;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    /** 1-一般 2-紧急 3-危急 */
    private Integer urgency;

    /** PENDING / ASSIGNED / PROCESSING / RESOLVED / CLOSED */
    private String status;

    /** 处理人（救助站账号 ID） */
    private Long handlerId;

    private String handleRemark;

    private LocalDateTime handleTime;
}
