package com.wanderingpet.module.pet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 宠物档案保存入参（发布/编辑）
 */
@Data
public class PetSaveDTO {

    @NotBlank(message = "请输入宠物名字")
    @Size(max = 32)
    private String name;

    @NotNull(message = "请选择物种")
    private Long categoryId;

    private Long breedId;

    @NotBlank(message = "请选择性别")
    private String gender;

    @NotNull(message = "请填写月龄")
    private Integer ageMonth;

    private BigDecimal weight;

    @NotBlank(message = "请选择体型")
    private String size;

    @Size(max = 32)
    private String color;

    private Integer sterilized;

    private Integer vaccinated;

    private Integer dewormed;

    @NotBlank(message = "请选择健康状况")
    private String healthStatus;

    @Size(max = 255)
    private String characterDesc;

    @Size(max = 512)
    private String adoptionRequirements;

    private String story;

    @NotBlank(message = "请上传封面图")
    private String coverImage;

    /** 相册（不含封面），对象名 + URL + 排序 */
    private List<ImageItem> images;

    /** 保存后状态：AVAILABLE（直接发布）/ OFFLINE（存为下架） */
    private String status;
}
