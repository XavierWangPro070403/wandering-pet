package com.wanderingpet.module.adoption.dto;

import lombok.Data;

/**
 * 领养申请列表查询入参（申请人/机构/管理端共用）
 */
@Data
public class ApplicationQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    /** 状态筛选 */
    private String status;

    /** 宠物 ID */
    private Long petId;

    /** 关键词（申请编号/宠物名/申请人姓名模糊） */
    private String keyword;

    /** 机构端固定传当前机构；管理端可传 */
    private Long shelterId;

    /** 申请人视角固定传当前用户 */
    private Long userId;
}
