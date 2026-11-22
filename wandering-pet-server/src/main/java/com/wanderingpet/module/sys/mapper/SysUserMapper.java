package com.wanderingpet.module.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.sys.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台账号 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
