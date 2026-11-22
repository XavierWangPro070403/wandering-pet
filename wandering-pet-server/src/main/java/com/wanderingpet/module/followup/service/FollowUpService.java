package com.wanderingpet.module.followup.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.module.followup.dto.FollowUpSubmitDTO;
import com.wanderingpet.module.followup.vo.FollowUpVO;

/**
 * 回访服务
 */
public interface FollowUpService {

    /** 领养人填写回访表（任务归属校验，PENDING/OVERDUE → DONE） */
    void submit(Long id, FollowUpSubmitDTO dto);

    /** 我的回访任务列表 */
    PageResult<FollowUpVO> pageMy(Integer pageNum, Integer pageSize, String status);

    /** 机构回访任务分页（关联宠物/领养人） */
    PageResult<FollowUpVO> pageShelter(Integer pageNum, Integer pageSize, String status, Integer round);

    /** 机构催办（发送站内提醒） */
    void remind(Long id);

    /** 机构归档（催办后确认线下沟通完成 → DONE） */
    void archive(Long id);
}
