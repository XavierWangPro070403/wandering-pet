package com.wanderingpet.module.donation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.donation.entity.DonationProject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 募捐项目 Mapper
 */
@Mapper
public interface DonationProjectMapper extends BaseMapper<DonationProject> {
}
