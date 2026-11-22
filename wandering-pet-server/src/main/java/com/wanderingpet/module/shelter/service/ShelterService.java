package com.wanderingpet.module.shelter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.module.shelter.dto.ShelterApplyDTO;
import com.wanderingpet.module.shelter.dto.ShelterAuditDTO;
import com.wanderingpet.module.shelter.dto.ShelterUpdateDTO;
import com.wanderingpet.module.shelter.vo.AdminShelterVO;
import com.wanderingpet.module.shelter.vo.ShelterSimpleVO;
import com.wanderingpet.module.shelter.vo.ShelterVO;

import java.util.List;

/**
 * 救助站服务
 */
public interface ShelterService {

    /** 已入驻（审核通过）救助站公开列表 */
    List<ShelterVO> listApproved(String city);

    /** 救助站详情 */
    ShelterVO detail(Long id);

    /** C 端用户申请入驻（创建待审机构 + 机构账号，初始密码 123456） */
    Long apply(ShelterApplyDTO dto);

    /** 机构自己维护信息 */
    void updateMine(ShelterUpdateDTO dto);

    /** 机构自己的信息 */
    ShelterVO myShelter();

    // ---------- 管理端 ----------

    /** 分页（按审核状态筛选） */
    com.wanderingpet.common.result.PageResult<AdminShelterVO> pageAdmin(Integer pageNum, Integer pageSize, Integer auditStatus, String keyword);

    /** 入驻审核 */
    void audit(Long id, ShelterAuditDTO dto);

    /** 内部：简要信息 */
    ShelterSimpleVO simple(Long id);
}
