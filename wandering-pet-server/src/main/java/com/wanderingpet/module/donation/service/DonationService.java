package com.wanderingpet.module.donation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.module.donation.dto.DonateDTO;
import com.wanderingpet.module.donation.dto.ProjectSaveDTO;
import com.wanderingpet.module.donation.vo.DonationRecordVO;
import com.wanderingpet.module.donation.vo.DonorRankVO;
import com.wanderingpet.module.donation.vo.ProjectVO;

import java.util.List;

/**
 * 募捐服务
 */
public interface DonationService {

    /** 进行中项目列表（门户） */
    List<ProjectVO> listOngoing();

    /** 项目详情（含机构名） */
    ProjectVO detail(Long id);

    /** 捐赠（模拟支付：立即成功，更新进度） */
    void donate(Long projectId, DonateDTO dto);

    /** 项目捐赠明细（公开，姓名脱敏 + 匿名） */
    List<DonationRecordVO> recordsOf(Long projectId, Integer limit);

    /** 捐赠榜 Top N */
    List<DonorRankVO> donorRank(Long projectId, Integer topN);

    // ---------- 机构端 ----------

    List<ProjectVO> myProjects();

    Long saveProject(ProjectSaveDTO dto);

    void updateProject(Long id, ProjectSaveDTO dto);

    void closeProject(Long id);

    /** 机构捐赠明细 */
    PageResult<DonationRecordVO> myRecords(Integer pageNum, Integer pageSize, Long projectId);

    // ---------- 管理端 ----------

    PageResult<DonationRecordVO> pageAdminRecords(Integer pageNum, Integer pageSize, Long projectId);
}
