package com.wanderingpet.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.user.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户地址 Mapper
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
