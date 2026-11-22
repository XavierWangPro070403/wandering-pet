package com.wanderingpet.module.sys.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限树节点
 */
@Data
public class PermissionTreeVO {

    private Long id;

    private String permCode;

    private String permName;

    private String permType;

    private Long parentId;

    private String path;

    private Integer sort;

    private List<PermissionTreeVO> children = new ArrayList<>();
}
