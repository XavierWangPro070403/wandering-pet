package com.wanderingpet.module.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.sys.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据字典 Mapper
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {
}
