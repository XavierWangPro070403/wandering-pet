<template>
  <div class="admin-settings">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1>系统设置</h1>
        <p class="page-head__desc">配置角色权限、数据字典，并查看后台操作日志。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- ① 角色权限 -->
      <el-tab-pane label="角色权限" name="roles">
        <div v-if="rolesLoading" class="pane-loading"><el-skeleton :rows="8" animated /></div>
        <div v-else-if="rolesError" class="pane-loading">
          <EmptyState title="角色数据加载失败" desc="请检查网络后重试。" action-text="重新加载" @action="loadRoleData" />
        </div>
        <div v-else class="role-pane">
          <div class="role-pane__list" aria-label="角色列表">
            <button
              v-for="role in roles"
              :key="role.id"
              class="role-item"
              :class="{ active: selectedRole && selectedRole.id === role.id }"
              @click="selectRole(role)">
              <p class="role-item__name">{{ role.roleName }}</p>
              <p class="role-item__code">{{ role.roleCode }}</p>
            </button>
          </div>
          <div v-if="selectedRole" class="role-pane__tree">
            <div class="role-pane__tree-head">
              <div>
                <p class="role-pane__tree-title">{{ selectedRole.roleName }}</p>
                <p class="role-pane__tree-desc">{{ selectedRole.description || '该角色可访问的功能权限' }}</p>
              </div>
              <el-button
                type="primary"
                size="small"
                :loading="permSaving"
                @click="saveRolePerms">保存权限</el-button>
            </div>
            <el-tree
              v-if="treeData.length"
              ref="permTree"
              :key="selectedRole.id"
              :data="treeData"
              node-key="id"
              show-checkbox
              default-expand-all
              :props="treeProps"
              :default-checked-keys="checkedKeys" />
            <EmptyState v-else title="暂无权限项" />
          </div>
        </div>
      </el-tab-pane>

      <!-- ② 数据字典 -->
      <el-tab-pane label="数据字典" name="dict">
        <div class="filter-bar">
          <el-select
            v-model="dictQuery.dictType"
            placeholder="全部类型"
            clearable
            style="width: 200px"
            @change="searchDicts">
            <el-option v-for="t in dictTypes" :key="t" :label="t" :value="t" />
          </el-select>
          <el-button type="primary" class="filter-bar__main" @click="openDictDialog(null)">新增字典项</el-button>
        </div>

        <div class="table-card">
          <el-skeleton v-if="dictLoading && dictFirstLoad" :rows="6" animated />
          <el-table v-else v-loading="dictLoading" :data="pagedDicts" empty-text="">
            <template slot="empty">
              <EmptyState
                v-if="dictError"
                title="字典数据加载失败"
                desc="请检查网络后重试。"
                action-text="重新加载"
                @action="loadDicts" />
              <EmptyState v-else title="暂无字典项" desc="点击右上角「新增字典项」维护枚举值。" />
            </template>
            <el-table-column prop="dictType" label="类型" min-width="150" />
            <el-table-column prop="dictCode" label="编码" min-width="140" />
            <el-table-column prop="dictLabel" label="标签" min-width="140" />
            <el-table-column prop="sort" label="排序" width="80" align="center">
              <template slot-scope="{ row }"><span class="num">{{ row.sort }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template slot-scope="{ row }">
                <StatusTag :status="row.status === 1 ? 'APPROVED' : 'DRAFT'" :label="row.status === 1 ? '启用' : '停用'" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" label-class-name="op-col" fixed="right">
              <template slot-scope="{ row }">
                <div class="row-actions">
                  <el-button type="text" size="small" @click="openDictDialog(row)">编辑</el-button>
                  <el-button type="text" size="small" class="op-danger" @click="removeDict(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="filteredDicts.length > dictQuery.pageSize"
            class="pagination"
            layout="total, prev, pager, next"
            :total="filteredDicts.length"
            :page-size="dictQuery.pageSize"
            :current-page.sync="dictQuery.pageNum" />
        </div>
      </el-tab-pane>

      <!-- ③ 操作日志 -->
      <el-tab-pane label="操作日志" name="logs">
        <div class="filter-bar">
          <el-input
            v-model="logQuery.userId"
            placeholder="用户 ID"
            clearable
            style="width: 140px"
            @keyup.enter.native="searchLogs"
            @clear="searchLogs" />
          <el-select
            v-model="logQuery.module"
            placeholder="全部模块"
            clearable
            style="width: 160px"
            @change="searchLogs">
            <el-option v-for="m in moduleOptions" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
          <el-button type="primary" @click="searchLogs">查询</el-button>
          <el-button @click="resetLogs">重置</el-button>
        </div>

        <div class="table-card">
          <el-skeleton v-if="logLoading && logFirstLoad" :rows="6" animated />
          <el-table v-else v-loading="logLoading" :data="logs" empty-text="">
            <template slot="empty">
              <EmptyState
                v-if="logError"
                title="操作日志加载失败"
                desc="请检查网络后重试。"
                action-text="重新加载"
                @action="loadLogs" />
              <EmptyState v-else title="暂无操作日志" />
            </template>
            <el-table-column prop="createTime" label="时间" width="170" />
            <el-table-column prop="userId" label="用户" width="90">
              <template slot-scope="{ row }"><span class="num">{{ row.userId }}</span></template>
            </el-table-column>
            <el-table-column label="类型" width="110">
              <template slot-scope="{ row }">{{ userTypeText(row.userType) }}</template>
            </el-table-column>
            <el-table-column prop="module" label="模块" width="110">
              <template slot-scope="{ row }">{{ moduleText(row.module) }}</template>
            </el-table-column>
            <el-table-column prop="action" label="动作" min-width="160" show-overflow-tooltip />
            <el-table-column label="耗时" width="90" align="right">
              <template slot-scope="{ row }"><span class="num">{{ row.duration }} ms</span></template>
            </el-table-column>
            <el-table-column label="结果" width="80">
              <template slot-scope="{ row }">
                <StatusTag :status="row.success ? 'APPROVED' : 'REJECTED'" :label="row.success ? '成功' : '失败'" />
              </template>
            </el-table-column>
            <el-table-column prop="ip" label="IP" width="130">
              <template slot-scope="{ row }">{{ row.ip || '—' }}</template>
            </el-table-column>
            <el-table-column label="参数" width="220">
              <template slot-scope="{ row }">
                <el-tooltip :content="truncate(row.params, 120)" placement="top" :disabled="!row.params">
                  <el-button
                    v-if="row.params"
                    type="text"
                    size="small"
                    class="params-text"
                    @click="showParams(row)">{{ truncate(row.params, 40) }}</el-button>
                  <span v-else>—</span>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            class="pagination"
            layout="total, prev, pager, next, jumper"
            :total="logTotal"
            :page-size="logQuery.pageSize"
            :current-page.sync="logQuery.pageNum"
            @current-change="loadLogs" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 字典项新增 / 编辑 dialog -->
    <el-dialog
      :title="dictForm.id ? '编辑字典项' : '新增字典项'"
      :visible.sync="dictVisible"
      width="480px"
      :close-on-click-modal="false"
      @closed="$refs.dictFormRef && $refs.dictFormRef.clearValidate()">
      <el-form ref="dictFormRef" :model="dictForm" :rules="dictRules" label-position="top">
        <el-form-item label="字典类型" prop="dictType">
          <el-input
            v-model="dictForm.dictType"
            :disabled="!!dictForm.id"
            maxlength="50"
            placeholder="如 pet_size，编辑时不可修改" />
        </el-form-item>
        <el-form-item label="编码" prop="dictCode">
          <el-input v-model="dictForm.dictCode" maxlength="50" placeholder="如 SMALL" />
        </el-form-item>
        <el-form-item label="标签" prop="dictLabel">
          <el-input v-model="dictForm.dictLabel" maxlength="50" placeholder="如 小型" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="dictForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="dictForm.enable" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template slot="footer">
        <el-button @click="dictVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitDict">保存</el-button>
      </template>
    </el-dialog>

    <!-- 日志参数 dialog -->
    <el-dialog
      title="参数详情"
      :visible.sync="paramsVisible"
      width="560px">
      <pre class="params-json">{{ paramsPretty }}</pre>
      <template slot="footer">
        <el-button @click="paramsVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import {
  adminDictItems,
  createDictItem,
  updateDictItem,
  deleteDictItem,
  adminRoles,
  permissionTree,
  assignRolePerms,
  adminOperationLogs
} from '@/api/system';

