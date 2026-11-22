<template>
  <div class="admin-articles">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1>内容管理</h1>
        <p class="page-head__desc">维护平台资讯文章与用户评论，违规内容可下架或屏蔽。</p>
      </div>
      <el-button v-if="activeTab === 'articles'" type="primary" @click="openCreate">新建文章</el-button>
    </div>

    <el-tabs v-model="activeTab" class="content-tabs" @tab-click="onTabClick">
      <!-- 文章管理 -->
      <el-tab-pane label="文章管理" name="articles">
        <div class="filter-bar">
          <el-select
            v-model="articleQuery.status"
            placeholder="全部状态"
            clearable
            style="width: 130px"
            @change="searchArticles">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
          <el-select
            v-model="articleQuery.category"
            placeholder="全部分类"
            clearable
            style="width: 140px"
            @change="searchArticles">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
          <el-input
            v-model="articleQuery.keyword"
            placeholder="文章标题"
            clearable
            style="width: 220px"
            @keyup.enter.native="searchArticles"
            @clear="searchArticles" />
          <el-button type="primary" @click="searchArticles">查询</el-button>
          <el-button @click="resetArticles">重置</el-button>
        </div>

        <div class="table-card">
          <el-skeleton v-if="articleLoading && articleFirstLoad" :rows="6" animated />
          <el-table v-else v-loading="articleLoading" :data="articles" empty-text="">
            <template slot="empty">
              <EmptyState
                v-if="articleError"
                title="文章列表加载失败"
                desc="请检查网络后重试。"
                action-text="重新加载"
                @action="loadArticles" />
              <EmptyState v-else title="暂无文章" desc="点击右上角「新建文章」开始创作。" />
            </template>
            <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
            <el-table-column label="分类" width="110">
              <template slot-scope="{ row }">{{ articleCategoryText(row.category) }}</template>
            </el-table-column>
            <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
            <el-table-column label="浏览 / 点赞" width="120" align="center">
              <template slot-scope="{ row }">
                <span class="num">{{ row.viewCount || 0 }}</span>
                <span class="sep">/</span>
                <span class="num">{{ row.likeCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="置顶" width="80">
              <template slot-scope="{ row }">
                <span v-if="row.isTop === 1" class="top-chip">置顶</span>
                <span v-else class="top-chip top-chip--plain">—</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="96">
              <template slot-scope="{ row }">
                <StatusTag :status="articleStatusKey(row.status)" :label="articleStatusText(row.status)" />
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="170">
              <template slot-scope="{ row }">{{ row.publishTime || '—' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="290" label-class-name="op-col" fixed="right">
              <template slot-scope="{ row }">
                <div class="row-actions">
                  <el-button type="text" size="small" @click="openEdit(row)">编辑</el-button>
                  <el-button
                    v-if="row.status !== 1"
                    type="text"
                    size="small"
                    @click="changeStatus(row, 1)">发布</el-button>
                  <el-button
                    v-if="row.status === 1"
                    type="text"
                    size="small"
                    @click="changeStatus(row, 2)">下架</el-button>
                  <el-button type="text" size="small" @click="toggleTop(row)">{{ row.isTop === 1 ? '取消置顶' : '置顶' }}</el-button>
                  <el-button type="text" size="small" class="op-danger" @click="removeArticle(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            class="pagination"
            layout="total, prev, pager, next, jumper"
            :total="articleTotal"
            :page-size="articleQuery.pageSize"
            :current-page.sync="articleQuery.pageNum"
            @current-change="loadArticles" />
        </div>
      </el-tab-pane>

      <!-- 评论审核 -->
      <el-tab-pane label="评论审核" name="comments">
        <div class="filter-bar">
          <el-select
            v-model="commentQuery.status"
            placeholder="全部状态"
            clearable
            style="width: 130px"
            @change="searchComments">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已屏蔽" :value="2" />
          </el-select>
          <el-select
            v-model="commentQuery.targetType"
            placeholder="全部对象"
            clearable
            style="width: 140px"
            @change="searchComments">
            <el-option label="宠物档案" value="PET" />
            <el-option label="文章" value="ARTICLE" />
          </el-select>
          <el-button type="primary" @click="searchComments">查询</el-button>
          <el-button @click="resetComments">重置</el-button>
        </div>

        <div class="table-card">
          <el-skeleton v-if="commentLoading && commentFirstLoad" :rows="6" animated />
          <el-table v-else v-loading="commentLoading" :data="comments" empty-text="">
            <template slot="empty">
              <EmptyState
                v-if="commentError"
                title="评论列表加载失败"
                desc="请检查网络后重试。"
                action-text="重新加载"
                @action="loadComments" />
              <EmptyState v-else title="暂无评论" />
            </template>
            <el-table-column prop="userNickname" label="评论人" width="140" show-overflow-tooltip />
            <el-table-column prop="content" label="内容" min-width="260">
              <template slot-scope="{ row }">
                <el-tooltip :content="row.content" placement="top" :disabled="!row.content || row.content.length <= 30">
                  <span class="comment-text">{{ row.content }}</span>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="对象类型" width="100">
              <template slot-scope="{ row }">{{ row.targetType === 'PET' ? '宠物档案' : '文章' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="96">
              <template slot-scope="{ row }">
                <StatusTag :status="commentStatusKey(row.status)" :label="commentStatusText(row.status)" />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="170" />
            <el-table-column label="操作" width="190" label-class-name="op-col" fixed="right">
              <template slot-scope="{ row }">
                <div class="row-actions">
                  <el-button
                    v-if="row.status !== 1"
                    type="text"
                    size="small"
                    @click="audit(row, true)">通过</el-button>
                  <el-button
                    v-if="row.status !== 2"
                    type="text"
                    size="small"
                    class="op-danger"
                    @click="audit(row, false)">屏蔽</el-button>
                  <el-button type="text" size="small" class="op-danger" @click="removeComment(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            class="pagination"
            layout="total, prev, pager, next, jumper"
            :total="commentTotal"
            :page-size="commentQuery.pageSize"
            :current-page.sync="commentQuery.pageNum"
            @current-change="loadComments" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建 / 编辑文章 dialog -->
    <el-dialog
      :title="articleForm.id ? '编辑文章' : '新建文章'"
      :visible.sync="editorVisible"
      width="960px"
      top="4vh"
      :close-on-click-modal="false"
      class="article-dialog"
      @closed="onDialogClosed">
      <el-form ref="articleFormRef" :model="articleForm" :rules="articleRules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="articleForm.title" maxlength="128" show-word-limit placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="articleForm.category" style="width: 240px" placeholder="请选择分类">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面">
          <ImageUploader v-model="coverList" biz-type="article" :limit="1" />
          <p class="cover-tip">正文图片请在封面上传，编辑器内不再插入图片。</p>
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input
            v-model="articleForm.summary"
            type="textarea"
            :rows="2"
            maxlength="255"
            show-word-limit
            placeholder="列表页展示的摘要，建议 60 字以内" />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <div v-if="editorVisible" class="rich-editor">
            <Toolbar
              class="rich-editor__toolbar"
              :editor="editor"
              :default-config="toolbarConfig"
              mode="default" />
            <Editor
              class="rich-editor__content"
              v-model="articleForm.content"
              :default-config="editorConfig"
              mode="default"
              @onCreated="onEditorCreated" />
          </div>
        </el-form-item>
      </el-form>
      <template slot="footer">
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button :loading="submitting" @click="submitArticle(0)">存草稿</el-button>
        <el-button type="primary" :loading="submitting" @click="submitArticle(1)">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import '@wangeditor/editor/dist/css/style.css';
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import ImageUploader from '@/components/ImageUploader.vue';
import {
  adminArticles,
  articleDetail,
  saveArticle,
  updateArticle,
  deleteArticle,
  topArticle,
  changeArticleStatus,
  adminComments,
  auditComment,
  deleteComment
} from '@/api/article';
import { articleCategoryText } from '@/utils/format';

const CATEGORY_OPTIONS = [
  { value: 'RAISE', label: '饲养指导' },
  { value: 'KNOWLEDGE', label: '科普百科' },
  { value: 'NEWS', label: '平台动态' },
  { value: 'STORY', label: '救助故事' }
];

/**
 * 管理端内容管理（/admin/articles）：文章管理 + 评论审核
 */
export default {
  name: 'AdminArticles',
  components: { Editor, Toolbar, StatusTag, EmptyState, ImageUploader },
  data() {
    return {
      activeTab: 'articles',
      categoryOptions: CATEGORY_OPTIONS,
      // ---- 文章 ----
      articleLoading: false,
      articleFirstLoad: true,
      articleError: false,
      articles: [],
      articleTotal: 0,
      articleQuery: { pageNum: 1, pageSize: 10, status: null, category: '', keyword: '' },
      // ---- 评论 ----
      commentLoading: false,
      commentFirstLoad: true,
      commentError: false,
      comments: [],
      commentTotal: 0,
      commentQuery: { pageNum: 1, pageSize: 10, status: null, targetType: '' },
      commentLoaded: false,
      // ---- 编辑器 ----
      editorVisible: false,
      editor: null,
      toolbarConfig: {
        // 禁用编辑器内图片/视频插入，正文图片统一走封面（bizType=article）
        excludeKeys: ['group-image', 'group-video']
      },
      editorConfig: {
        placeholder: '请输入正文内容…'
      },
      coverList: [],
      articleForm: { id: null, title: '', category: '', summary: '', content: '' },
      submitting: false
    };
  },
  computed: {
    articleRules() {
      return {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }],
        summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
        content: [
          { required: true, message: '请输入正文', trigger: 'blur' },
          { validator: this.validateContent, trigger: 'change' }
        ]
      };
    }
  },
  mounted() {
    this.loadArticles();
  },
  beforeDestroy() {
    if (this.editor) {
      this.editor.destroy();
      this.editor = null;
    }
  },
  watch: {
    /** 关闭弹窗时销毁编辑器实例，避免重复创建导致泄漏 */
    editorVisible(val) {
      if (!val && this.editor) {
        this.editor.destroy();
        this.editor = null;
      }
    }
  },
  methods: {
    articleCategoryText,

    // ---------- 文章 ----------
    async loadArticles() {
      this.articleLoading = true;
      this.articleError = false;
      try {
        const params = { pageNum: this.articleQuery.pageNum, pageSize: this.articleQuery.pageSize };
        if (this.articleQuery.status !== null && this.articleQuery.status !== '') params.status = this.articleQuery.status;
        if (this.articleQuery.category) params.category = this.articleQuery.category;
        if (this.articleQuery.keyword) params.keyword = this.articleQuery.keyword;
        const data = await adminArticles(params);
        this.articles = data.list || [];
        this.articleTotal = data.total || 0;
      } catch (e) {
        this.articleError = true;
        this.articles = [];
        this.articleTotal = 0;
      } finally {
        this.articleLoading = false;
        this.articleFirstLoad = false;
      }
    },
    searchArticles() {
      this.articleQuery.pageNum = 1;
      this.loadArticles();
    },
    resetArticles() {
      this.articleQuery = { pageNum: 1, pageSize: 10, status: null, category: '', keyword: '' };
      this.loadArticles();
    },
    articleStatusKey(status) {
      return { 0: 'DRAFT', 1: 'PUBLISHED', 2: 'OFFLINE' }[status] || 'DRAFT';
    },
    articleStatusText(status) {
      return { 0: '草稿', 1: '已发布', 2: '已下架' }[status] || '草稿';
    },
    validateContent(rule, value, callback) {
      const text = String(value || '').replace(/<[^>]+>/g, '').trim();
      if (!text) callback(new Error('请输入正文内容'));
      else callback();
    },
    openCreate() {
      this.articleForm = { id: null, title: '', category: '', summary: '', content: '' };
      this.coverList = [];
      this.editorVisible = true;
      this.$nextTick(() => {
        if (this.$refs.articleFormRef) this.$refs.articleFormRef.clearValidate();
      });
    },
    async openEdit(row) {
      this.articleForm = { id: row.id, title: '', category: '', summary: '', content: '' };
      this.editorVisible = true;
      try {
        const detail = await articleDetail(row.id);
        this.articleForm = {
          id: row.id,
          title: detail.title || '',
          category: detail.category || '',
          summary: detail.summary || '',
          content: detail.content || ''
        };
        this.coverList = detail.cover ? [{ objectName: '', url: detail.cover }] : [];
        this.$nextTick(() => {
          if (this.$refs.articleFormRef) this.$refs.articleFormRef.clearValidate();
        });
      } catch (e) {
        this.editorVisible = false;
      }
    },
    onEditorCreated(editor) {
      this.editor = Object.seal(editor);
    },
    onDialogClosed() {
      if (this.$refs.articleFormRef) this.$refs.articleFormRef.clearValidate();
    },
    submitArticle(status) {
      this.$refs.articleFormRef.validate(async (valid) => {
        if (!valid) return;
        this.submitting = true;
        const payload = {
          title: this.articleForm.title,
          cover: this.coverList.length ? this.coverList[0].url : '',
          category: this.articleForm.category,
          summary: this.articleForm.summary,
          content: this.articleForm.content,
          status
        };
        try {
          if (this.articleForm.id) {
            await updateArticle(this.articleForm.id, payload);
            this.$message.success(status === 1 ? '文章已发布' : '草稿已保存');
          } else {
            await saveArticle(payload);
            this.$message.success(status === 1 ? '文章已发布' : '草稿已保存');
          }
          this.editorVisible = false;
          this.loadArticles();
        } catch (e) {
          /* 错误提示由 request 统一处理 */
        } finally {
          this.submitting = false;
        }
      });
    },
    changeStatus(row, status) {
      const tip = status === 1 ? `确认发布文章「${row.title}」吗？` : `确认下架文章「${row.title}」吗？下架后用户端不再展示。`;
      this.$confirm(tip, '提示', { type: status === 1 ? 'info' : 'warning' })
        .then(async () => {
          await changeArticleStatus(row.id, status);
          this.$message.success(status === 1 ? '文章已发布' : '文章已下架');
          this.loadArticles();
        })
        .catch(() => {});
    },
    toggleTop(row) {
      const next = row.isTop === 1 ? 0 : 1;
      topArticle(row.id, next)
        .then(() => {
          this.$message.success(next === 1 ? '文章已置顶' : '已取消置顶');
          this.loadArticles();
        })
        .catch(() => {});
    },
    removeArticle(row) {
      this.$confirm(`确认删除文章「${row.title}」吗？删除后不可恢复。`, '删除确认', { type: 'warning' })
        .then(async () => {
          await deleteArticle(row.id);
          this.$message.success('文章已删除');
          this.loadArticles();
        })
        .catch(() => {});
    },

    // ---------- 评论 ----------
    onTabClick(tab) {
      if (tab.name === 'comments' && !this.commentLoaded) {
        this.commentLoaded = true;
        this.loadComments();
      }
    },
    async loadComments() {
      this.commentLoading = true;
      this.commentError = false;
      try {
        const params = { pageNum: this.commentQuery.pageNum, pageSize: this.commentQuery.pageSize };
        if (this.commentQuery.status !== null && this.commentQuery.status !== '') params.status = this.commentQuery.status;
        if (this.commentQuery.targetType) params.targetType = this.commentQuery.targetType;
        const data = await adminComments(params);
        this.comments = data.list || [];
        this.commentTotal = data.total || 0;
      } catch (e) {
        this.commentError = true;
        this.comments = [];
        this.commentTotal = 0;
      } finally {
        this.commentLoading = false;
        this.commentFirstLoad = false;
      }
    },
    searchComments() {
      this.commentQuery.pageNum = 1;
      this.loadComments();
    },
    resetComments() {
      this.commentQuery = { pageNum: 1, pageSize: 10, status: null, targetType: '' };
      this.loadComments();
    },
    commentStatusKey(status) {
      return { 0: 'PENDING', 1: 'APPROVED', 2: 'OFFLINE' }[status] || 'DRAFT';
    },
    commentStatusText(status) {
      return { 0: '待审核', 1: '已通过', 2: '已屏蔽' }[status] || '待审核';
    },
    audit(row, pass) {
      const tip = pass
        ? `确认通过该评论吗？通过后用户端可见。`
        : `确认屏蔽该评论吗？屏蔽后用户端不再展示。`;
      this.$confirm(tip, '提示', { type: pass ? 'info' : 'warning' })
        .then(async () => {
          await auditComment(row.id, pass);
          this.$message.success(pass ? '评论已通过' : '评论已屏蔽');
          this.loadComments();
        })
        .catch(() => {});
    },
    removeComment(row) {
      this.$confirm('确认删除该评论吗？删除后不可恢复。', '删除确认', { type: 'warning' })
        .then(async () => {
          await deleteComment(row.id);
          this.$message.success('评论已删除');
          this.loadComments();
        })
        .catch(() => {});
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
}

.table-card {
  @include card;
  padding: $space-4;
}

.pagination {
  margin-top: $space-4;
  text-align: right;
}

.sep {
  margin: 0 4px;
  color: $text-disabled;
}

.top-chip {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 $space-2;
  border-radius: $radius-small;
  background: $color-warning-light;
  color: $color-warning-text;
  font-size: $font-w-caption;

  &--plain {
    background: transparent;
    color: $text-disabled;
  }
}

.comment-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.op-danger {
  color: $color-danger-text;
}

.cover-tip {
  margin-top: $space-2;
  font-size: $font-w-caption;
  color: $text-secondary;
}

.rich-editor {
  border: 1px solid $border-color;
  border-radius: $radius-base;
  overflow: hidden;
  z-index: 20;

  &__toolbar {
    border-bottom: 1px solid $border-color;
    background: $bg-card;
  }

  &__content {
    height: 320px;
    overflow-y: auto;
    background: $bg-card;
  }
}
</style>


