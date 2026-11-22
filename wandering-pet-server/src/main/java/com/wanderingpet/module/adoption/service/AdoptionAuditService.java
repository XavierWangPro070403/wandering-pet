package com.wanderingpet.module.adoption.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.module.adoption.dto.ApplicationQueryDTO;
import com.wanderingpet.module.adoption.dto.AuditActionDTO;
import com.wanderingpet.module.adoption.dto.HomeVisitDTO;
import com.wanderingpet.module.adoption.dto.InterviewDTO;
import com.wanderingpet.module.adoption.dto.ReturnDTO;
import com.wanderingpet.module.adoption.dto.SignDTO;
import com.wanderingpet.module.adoption.vo.AdoptionRecordVO;
import com.wanderingpet.module.adoption.vo.ApplicationListVO;

/**
 * 领养审核服务（机构视角 + 管理端查询）
 *
 * <p>状态机：SUBMITTED → REVIEWING → INTERVIEW → HOME_VISIT → APPROVED → SIGNED → ADOPTED；
 * 任意审核阶段可 REJECTED；领养完成后可 RETURNED。</p>
 */
public interface AdoptionAuditService {

    /** 机构申请分页（仅本机构） */
    PageResult<ApplicationListVO> pageShelter(ApplicationQueryDTO query);

    /** 管理端申请分页 */
    PageResult<ApplicationListVO> pageAdmin(ApplicationQueryDTO query);

    /** 受理：SUBMITTED → REVIEWING */
    void accept(Long id);

    /** 排期面谈：REVIEWING → INTERVIEW */
    void interview(Long id, InterviewDTO dto);

    /** 排期家访：INTERVIEW → HOME_VISIT */
    void homeVisit(Long id, HomeVisitDTO dto);

    /** 审核通过：HOME_VISIT → APPROVED */
    void approve(Long id, AuditActionDTO dto);

    /** 驳回：REVIEWING/INTERVIEW/HOME_VISIT → REJECTED */
    void reject(Long id, AuditActionDTO dto);

    /** 签约：APPROVED → SIGNED，生成领养记录 */
    void sign(Long id, SignDTO dto);

    /** 完成领养：SIGNED → ADOPTED，宠物置为 ADOPTED，生成 4 次回访任务 */
    void complete(Long id);

    /** 退养：记录置 RETURNED，宠物回到可领养 */
    void returnAdoption(Long recordId, ReturnDTO dto);

    /** 机构领养记录分页 */
    PageResult<AdoptionRecordVO> pageRecords(Long shelterId, Integer pageNum, Integer pageSize, String status);
}
