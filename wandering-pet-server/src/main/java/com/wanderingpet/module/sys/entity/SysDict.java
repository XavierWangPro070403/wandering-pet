package com.wanderingpet.module.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDict extends BaseEntity {

    /** 字典类型，如 pet_size */
    private String dictType;

    /** 字典编码，如 SMALL */
    private String dictCode;

    /** 字典标签，如 小型 */
    private String dictLabel;

    private Integer sort;

    /** 1-启用 0-停用 */
    private Integer status;
}
