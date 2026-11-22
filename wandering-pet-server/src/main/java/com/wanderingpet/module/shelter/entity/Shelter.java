package com.wanderingpet.module.shelter.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 救助站（机构）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shelter")
public class Shelter extends BaseEntity {

    private String name;

    private String logo;

    /** 统一社会信用代码/登记证号 */
    private String licenseNo;

    private String contactName;

    private String contactPhone;

    private String province;

    private String city;

    private String district;

    /** 仅到区+街道，不展示门牌号 */
    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String intro;

    /** 0-待审核 1-已通过 2-已驳回 */
    private Integer auditStatus;

    private String auditRemark;

    private LocalDateTime auditTime;

    /** 审核人（sys_user.id） */
    private Long auditBy;
}
