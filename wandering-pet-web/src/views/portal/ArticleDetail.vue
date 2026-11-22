<template>
  <div class="article-detail">
    <div class="wrap">
      <div class="detail-col">
        <header class="detail-head">
          <span class="detail-head__category">{{ categoryText(article.category) }}</span>
          <h1 class="detail-head__title">{{ article.title }}</h1>
          <p class="detail-head__meta num">
            {{ article.author || '编辑部' }} · 发布于 {{ article.publishTime || '—' }}
            · {{ article.viewCount }} 次阅读
          </p>
        </header>

        <div v-if="article.cover" class="detail-cover">
          <img :src="article.cover" :alt="`文章封面：${article.title}`" />
        </div>

        <div class="detail-body" v-html="article.content"></div>

        <!-- 点赞 -->
        <div class="like-bar">
          <button class="like-bar__btn" :class="{ liked: article.liked }" @click="toggleLike">
            <i :class="article.liked ? 'el-icon-star-on' : 'el-icon-star-off'" aria-hidden="true"></i>
            {{ article.liked ? '已点赞' : '点赞' }}
            <span class="num">{{ article.likeCount }}</span>
          </button>
        </div>

        <!-- 评论区 -->
        <section class="comments" aria-labelledby="sec-comments">
          <h2 id="sec-comments">评论 <span class="num">（{{ comments.length }}）</span></h2>

          <div class="comments__form">
            <el-input
              v-model="commentDraft"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="分享你的饲养经验或疑问" />
            <div class="comments__form-foot">
              <p class="comments__form-tip">评论通过审核后展示</p>
              <el-button type="primary" size="small" :loading="commentSubmitting" @click="submitComment">发表评论</el-button>
            </div>
          </div>

          <ul v-if="comments.length" class="comments__list">
            <li v-for="c in comments" :key="c.id" class="comment">
              <img class="comment__avatar" :src="c.userAvatar || defaultAvatar" :alt="`${c.userNickname}的头像`" />
              <div class="comment__main">
                <p class="comment__head">
                  <span class="comment__nick">{{ c.userNickname }}</span>
                  <time class="comment__time num">{{ c.createTime }}</time>
                </p>
                <p class="comment__content">{{ c.content }}</p>
              </div>
            </li>
          </ul>
          <EmptyState v-else title="还没有评论" desc="第一条评论往往能帮到后面的领养人。" />
        </section>
      </div>
    </div>
  </div>
</template>

<script>
import EmptyState from '@/components/EmptyState.vue';
import { articleDetail, likeArticle, createComment } from '@/api/article';

