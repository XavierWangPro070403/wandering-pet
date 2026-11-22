package com.wanderingpet.module.sys.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 角色权限分配入参
 */
@Data
public class RolePermAssignDTO {

    @NotEmpty(message = "请选择权限")
    private List<Long> permIds;
}
