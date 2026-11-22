package com.wanderingpet.module.shelter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.pet.entity.Pet;
import com.wanderingpet.module.pet.mapper.PetMapper;
import com.wanderingpet.module.shelter.dto.ShelterApplyDTO;
import com.wanderingpet.module.shelter.dto.ShelterAuditDTO;
import com.wanderingpet.module.shelter.dto.ShelterUpdateDTO;
import com.wanderingpet.module.shelter.entity.Shelter;
import com.wanderingpet.module.shelter.mapper.ShelterMapper;
import com.wanderingpet.module.shelter.service.ShelterService;
import com.wanderingpet.module.shelter.vo.AdminShelterVO;
import com.wanderingpet.module.shelter.vo.ShelterSimpleVO;
import com.wanderingpet.module.shelter.vo.ShelterVO;
import com.wanderingpet.module.sys.entity.SysUser;
import com.wanderingpet.module.sys.mapper.SysUserMapper;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 救助站服务实现
 */
@Service
@RequiredArgsConstructor
public class ShelterServiceImpl implements ShelterService {

    private final ShelterMapper shelterMapper;
    private final PetMapper petMapper;
    private final SysUserMapper sysUserMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Override
    public List<ShelterVO> listApproved(String city) {
        LambdaQueryWrapper<Shelter> wrapper = new LambdaQueryWrapper<Shelter>()
                .eq(Shelter::getAuditStatus, BizConstant.SHELTER_AUDIT_PASS)
                .eq(city != null && !city.isBlank(), Shelter::getCity, city)
                .orderByDesc(Shelter::getCreateTime);
        return shelterMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public ShelterVO detail(Long id) {
        Shelter shelter = shelterMapper.selectById(id);
        if (shelter == null || shelter.getAuditStatus() != BizConstant.SHELTER_AUDIT_PASS) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        return toVO(shelter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(ShelterApplyDTO dto) {
        LoginUser current = UserContext.current();
        if (current == null || !current.isUser()) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        // 同一账号存在待审机构时不允许重复提交
        Long pending = shelterMapper.selectCount(new LambdaQueryWrapper<Shelter>()
                .eq(Shelter::getAuditStatus, BizConstant.SHELTER_AUDIT_PENDING));
        Shelter exists = shelterMapper.selectOne(new LambdaQueryWrapper<Shelter>()
                .eq(Shelter::getLicenseNo, dto.getLicenseNo())
                .last("LIMIT 1"));
        if (exists != null) {
            throw BizException.of(ResultCode.BAD_REQUEST, "该证号已提交过入驻申请");
        }
        Shelter shelter = new Shelter();
        BeanUtils.copyProperties(dto, shelter);
        shelter.setAuditStatus(BizConstant.SHELTER_AUDIT_PENDING);
        shelter.setAuditRemark("");
        shelterMapper.insert(shelter);

        // 创建机构账号（用户名默认为联系人手机号，初始密码 123456，登录后可改）
        String username = dto.getAccountUsername() == null || dto.getAccountUsername().isBlank()
                ? dto.getContactPhone() : dto.getAccountUsername();
        SysUser account = new SysUser();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode("123456"));
        account.setRealName(dto.getContactName());
        account.setPhone(dto.getContactPhone());
        account.setRoleCode(AuthConstant.USER_TYPE_SHELTER);
        account.setShelterId(shelter.getId());
        account.setStatus(1);
        sysUserMapper.insert(account);

        // 通知平台管理员
        List<SysUser> admins = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRoleCode, AuthConstant.USER_TYPE_ADMIN));
        for (SysUser admin : admins) {
            notificationService.send(admin.getId(), AuthConstant.USER_TYPE_ADMIN,
                    "救助站入驻申请待审核", "%s 已提交入驻申请，请前往“机构审核”处理。".formatted(shelter.getName()),
                    BizConstant.BIZ_SHELTER, shelter.getId());
        }
        return shelter.getId();
    }

