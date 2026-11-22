package com.wanderingpet.module.adoption.vo;

import com.wanderingpet.module.followup.vo.FollowUpVO;
import lombok.Data;

import java.util.List;

/**
 * 领养正式记录出参（含回访任务）
 */
@Data
public class AdoptionRecordVO {

    private Long id;

    private String adoptionNo;

    private Long applicationId;

    private Long petId;

    private String petName;

    private String petCover;

    private Long userId;

    private String userNickname;

    private Long shelterId;

    private String shelterName;

    private String signDate;

    /** 协议预签名 URL（私有桶，30 分钟有效） */
    private String agreementUrl;

    private String status;

    private String returnReason;

    private String returnTime;

    private String createTime;

    /** 关联回访任务 */
    private List<FollowUpVO> followUps;
}
