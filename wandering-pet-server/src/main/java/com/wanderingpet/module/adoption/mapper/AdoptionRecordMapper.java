package com.wanderingpet.module.adoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.adoption.entity.AdoptionRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 领养正式记录 Mapper
 */
@Mapper
public interface AdoptionRecordMapper extends BaseMapper<AdoptionRecord> {
}
