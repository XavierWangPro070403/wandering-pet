package com.wanderingpet.module.adoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.adoption.entity.AdoptionFlowLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 领养状态流水 Mapper
 */
@Mapper
public interface AdoptionFlowLogMapper extends BaseMapper<AdoptionFlowLog> {
}
