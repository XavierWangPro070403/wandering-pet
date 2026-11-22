<template>
  <div class="admin-users">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1>用户管理</h1>
        <p class="page-head__desc">维护领养用户与后台账号，异常账号可随时停用。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="role-tabs" @tab-click="onTabClick">
      <!-- C 端用户 -->
      <el-tab-pane label="C 端用户" name="members">
        <div class="filter-bar">
          <el-input
            v-model="memberQuery.keyword"
            placeholder="昵称 / 账号 / 手机号"
            clearable
            style="width: 240px"
            @keyup.enter.native="searchMembers"
            @clear="searchMembers" />
          <el-select
            v-model="memberQuery.status"
            placeholder="全部状态"
            clearable
            style="width: 140px"
            @change="searchMembers">
            <el-option label="已启用" :value="1" />
            <el-option label="已禁用" :value="0" />
          </el-select>
          <el-button type="primary" @click="searchMembers">查询</el-button>
          <el-button @click="resetMembers">重置</el-button>
        </div>

        <div class="table-card">
          <el-skeleton v-if="memberLoading && memberFirstLoad" :rows="6" animated />
          <el-table v-else v-loading="memberLoading" :data="members" empty-text="">
            <template slot="empty">
              <EmptyState
                v-if="memberError"
                title="用户列表加载失败"
                desc="请检查网络后重试。"
                action-text="重新加载"
                @action="loadMembers" />
              <EmptyState v-else title="暂无注册用户" />
            </template>
            <el-table-column prop="nickname" label="昵称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="username" label="账号" min-width="130" show-overflow-tooltip>
              <template slot-scope="{ row }">{{ row.username || '—' }}</template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="130">
              <template slot-scope="{ row }">{{ row.phone || '—' }}</template>
            </el-table-column>
            <el-table-column prop="city" label="城市" width="90">
              <template slot-scope="{ row }">{{ row.city || '—' }}</template>
            </el-table-column>
            <el-table-column label="实名状态" width="100">
              <template slot-scope="{ row }">
                <StatusTag :status="row.realNameStatus === 1 ? 'APPROVED' : 'DRAFT'"
                  :label="row.realNameStatus === 1 ? '已认证' : '未认证'" />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="{ row }">
                <el-switch
                  :value="row.status === 1"
                  @change="onToggleMember(row)" />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="170" />
          </el-table>

          <el-pagination
            class="pagination"
            layout="total, prev, pager, next, jumper"
            :total="memberTotal"
            :page-size="memberQuery.pageSize"
            :current-page.sync="memberQuery.pageNum"
            @current-change="loadMembers" />
        </div>
      </el-tab-pane>

      <!-- 后台账号 -->
      <el-tab-pane label="后台账号" name="sys">
        <div class="filter-bar">
          <el-select
            v-model="sysQuery.roleCode"
            placeholder="全部角色"
            clearable
            style="width: 150px"
            @change="searchSys">
            <el-option label="平台管理员" value="ADMIN" />
            <el-option label="机构账号" value="SHELTER" />
          </el-select>
          <el-input
            v-model="sysQuery.keyword"
            placeholder="用户名 / 姓名"
            clearable
            style="width: 220px"
            @keyup.enter.native="searchSys"
            @clear="searchSys" />
          <el-button type="primary" @click="searchSys">查询</el-button>
          <el-button @click="resetSys">重置</el-button>
          <el-button type="primary" class="filter-bar__main" @click="openCreate">新增后台账号</el-button>
        </div>

        <div class="table-card">
          <el-skeleton v-if="sysLoading && sysFirstLoad" :rows="6" animated />
          <el-table v-else v-loading="sysLoading" :data="sysUsers" empty-text="">
            <template slot="empty">
              <EmptyState
                v-if="sysError"
                title="后台账号加载失败"
                desc="请检查网络后重试。"
                action-text="重新加载"
                @action="loadSys" />
              <EmptyState v-else title="暂无后台账号" />
            </template>
            <el-table-column prop="username" label="用户名" min-width="130" show-overflow-tooltip />
            <el-table-column prop="realName" label="姓名" width="110" />
            <el-table-column prop="phone" label="电话" width="130">
              <template slot-scope="{ row }">{{ row.phone || '—' }}</template>
            </el-table-column>
            <el-table-column label="角色" width="170">
              <template slot-scope="{ row }">
                <div class="role-cell">
                  <StatusTag :status="row.roleCode === 'ADMIN' ? 'REVIEWING' : 'APPROVED'"
                    :label="row.roleCode === 'ADMIN' ? '平台管理员' : '机构账号'" />
                  <span v-if="row.shelterName" class="role-cell__shelter">{{ row.shelterName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template slot-scope="{ row }">
                <el-switch
                  :value="row.status === 1"
                  @change="onToggleSys(row)" />
              </template>
            </el-table-column>
            <el-table-column prop="lastLoginTime" label="最近登录" width="170">
              <template slot-scope="{ row }">{{ row.lastLoginTime || '—' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column label="操作" width="140" label-class-name="op-col" fixed="right">
              <template slot-scope="{ row }">
                <div class="row-actions">
                  <el-button type="text" size="small" @click="openReset(row)">重置密码</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            class="pagination"
            layout="total, prev, pager, next, jumper"
            :total="sysTotal"
            :page-size="sysQuery.pageSize"
            :current-page.sync="sysQuery.pageNum"
            @current-change="loadSys" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增后台账号 dialog -->
    <el-dialog
      title="新增后台账号"
      :visible.sync="createVisible"
      width="520px"
      :close-on-click-modal="false"
      @closed="$refs.createFormRef && $refs.createFormRef.clearValidate()">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" maxlength="32" placeholder="4-32 位，登录账号" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input v-model="createForm.password" maxlength="32" show-password placeholder="6-32 位" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="createForm.realName" maxlength="32" placeholder="账号使用者姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="createForm.phone" maxlength="20" placeholder="选填" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="createForm.roleCode" style="width: 100%" placeholder="请选择角色" @change="onRoleChange">
            <el-option label="平台管理员（ADMIN）" value="ADMIN" />
            <el-option label="机构账号（SHELTER）" value="SHELTER" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="createForm.roleCode === 'SHELTER'" label="所属机构" prop="shelterId">
          <el-select v-model="createForm.shelterId" style="width: 100%" placeholder="请选择已通过的机构" filterable>
            <el-option v-for="s in shelterOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="createForm.enable" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template slot="footer">
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">创建账号</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码 dialog -->
    <el-dialog
      title="重置密码"
      :visible.sync="resetVisible"
      width="440px"
      :close-on-click-modal="false"
      @closed="$refs.resetFormRef && $refs.resetFormRef.clearValidate()">
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-position="top">
        <p class="reset-target">账号：{{ resetTarget && resetTarget.username }}</p>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetForm.newPassword" maxlength="32" show-password placeholder="6-32 位" />
        </el-form-item>
      </el-form>
      <template slot="footer">
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReset">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import { adminUsers, toggleUser, adminSysUsers, createSysUser, toggleSysUser, resetSysUserPassword, adminShelters } from '@/api/system';

/**
 * 管理端用户管理（/admin/users）：C 端用户 + 后台账号
 */
export default {
  name: 'AdminUsers',
  components: { StatusTag, EmptyState },
  data() {
    return {
      activeTab: 'members',
      // ---- C 端用户 ----
      memberLoading: false,
      memberFirstLoad: true,
      memberError: false,
      members: [],
      memberTotal: 0,
      memberQuery: { pageNum: 1, pageSize: 10, keyword: '', status: null },
      // ---- 后台账号 ----
      sysLoading: false,
      sysFirstLoad: true,
      sysError: false,
      sysUsers: [],
      sysTotal: 0,
      sysQuery: { pageNum: 1, pageSize: 10, roleCode: '', keyword: '' },
      sysLoaded: false,
      // ---- 弹窗 ----
      createVisible: false,
      resetVisible: false,
      submitting: false,
      shelterOptions: [],
      createForm: { username: '', password: '', realName: '', phone: '', roleCode: '', shelterId: null, enable: true },
      resetTarget: null,
      resetForm: { newPassword: '' }
    };
  },
  computed: {
    createRules() {
      const rules = {
        username: [
          { required: true, message: '请输入登录账号', trigger: 'blur' },
          { min: 4, max: 32, message: '账号长度为 4-32 位', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入初始密码', trigger: 'blur' },
          { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' }
        ],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }]
      };
      if (this.createForm.roleCode === 'SHELTER') {
        rules.shelterId = [{ required: true, message: '请选择所属机构', trigger: 'change' }];
      }
      return rules;
    },
    resetRules() {
      return {
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' }
        ]
      };
    }
  },
  mounted() {
    this.loadMembers();
  },
  methods: {
    onTabClick(tab) {
      if (tab.name === 'sys' && !this.sysLoaded) {
        this.sysLoaded = true;
        this.loadSys();
      }
    },

    // ---------- C 端用户 ----------
    async loadMembers() {
      this.memberLoading = true;
      this.memberError = false;
      try {
        const params = { pageNum: this.memberQuery.pageNum, pageSize: this.memberQuery.pageSize };
        if (this.memberQuery.keyword) params.keyword = this.memberQuery.keyword;
        if (this.memberQuery.status !== null && this.memberQuery.status !== '') params.status = this.memberQuery.status;
        const data = await adminUsers(params);
        this.members = data.list || [];
        this.memberTotal = data.total || 0;
      } catch (e) {
        this.memberError = true;
        this.members = [];
        this.memberTotal = 0;
      } finally {
        this.memberLoading = false;
        this.memberFirstLoad = false;
      }
    },
    searchMembers() {
      this.memberQuery.pageNum = 1;
      this.loadMembers();
    },
    resetMembers() {
      this.memberQuery = { pageNum: 1, pageSize: 10, keyword: '', status: null };
      this.loadMembers();
    },
    /** 启用/禁用 C 端用户（switch 受控，确认后才调用接口并刷新） */
    onToggleMember(row) {
      const enable = row.status !== 1;
      const name = row.nickname || row.username || '该用户';
      this.$confirm(`确认${enable ? '启用' : '禁用'}用户「${name}」吗？`, '提示', { type: 'warning' })
        .then(async () => {
          await toggleUser(row.id, enable);
          this.$message.success(enable ? '已启用' : '已禁用');
          this.loadMembers();
        })
        .catch(() => {});
    },

    // ---------- 后台账号 ----------
    async loadSys() {
      this.sysLoading = true;
      this.sysError = false;
      try {
        const params = { pageNum: this.sysQuery.pageNum, pageSize: this.sysQuery.pageSize };
        if (this.sysQuery.roleCode) params.roleCode = this.sysQuery.roleCode;
        if (this.sysQuery.keyword) params.keyword = this.sysQuery.keyword;
        const data = await adminSysUsers(params);
        this.sysUsers = data.list || [];
        this.sysTotal = data.total || 0;
      } catch (e) {
        this.sysError = true;
        this.sysUsers = [];
        this.sysTotal = 0;
      } finally {
        this.sysLoading = false;
        this.sysFirstLoad = false;
      }
    },
    searchSys() {
      this.sysQuery.pageNum = 1;
      this.loadSys();
    },
    resetSys() {
      this.sysQuery = { pageNum: 1, pageSize: 10, roleCode: '', keyword: '' };
      this.loadSys();
    },
    onToggleSys(row) {
      const enable = row.status !== 1;
      this.$confirm(`确认${enable ? '启用' : '禁用'}账号「${row.username}」吗？`, '提示', { type: 'warning' })
        .then(async () => {
          await toggleSysUser(row.id, enable);
          this.$message.success(enable ? '已启用' : '已禁用');
          this.loadSys();
        })
        .catch(() => {});
    },
    async openCreate() {
      this.createForm = { username: '', password: '', realName: '', phone: '', roleCode: '', shelterId: null, enable: true };
      this.createVisible = true;
      if (!this.shelterOptions.length) {
        try {
          const data = await adminShelters({ pageNum: 1, pageSize: 100, auditStatus: 1 });
          this.shelterOptions = data.list || [];
        } catch (e) {
          /* 下拉为空时无法选择机构账号，提示由 request 统一处理 */
        }
      }
    },
    onRoleChange() {
      if (this.createForm.roleCode !== 'SHELTER') this.createForm.shelterId = null;
      if (this.$refs.createFormRef) this.$refs.createFormRef.clearValidate('shelterId');
    },
    submitCreate() {
      this.$refs.createFormRef.validate(async (valid) => {
        if (!valid) return;
        this.submitting = true;
        try {
          await createSysUser({
            username: this.createForm.username,
            password: this.createForm.password,
            realName: this.createForm.realName,
            phone: this.createForm.phone,
            roleCode: this.createForm.roleCode,
            shelterId: this.createForm.roleCode === 'SHELTER' ? this.createForm.shelterId : null,
            status: this.createForm.enable ? 1 : 0
          });
          this.$message.success('后台账号已创建');
          this.createVisible = false;
          this.searchSys();
        } catch (e) {
          /* 错误提示由 request 统一处理 */
        } finally {
          this.submitting = false;
        }
      });
    },
    openReset(row) {
      this.resetTarget = row;
      this.resetForm = { newPassword: '' };
      this.resetVisible = true;
    },
    submitReset() {
      this.$refs.resetFormRef.validate(async (valid) => {
        if (!valid) return;
        this.submitting = true;
        try {
          await resetSysUserPassword(this.resetTarget.id, this.resetForm.newPassword);
          this.$message.success('密码已重置');
          this.resetVisible = false;
        } catch (e) {
          /* 错误提示由 request 统一处理 */
        } finally {
          this.submitting = false;
        }
      });
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

.pagination {
  margin-top: $space-4;
  text-align: right;
}

.role-tabs {
  margin-top: -$space-2;
}

.role-cell {
  display: flex;
  align-items: center;
  gap: $space-2;

  &__shelter {
    font-size: $font-w-caption;
    color: $text-secondary;
  }
}

.reset-target {
  margin-bottom: $space-3;
  font-size: $font-w-body;
  color: $text-regular;
}
</style>


