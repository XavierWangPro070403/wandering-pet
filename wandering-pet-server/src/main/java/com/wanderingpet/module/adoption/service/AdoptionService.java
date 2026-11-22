package com.wanderingpet.module.adoption.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.module.adoption.dto.ApplicationQueryDTO;
import com.wanderingpet.module.adoption.dto.ApplicationSubmitDTO;
import com.wanderingpet.module.adoption.vo.AdoptionRecordVO;
import com.wanderingpet.module.adoption.vo.ApplicationDetailVO;
import com.wanderingpet.module.adoption.vo.ApplicationListVO;
import com.wanderingpet.module.adoption.vo.FlowLogVO;

import java.util.List;

/**
 * 领养申请服务（申请人视角）
 */
public interface AdoptionService {

    /**
     * 提交领养申请
     *
     * @param dto 申请信息
     * @return 申请 ID
     * @throws BizException 3001 存在进行中的申请 / 2002 宠物不可申请
     */
    Long submit(ApplicationSubmitDTO dto);

    /** 撤回申请（SUBMITTED/REVIEWING → CANCELED，释放宠物锁定） */
    void cancel(Long id);

    /** 我的申请分页 */
    PageResult<ApplicationListVO> pageMy(ApplicationQueryDTO query);

    /** 申请详情（本人/机构/管理员可见） */
    ApplicationDetailVO detail(Long id);

    /** 状态流水 */
    List<FlowLogVO> flowLogs(Long id);

    /** 我的领养记录（含回访任务） */
    List<AdoptionRecordVO> myRecords();

    /** 领养记录详情 */
    AdoptionRecordVO recordDetail(Long recordId);
}
