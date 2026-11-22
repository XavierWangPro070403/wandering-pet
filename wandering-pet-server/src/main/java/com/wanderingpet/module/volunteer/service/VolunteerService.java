package com.wanderingpet.module.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.module.volunteer.dto.VolunteerApplyDTO;
import com.wanderingpet.module.volunteer.dto.VolunteerAuditDTO;
import com.wanderingpet.module.volunteer.vo.VolunteerVO;

/**
 * 志愿者服务
 */
public interface VolunteerService {

    /** 报名（USER） */
    Long apply(VolunteerApplyDTO dto);

    /** 我的报名列表 */
    PageResult<VolunteerVO> pageMy(Integer pageNum, Integer pageSize);

    /** 机构报名审核分页 */
    PageResult<VolunteerVO> pageShelter(Integer pageNum, Integer pageSize, String status);

    /** 机构审核 */
    void audit(Long id, VolunteerAuditDTO dto);
}
