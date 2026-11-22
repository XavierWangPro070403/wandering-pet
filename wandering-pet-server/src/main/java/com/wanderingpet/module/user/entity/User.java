package com.wanderingpet.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import com.wanderingpet.common.util.AesTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * C 端用户（领养人）。phone / idCardNo 为 AES 加密存储，由 TypeHandler 加解密
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "`user`", autoResultMap = true)
public class User extends BaseEntity {

    /** 登录账号（Web 用户端注册，微信注册用户为空） */
    private String username;

    /** BCrypt 密文（微信注册用户为空） */
    private String password;

    /** 微信 openid */
    private String openid;

    private String nickname;

    private String avatar;

    /** AES 加密存储 */
    @TableField(typeHandler = AesTypeHandler.class)
    private String phone;

    /** 0-未知 1-男 2-女 */
    private Integer gender;

    private String city;

    /** AES 加密存储 */
    @TableField(typeHandler = AesTypeHandler.class)
    private String idCardNo;

    /** 实名姓名 */
    private String realName;

    /** 0-未认证 1-已认证 */
    private Integer realNameStatus;

    /** 领养申请人姓名（申请领养时资料补全，多宠物申请免重复填写） */
    private String adopterName;

    /** 领养申请人年龄 */
    private Integer adopterAge;

    /** 职业 */
    private String occupation;

    /** 住房类型 OWN-自有 RENT-租赁 */
    private String housingType;

    /** 有养宠经验 0-否 1-是 */
    private Integer hasPetExperience;

    /** 家人同意饲养 0-否 1-是 */
    private Integer familyAgree;

    /** 家中有儿童 0-否 1-是 */
    private Integer hasChildren;

    /** 有过敏史 0-否 1-是 */
    private Integer hasAllergy;

    /** 积分余额（1元消费=1积分） */
    private Integer points;

    /** 1-正常 0-禁用 */
    private Integer status;

    private LocalDateTime lastLoginTime;
}
