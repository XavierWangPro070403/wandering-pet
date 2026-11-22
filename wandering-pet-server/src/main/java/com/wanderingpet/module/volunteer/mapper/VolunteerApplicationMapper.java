package com.wanderingpet.module.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.volunteer.entity.VolunteerApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 志愿者报名 Mapper
 */
@Mapper
public interface VolunteerApplicationMapper extends BaseMapper<VolunteerApplication> {
}
