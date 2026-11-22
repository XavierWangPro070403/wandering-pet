package com.wanderingpet.module.followup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.followup.entity.FollowUp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 回访记录 Mapper
 */
@Mapper
public interface FollowUpMapper extends BaseMapper<FollowUp> {
}
