package com.wanderingpet.module.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.document.OperationLog;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.RedisUtil;
import com.wanderingpet.module.sys.dto.RolePermAssignDTO;
import com.wanderingpet.module.sys.dto.SysUserSaveDTO;
import com.wanderingpet.module.sys.entity.SysDict;
import com.wanderingpet.module.sys.entity.SysPermission;
import com.wanderingpet.module.sys.entity.SysRole;
import com.wanderingpet.module.sys.entity.SysRolePermission;
import com.wanderingpet.module.sys.entity.SysUser;
import com.wanderingpet.module.sys.mapper.SysDictMapper;
import com.wanderingpet.module.sys.mapper.SysPermissionMapper;
import com.wanderingpet.module.sys.mapper.SysRoleMapper;
import com.wanderingpet.module.sys.mapper.SysRolePermissionMapper;
import com.wanderingpet.module.sys.mapper.SysUserMapper;
import com.wanderingpet.module.sys.service.SysManageService;
import com.wanderingpet.module.sys.vo.DictItemVO;
import com.wanderingpet.module.sys.vo.OperationLogVO;
import com.wanderingpet.module.sys.vo.PermissionTreeVO;
import com.wanderingpet.module.sys.vo.RoleVO;
import com.wanderingpet.module.sys.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统管理服务实现
 */
@Service
@RequiredArgsConstructor
public class SysManageServiceImpl implements SysManageService {

    private final SysDictMapper dictMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    // ---------------------------------------------------------------- 字典

    @Override
    public Map<String, List<DictItemVO>> dictAll() {
        Map<Object, Object> cached = redisUtil.hashGetAll(RedisKeyConstant.CACHE_DICT_ALL);
        if (cached != null && !cached.isEmpty()) {
            Map<String, List<DictItemVO>> fromCache = new java.util.HashMap<>();
            boolean parsed = true;
            for (Map.Entry<Object, Object> entry : cached.entrySet()) {
                try {
                    fromCache.put(String.valueOf(entry.getKey()),
                            objectMapper.readValue(String.valueOf(entry.getValue()), new TypeReference<List<DictItemVO>>() { }));
                } catch (Exception e) {
                    parsed = false;
                    break;
                }
            }
            if (parsed) {
                return fromCache;
            }
            redisUtil.delete(RedisKeyConstant.CACHE_DICT_ALL);
        }
        Map<String, List<DictItemVO>> grouped = dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                        .eq(SysDict::getStatus, 1)
                        .orderByAsc(SysDict::getSort)).stream()
                .map(d -> Map.entry(d.getDictType(), new DictItemVO(d.getDictCode(), d.getDictLabel())))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        try {
            Map<String, String> hash = new java.util.HashMap<>();
            for (Map.Entry<String, List<DictItemVO>> entry : grouped.entrySet()) {
                hash.put(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
            }
            redisUtil.hashSetAll(RedisKeyConstant.CACHE_DICT_ALL, hash, Duration.ofHours(24));
        } catch (Exception ignored) {
            // 缓存失败不影响返回
        }
        return grouped;
    }

