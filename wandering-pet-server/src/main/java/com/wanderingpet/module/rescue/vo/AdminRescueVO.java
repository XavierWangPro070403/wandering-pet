package com.wanderingpet.module.rescue.vo;

import lombok.Data;

/**
 * 管理端线索出参
 */
@Data
public class AdminRescueVO {

    private Long id;

    private String reportNo;

    private Long userId;

    private String userNickname;

    private Long categoryId;

    private String categoryName;

    private String description;

    private java.util.List<String> images;

    private String province;

    private String city;

    private String district;

    private String address;

    private Integer urgency;

    private String status;

    private Long handlerId;

    private String handlerName;

    private String handleRemark;

    private String handleTime;

    private String createTime;
}