export default {
  name: 'ArticleDetailPage',
  components: { EmptyState },
  data() {
    return {
      loading: true,
      article: {},
      commentDraft: '',
      commentSubmitting: false,
      defaultAvatar: '/imgs/avatar-default.svg'
    };
  },
  computed: {
    comments() {
      return this.article.comments || [];
    },
    isLoggedIn() {
      return !!this.$store.state.user.token;
    }
  },
  watch: {
    '$route.params.id'() {
      this.load();
    }
  },
  async mounted() {
    await this.load();
  },
  methods: {
    categoryText: (c) => ({
      RAISE: '饲养指导', KNOWLEDGE: '科普百科', NEWS: '平台动态', STORY: '救助故事'
    }[c] || c),
    async load() {
      this.loading = true;
      try {
        this.article = await articleDetail(this.$route.params.id);
      } catch (e) {
        this.$message.error('文章不存在或已下架');
        this.$router.replace('/articles');
      } finally {
        this.loading = false;
      }
    },
    async toggleLike() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录后再点赞');
        return;
      }
      if (this.article.liked) {
        this.$message.info('已点赞过这篇文章');
        return;
      }
      try {
        await likeArticle(this.article.id);
        this.article.liked = true;
        this.article.likeCount = (this.article.likeCount || 0) + 1;
        this.$message.success('已点赞');
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async submitComment() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录后再发表评论');
        return;
      }
      const content = this.commentDraft.trim();
      if (!content) {
        this.$message.warning('请输入评论内容');
        return;
      }
      this.commentSubmitting = true;
      try {
        await createComment({ targetType: 'ARTICLE', targetId: this.article.id, content });
        this.commentDraft = '';
        this.$message.success('评论已提交，通过审核后展示');
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.commentSubmitting = false;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.wrap {
  @include wrap;
}

.article-detail {
  background: $bg-card;
  padding: 40px 0 80px;
}

.detail-col {
  max-width: 720px;
  margin: 0 auto;
}

.detail-head {
  &__category {
    display: inline-block;
    padding: 2px 8px;
    border-radius: $radius-small;
    background: $color-primary-light;
    color: $color-primary-text;
    font-size: 12px;
    line-height: 20px;
  }

  &__title {
    margin-top: 12px;
    font-size: $font-h3;
    line-height: $lh-h3;
  }

  &__meta {
    margin-top: 12px;
    font-size: 13px;
    color: $text-secondary;
    @include tabular-nums;
  }
}

.detail-cover {
  margin-top: 24px;
  aspect-ratio: 4 / 3;
  // 防止 height:100% 子图按自然比例撑破比例框
  min-height: 0;
  border-radius: $radius-card;
  overflow: hidden;
  background: $bg-page;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}

// 正文（后端已白名单过滤的富文本）
.detail-body {
  margin-top: 24px;
  font-size: 16px;
  line-height: 28px;
  color: $text-regular;
  word-break: break-word;

  ::v-deep p {
    margin: 0 0 16px;
  }

  ::v-deep h2,
  ::v-deep h3 {
    font-size: 20px;
    line-height: 30px;
    margin: 24px 0 12px;
    color: $text-primary;
  }

  ::v-deep img {
    max-width: 100%;
    height: auto;
    border-radius: $radius-base;
    display: block;
    margin: 16px auto;
  }

  ::v-deep a {
    color: $color-primary-text;
    text-decoration: underline;
  }

  ::v-deep ul,
  ::v-deep ol {
    padding-left: 24px;
    margin: 0 0 16px;
  }

  ::v-deep li {
    margin-bottom: 6px;
  }

  ::v-deep blockquote {
    margin: 16px 0;
    padding: 12px 16px;
    border-left: 3px solid $color-primary;
    background: $bg-page;
    border-radius: 0 $radius-base $radius-base 0;
    color: $text-regular;
  }
}

// 点赞
.like-bar {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid $border-color;
  text-align: center;

  &__btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    height: 44px;
    padding: 0 24px;
    border-radius: $radius-base;
    border: 1px solid $border-color;
    background: $bg-card;
    color: $text-regular;
    font-size: 14px;
    cursor: pointer;
    transition: all $duration-hover-in $ease-out-quart;

    i {
      font-size: 18px;
    }

    &:hover {
      border-color: $color-primary;
      color: $color-primary-text;
      background: $color-primary-light;
    }

    &.liked {
      border-color: $color-primary;
      color: $color-primary-text;
      background: $color-primary-light;
    }
  }
}

// 评论区
.comments {
  margin-top: 40px;

  h2 {
    font-size: 20px;
    line-height: 30px;
    margin-bottom: 20px;
  }

  &__form {
    @include card;
    padding: 16px;
  }

  &__form-foot {
    margin-top: 12px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__form-tip {
    font-size: 12px;
    color: $text-secondary;
  }

  &__list {
    margin: 16px 0 0;
    padding: 0;
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
}

.comment {
  @include card;

  display: flex;
  gap: 12px;
  padding: 16px;

  &__avatar {
    width: 40px;
    height: 40px;
    border-radius: $radius-circle;
    object-fit: cover;
    flex-shrink: 0;
    background: $bg-page;
  }

  &__main {
    min-width: 0;
    flex: 1;
  }

  &__head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 12px;
  }

  &__nick {
    font-size: 14px;
    font-weight: 500;
    color: $text-primary;
  }

  &__time {
    font-size: 12px;
    color: $text-secondary;
  }

  &__content {
    margin-top: 4px;
    font-size: 14px;
    line-height: 22px;
    color: $text-regular;
    white-space: pre-line;
    word-break: break-word;
  }
}
</style>
