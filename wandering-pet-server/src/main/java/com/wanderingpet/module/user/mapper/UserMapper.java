package com.wanderingpet.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * C 端用户 Mapper（phone/id_card_no 读写自动加解密）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