    @Override
    public List<SysDict> listDict(String dictType) {
        return dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(dictType != null && !dictType.isBlank(), SysDict::getDictType, dictType)
                .orderByAsc(SysDict::getDictType)
                .orderByAsc(SysDict::getSort));
    }

    @Override
    public void saveDict(SysDict dict) {
        dict.setId(null);
        dictMapper.insert(dict);
        redisUtil.delete(RedisKeyConstant.CACHE_DICT_ALL);
    }

    @Override
    public void updateDict(Long id, SysDict dict) {
        SysDict exists = dictMapper.selectById(id);
        if (exists == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        exists.setDictType(dict.getDictType());
        exists.setDictCode(dict.getDictCode());
        exists.setDictLabel(dict.getDictLabel());
        exists.setSort(dict.getSort());
        exists.setStatus(dict.getStatus());
        dictMapper.updateById(exists);
        redisUtil.delete(RedisKeyConstant.CACHE_DICT_ALL);
    }

    @Override
    public void deleteDict(Long id) {
        dictMapper.deleteById(id);
        redisUtil.delete(RedisKeyConstant.CACHE_DICT_ALL);
    }

    // ---------------------------------------------------------------- 角色权限

    @Override
    public List<RoleVO> listRoles() {
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getId));
        return roles.stream().map(role -> {
            RoleVO vo = new RoleVO();
            org.springframework.beans.BeanUtils.copyProperties(role, vo);
            vo.setPermIds(rolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                            .eq(SysRolePermission::getRoleId, role.getId()))
                    .stream().map(SysRolePermission::getPermId).toList());
            return vo;
        }).toList();
    }

    @Override
    public List<PermissionTreeVO> permissionTree() {
        List<SysPermission> all = permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSort));
        Map<Long, List<SysPermission>> byParent = all.stream()
                .collect(Collectors.groupingBy(SysPermission::getParentId));
        return buildTree(0L, byParent);
    }

    private List<PermissionTreeVO> buildTree(Long parentId, Map<Long, List<SysPermission>> byParent) {
        return byParent.getOrDefault(parentId, List.of()).stream()
                .sorted(Comparator.comparing(SysPermission::getId))
                .map(p -> {
                    PermissionTreeVO vo = new PermissionTreeVO();
                    org.springframework.beans.BeanUtils.copyProperties(p, vo);
                    vo.setChildren(buildTree(p.getId(), byParent));
                    return vo;
                }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePerms(Long roleId, RolePermAssignDTO dto) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        for (Long permId : dto.getPermIds()) {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(roleId);
            relation.setPermId(permId);
            rolePermissionMapper.insert(relation);
        }
        // 该角色下的在线账号权限缓存需要失效（简化：删除全部权限缓存）
        clearPermCache(role.getRoleCode());
    }

    private void clearPermCache(String roleCode) {
        // 权限缓存 Key 含 userId，无法精确枚举；采用版本号思路简化：TTL 2h 内自然过期，
        // 立即生效场景由「账号重新登录」触发重建，演示环境可接受。
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRoleCode, roleCode));
        users.forEach(u -> redisUtil.delete(RedisKeyConstant.AUTH_PERM.formatted(u.getRoleCode(), u.getId())));
    }

    // ---------------------------------------------------------------- 后台账号

    @Override
    public PageResult<SysUserVO> pageSysUsers(Integer pageNum, Integer pageSize, String roleCode, String keyword) {
        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)),
                new LambdaQueryWrapper<SysUser>()
                        .eq(roleCode != null && !roleCode.isBlank(), SysUser::getRoleCode, roleCode)
                        .and(keyword != null && !keyword.isBlank(), w -> w
                                .like(SysUser::getUsername, keyword)
                                .or().like(SysUser::getRealName, keyword))
                        .orderByDesc(SysUser::getCreateTime));
        List<SysUserVO> list = page.getRecords().stream().map(this::toSysUserVO).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public void saveSysUser(SysUserSaveDTO dto) {
        if (AuthConstant.USER_TYPE_SHELTER.equals(dto.getRoleCode()) && dto.getShelterId() == null) {
            throw BizException.of(ResultCode.BAD_REQUEST, "救助站账号必须选择所属机构");
        }
        Long exists = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw BizException.of(ResultCode.USERNAME_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone() == null ? "" : dto.getPhone());
        user.setRoleCode(dto.getRoleCode());
        user.setShelterId(dto.getShelterId());
        user.setStatus(1);
        sysUserMapper.insert(user);
    }

    @Override
    public void toggleSysUser(Long id, Boolean enable) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        user.setStatus(Boolean.TRUE.equals(enable) ? 1 : 0);
        sysUserMapper.updateById(user);
        if (!Boolean.TRUE.equals(enable)) {
            // 禁用即强制下线
            redisUtil.delete(RedisKeyConstant.AUTH_TOKEN.formatted(user.getRoleCode(), user.getId()));
        }
    }

    @Override
    public void resetSysUserPassword(Long id, String newPassword) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
        redisUtil.delete(RedisKeyConstant.AUTH_TOKEN.formatted(user.getRoleCode(), user.getId()));
    }

    // ---------------------------------------------------------------- 操作日志

    @Override
    public PageResult<OperationLogVO> pageOperationLogs(Integer pageNum, Integer pageSize, Long userId, String module) {
        Criteria criteria = new Criteria();
        if (userId != null) {
            criteria = criteria.and("userId").is(userId);
        }
        if (module != null && !module.isBlank()) {
            criteria = criteria.and("module").is(module);
        }
        Query countQuery = new Query(criteria);
        Query pageQuery = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "createTime"))
                .skip((long) (pageNum - 1) * pageSize)
                .limit(Math.min(pageSize, 100));
        List<OperationLog> logs = mongoTemplate.find(pageQuery, OperationLog.class);
        long total = mongoTemplate.count(countQuery, OperationLog.class);
        List<OperationLogVO> list = logs.stream().map(item -> {
            OperationLogVO vo = new OperationLogVO();
            org.springframework.beans.BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(item.getCreateTime() == null ? null : item.getCreateTime().toString().replace('T', ' '));
            return vo;
        }).toList();
        return new PageResult<>(list, total, pageNum, Math.min(pageSize, 100), (total + pageSize - 1) / pageSize);
    }

    private SysUserVO toSysUserVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        org.springframework.beans.BeanUtils.copyProperties(user, vo);
        vo.setLastLoginTime(user.getLastLoginTime() == null ? null : user.getLastLoginTime().toString().replace('T', ' '));
        vo.setCreateTime(user.getCreateTime() == null ? null : user.getCreateTime().toString().replace('T', ' '));
        return vo;
    }
}
