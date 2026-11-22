package com.wanderingpet.module.sys.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典项出参
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictItemVO {

    private String code;

    private String label;
}
