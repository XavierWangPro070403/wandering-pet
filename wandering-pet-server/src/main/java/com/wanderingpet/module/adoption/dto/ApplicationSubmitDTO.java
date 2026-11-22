package com.wanderingpet.module.adoption.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 领养申请提交入参
 */
@Data
public class ApplicationSubmitDTO {

    @NotNull(message = "请选择宠物")
    private Long petId;

    @NotBlank(message = "请输入申请人姓名")
    @Size(max = 32)
    private String applicantName;

    @NotBlank(message = "请输入联系电话")
    private String applicantPhone;

    @NotNull(message = "请填写年龄")
    @Min(value = 18, message = "领养人须年满 18 周岁")
    @Max(value = 100, message = "请填写正确的年龄")
    private Integer applicantAge;

    @Size(max = 32)
    private String occupation;

    @NotBlank(message = "请填写现居住城市")
    private String city;

    @NotBlank(message = "请选择住房类型")
    private String housingType;

    @NotNull(message = "请选择是否有养宠经验")
    private Integer hasPetExperience;

    @NotNull(message = "请确认家人是否同意")
    private Integer familyAgree;

    @NotNull(message = "请选择家中是否有儿童")
    private Integer hasChildren;

    @NotNull(message = "请选择是否有过敏史")
    private Integer hasAllergy;

    @NotBlank(message = "请填写领养原因")
    @Size(max = 500, message = "领养原因最长 500 字")
    private String reason;
}