const MODULE_OPTIONS = [
  { value: 'pet', label: '宠物' },
  { value: 'adoption', label: '领养' },
  { value: 'shelter', label: '机构' },
  { value: 'article', label: '文章' },
  { value: 'comment', label: '评论' },
  { value: 'file', label: '文件' },
  { value: 'donation', label: '捐赠' },
  { value: 'rescue', label: '线索' },
  { value: 'notification', label: '消息' },
  { value: 'sys-user', label: '后台账号' },
  { value: 'role', label: '角色' },
  { value: 'dict', label: '字典' }
];

const VALID_TABS = ['roles', 'dict', 'logs'];

/**
 * 管理端系统设置（/admin/settings）：角色权限 / 数据字典 / 操作日志
 */
export default {
  name: 'AdminSettings',
  components: { StatusTag, EmptyState },
  data() {
    return {
      activeTab: 'roles',
      // ---- 角色权限 ----
      rolesLoading: false,
      rolesError: false,
      roles: [],
      treeData: [],
      selectedRole: null,
      permSaving: false,
      treeProps: { label: 'permName', children: 'children' },
      // ---- 数据字典 ----
      dictLoading: false,
      dictFirstLoad: true,
      dictError: false,
      dictItems: [],
      dictQuery: { pageNum: 1, pageSize: 10, dictType: '' },
      dictVisible: false,
      submitting: false,
      dictForm: { id: null, dictType: '', dictCode: '', dictLabel: '', sort: 0, enable: true },
      // ---- 操作日志 ----
      logLoading: false,
      logFirstLoad: true,
      logError: false,
      logs: [],
      logTotal: 0,
      moduleOptions: MODULE_OPTIONS,
      logQuery: { pageNum: 1, pageSize: 10, userId: '', module: '' },
      paramsVisible: false,
      paramsRaw: '',
      loadedTabs: {}
    };
  },
  computed: {
    dictTypes() {
      const types = this.dictItems.map((item) => item.dictType).filter(Boolean);
      return Array.from(new Set(types));
    },
    filteredDicts() {
      if (!this.dictQuery.dictType) return this.dictItems;
      return this.dictItems.filter((item) => item.dictType === this.dictQuery.dictType);
    },
    pagedDicts() {
      const start = (this.dictQuery.pageNum - 1) * this.dictQuery.pageSize;
      return this.filteredDicts.slice(start, start + this.dictQuery.pageSize);
    },
    dictRules() {
      return {
        dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
        dictCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
        dictLabel: [{ required: true, message: '请输入标签', trigger: 'blur' }]
      };
    },
    /** 选中角色的权限 ID（仅保留树中存在的节点） */
    checkedKeys() {
      if (!this.selectedRole) return [];
      const ids = new Set(this.flatTreeNodes.map((node) => node.id));
      return (this.selectedRole.permIds || []).filter((id) => ids.has(id));
    },
    flatTreeNodes() {
      const result = [];
      const walk = (nodes) => {
        (nodes || []).forEach((node) => {
          result.push(node);
          walk(node.children);
        });
      };
      walk(this.treeData);
      return result;
    },
    paramsPretty() {
      if (!this.paramsRaw) return '';
      try {
        return JSON.stringify(JSON.parse(this.paramsRaw), null, 2);
      } catch (e) {
        return this.paramsRaw;
      }
    }
  },
  watch: {
    '$route.query.tab'(val) {
      const tab = VALID_TABS.includes(val) ? val : 'roles';
      if (tab !== this.activeTab) this.activeTab = tab;
    },
    activeTab(tab) {
      // 支持页内 ?tab= 同步与各 Tab 懒加载
      if (this.$route.query.tab !== tab) {
        this.$router.replace({ query: { ...this.$route.query, tab } }).catch(() => {});
      }
      this.ensureLoaded(tab);
    }
  },
  created() {
    const tab = VALID_TABS.includes(this.$route.query.tab) ? this.$route.query.tab : 'roles';
    this.activeTab = tab;
  },
  mounted() {
    this.ensureLoaded(this.activeTab);
  },
  methods: {
    userTypeText(type) {
      return { ADMIN: '平台管理员', SHELTER: '机构账号', USER: '用户' }[type] || type || '—';
    },
    moduleText(module) {
      const hit = MODULE_OPTIONS.find((m) => m.value === module);
      return hit ? hit.label : module || '—';
    },
    truncate(text, len) {
      const str = String(text || '');
      return str.length > len ? `${str.slice(0, len)}…` : str;
    },

    // ---------- Tab 加载（每个 Tab 首次激活时拉取） ----------
    ensureLoaded(tab) {
      if (this.loadedTabs[tab]) return;
      this.loadedTabs[tab] = true;
      if (tab === 'roles') this.loadRoleData();
      if (tab === 'dict') this.loadDicts();
      if (tab === 'logs') this.loadLogs();
    },

    // ---------- 角色权限 ----------
    async loadRoleData() {
      this.rolesLoading = true;
      this.rolesError = false;
      try {
        const [roles, tree] = await Promise.all([adminRoles(), permissionTree()]);
        this.roles = roles || [];
        this.treeData = tree || [];
        // 默认选中第一个角色
        if (!this.selectedRole || !this.roles.some((r) => r.id === this.selectedRole.id)) {
          this.selectedRole = this.roles[0] || null;
        } else {
          // 刷新已选角色的 permIds（保存后回显）
          const latest = this.roles.find((r) => r.id === this.selectedRole.id);
          this.selectedRole = latest || null;
        }
      } catch (e) {
        this.rolesError = true;
        this.roles = [];
        this.treeData = [];
      } finally {
        this.rolesLoading = false;
      }
    },
    selectRole(role) {
      this.selectedRole = role;
    },
    saveRolePerms() {
      if (!this.selectedRole || !this.$refs.permTree) return;
      const checkedKeys = this.$refs.permTree.getCheckedKeys();
      this.permSaving = true;
      assignRolePerms(this.selectedRole.id, checkedKeys)
        .then(() => {
          this.$message.success(`已保存「${this.selectedRole.roleName}」的权限`);
          return this.loadRoleData();
        })
        .catch(() => {})
        .finally(() => {
          this.permSaving = false;
        });
    },

    // ---------- 数据字典 ----------
    async loadDicts() {
      this.dictLoading = true;
      this.dictError = false;
      try {
        const data = await adminDictItems();
        this.dictItems = data || [];
      } catch (e) {
        this.dictError = true;
        this.dictItems = [];
      } finally {
        this.dictLoading = false;
        this.dictFirstLoad = false;
      }
    },
    searchDicts() {
      this.dictQuery.pageNum = 1;
    },
    openDictDialog(row) {
      if (row) {
        this.dictForm = {
          id: row.id,
          dictType: row.dictType,
          dictCode: row.dictCode,
          dictLabel: row.dictLabel,
          sort: row.sort || 0,
          enable: row.status === 1
        };
      } else {
        this.dictForm = {
          id: null,
          dictType: this.dictQuery.dictType || '',
          dictCode: '',
          dictLabel: '',
          sort: this.dictItems.length + 1,
          enable: true
        };
      }
      this.dictVisible = true;
    },
    submitDict() {
      this.$refs.dictFormRef.validate(async (valid) => {
        if (!valid) return;
        this.submitting = true;
        const payload = {
          dictType: this.dictForm.dictType,
          dictCode: this.dictForm.dictCode,
          dictLabel: this.dictForm.dictLabel,
          sort: this.dictForm.sort,
          status: this.dictForm.enable ? 1 : 0
        };
        try {
          if (this.dictForm.id) {
            await updateDictItem(this.dictForm.id, payload);
            this.$message.success('字典项已更新');
          } else {
            await createDictItem(payload);
            this.$message.success('字典项已新增');
          }
          this.dictVisible = false;
          this.loadDicts();
        } catch (e) {
          /* 错误提示由 request 统一处理 */
        } finally {
          this.submitting = false;
        }
      });
    },
    removeDict(row) {
      this.$confirm(`确认删除字典项「${row.dictType} / ${row.dictLabel}」吗？`, '删除确认', { type: 'warning' })
        .then(async () => {
          await deleteDictItem(row.id);
          this.$message.success('字典项已删除');
          this.loadDicts();
        })
        .catch(() => {});
    },

    // ---------- 操作日志 ----------
    async loadLogs() {
      this.logLoading = true;
      this.logError = false;
      try {
        const params = { pageNum: this.logQuery.pageNum, pageSize: this.logQuery.pageSize };
        const uid = parseInt(this.logQuery.userId, 10);
        if (!Number.isNaN(uid)) params.userId = uid;
        if (this.logQuery.module) params.module = this.logQuery.module;
        const data = await adminOperationLogs(params);
        this.logs = data.list || [];
        this.logTotal = data.total || 0;
      } catch (e) {
        this.logError = true;
        this.logs = [];
        this.logTotal = 0;
      } finally {
        this.logLoading = false;
        this.logFirstLoad = false;
      }
    },
    searchLogs() {
      this.logQuery.pageNum = 1;
      this.loadLogs();
    },
    resetLogs() {
      this.logQuery = { pageNum: 1, pageSize: 10, userId: '', module: '' };
      this.loadLogs();
    },
    showParams(row) {
      this.paramsRaw = row.params || '';
      this.paramsVisible = true;
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: $space-6;

  h1 {
    font-size: $font-w-h1;
    line-height: $lh-w-h1;
    color: $text-primary;
  }

  &__desc {
    margin-top: $space-2;
    font-size: $font-w-body;
    color: $text-secondary;
  }
}

.settings-tabs {
  min-height: 320px;
}

.filter-bar {
  @include card;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: $space-3;
  padding: $space-4 $space-5;
  margin-bottom: $space-4;

  &__main {
    margin-left: auto;
  }
}

.table-card {
  @include card;
  padding: $space-4;
}

.op-danger {
  color: $color-danger-text;
}

.params-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.params-json {
  margin: 0;
  max-height: 420px;
  overflow: auto;
  padding: $space-4;
  background: $bg-page;
  border-radius: $radius-base;
  font-size: $font-w-caption;
  line-height: $lh-w-caption;
  color: $text-regular;
  white-space: pre-wrap;
  word-break: break-all;
}

.pane-loading {
  padding: $space-6 0;
}

.role-pane {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: $space-5;
  min-height: 420px;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }

  &__list {
    display: flex;
    flex-direction: column;
    gap: $space-2;
  }

  &__tree {
    min-width: 0;
  }

  &__tree-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: $space-3;
    padding-bottom: $space-3;
    margin-bottom: $space-3;
    border-bottom: 1px solid $border-color;
  }

  &__tree-title {
    font-size: $font-w-h4;
    line-height: $lh-w-h4;
    font-weight: 600;
    color: $text-primary;
  }

  &__tree-desc {
    margin-top: 2px;
    font-size: $font-w-caption;
    color: $text-secondary;
  }
}

.role-item {
  text-align: left;
  border: 1px solid $border-color;
  background: $bg-card;
  border-radius: $radius-base;
  padding: $space-3 $space-4;
  cursor: pointer;
  transition: all $duration-hover-in $ease-out-quart;

  &:hover {
    border-color: $color-primary;
  }

  &.active {
    background: $color-primary-light;
    border-color: $color-primary;
  }

  &__name {
    font-size: $font-w-body;
    font-weight: 500;
    color: $text-primary;
  }

  &__code {
    margin-top: 2px;
    font-size: $font-w-caption;
    color: $text-secondary;
  }
}
</style>