    @Override
    public void updateMine(ShelterUpdateDTO dto) {
        LoginUser current = UserContext.current();
        if (current == null || current.getShelterId() == null) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        Shelter shelter = shelterMapper.selectById(current.getShelterId());
        if (shelter == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        if (dto.getLogo() != null) {
            shelter.setLogo(dto.getLogo());
        }
        if (dto.getIntro() != null) {
            shelter.setIntro(dto.getIntro());
        }
        if (dto.getContactName() != null) {
            shelter.setContactName(dto.getContactName());
        }
        if (dto.getContactPhone() != null) {
            shelter.setContactPhone(dto.getContactPhone());
        }
        if (dto.getAddress() != null) {
            shelter.setAddress(dto.getAddress());
        }
        shelterMapper.updateById(shelter);
    }

    @Override
    public ShelterVO myShelter() {
        LoginUser current = UserContext.current();
        if (current == null || current.getShelterId() == null) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        Shelter shelter = shelterMapper.selectById(current.getShelterId());
        if (shelter == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        return toVO(shelter);
    }

    @Override
    public com.wanderingpet.common.result.PageResult<AdminShelterVO> pageAdmin(Integer pageNum, Integer pageSize, Integer auditStatus, String keyword) {
        LambdaQueryWrapper<Shelter> wrapper = new LambdaQueryWrapper<Shelter>()
                .eq(auditStatus != null, Shelter::getAuditStatus, auditStatus)
                .like(keyword != null && !keyword.isBlank(), Shelter::getName, keyword)
                .orderByDesc(Shelter::getCreateTime);
        Page<Shelter> page = shelterMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)), wrapper);
        return com.wanderingpet.common.result.PageResult.of(page, this::toAdminVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, ShelterAuditDTO dto) {
        LoginUser current = UserContext.current();
        Shelter shelter = shelterMapper.selectById(id);
        if (shelter == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        shelter.setAuditStatus(Boolean.TRUE.equals(dto.getPass())
                ? BizConstant.SHELTER_AUDIT_PASS : BizConstant.SHELTER_AUDIT_REJECT);
        shelter.setAuditRemark(dto.getRemark() == null ? "" : dto.getRemark());
        shelter.setAuditTime(LocalDateTime.now());
        shelter.setAuditBy(current.getUserId());
        shelterMapper.updateById(shelter);

        // 通知机构账号
        SysUser account = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getShelterId, id).last("LIMIT 1"));
        if (account != null) {
            String title = Boolean.TRUE.equals(dto.getPass()) ? "入驻审核已通过" : "入驻审核未通过";
            String content = Boolean.TRUE.equals(dto.getPass())
                    ? "%s 已通过平台入驻审核，现在可以使用机构工作台了。".formatted(shelter.getName())
                    : "%s 入驻审核未通过：%s".formatted(shelter.getName(), shelter.getAuditRemark());
            notificationService.send(account.getId(), AuthConstant.USER_TYPE_SHELTER, title, content,
                    BizConstant.BIZ_SHELTER, shelter.getId());
        }
    }

    @Override
    public ShelterSimpleVO simple(Long id) {
        Shelter shelter = shelterMapper.selectById(id);
        if (shelter == null) {
            return null;
        }
        ShelterSimpleVO vo = new ShelterSimpleVO();
        BeanUtils.copyProperties(shelter, vo);
        vo.setContactPhone(shelter.getContactPhone());
        return vo;
    }

    private ShelterVO toVO(Shelter shelter) {
        ShelterVO vo = new ShelterVO();
        BeanUtils.copyProperties(shelter, vo);
        Long petCount = petMapper.selectCount(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getShelterId, shelter.getId())
                .eq(Pet::getStatus, "AVAILABLE"));
        vo.setPetCount(petCount);
        return vo;
    }

    private AdminShelterVO toAdminVO(Shelter shelter) {
        AdminShelterVO vo = new AdminShelterVO();
        BeanUtils.copyProperties(shelter, vo);
        vo.setAuditTime(shelter.getAuditTime() == null ? null : shelter.getAuditTime().toString());
        vo.setCreateTime(shelter.getCreateTime() == null ? null : shelter.getCreateTime().toString());
        SysUser account = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getShelterId, shelter.getId()).last("LIMIT 1"));
        vo.setAccountUsername(account == null ? null : account.getUsername());
        return vo;
    }
}
