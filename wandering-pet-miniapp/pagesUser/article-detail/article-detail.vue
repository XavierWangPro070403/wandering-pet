<template>
  <view class="page">
    <view class="head card">
      <text class="head__title">{{ article.title }}</text>
      <view class="head__meta">
        <text class="head__author">{{ article.author }}</text>
        <text class="head__time num">{{ article.publishTime }}</text>
      </view>
    </view>

    <view class="cover-wrap card" v-if="article.cover" style="padding:0;">
      <image class="cover" :src="article.cover" mode="widthFix" />
    </view>

    <view class="content card">
      <rich-text :nodes="richContent"></rich-text>
    </view>

    <!-- 点赞 -->
    <view class="like-row">
      <view class="like" :class="{ on: liked }" @click="like">
        <text>♥</text>
        <text class="num">{{ article.likeCount || 0 }}</text>
      </view>
    </view>

    <!-- 评论 -->
    <view class="comments card">
      <text class="card-title">评论（{{ comments.length }}）</text>
      <view class="comment" v-for="c in comments" :key="c.id">
        <image class="comment__avatar" :src="c.userAvatar || '/static/avatar-default.png'" mode="aspectFill" />
        <view class="comment__body">
          <text class="comment__name">{{ c.userNickname }}</text>
          <text class="comment__content">{{ c.content }}</text>
          <text class="comment__time num">{{ c.createTime }}</text>
        </view>
      </view>
      <view v-if="!comments.length" class="empty">
        <text class="empty__title">还没有评论</text>
        <text class="empty__desc">说说你的看法（评论通过审核后展示）</text>
      </view>
    </view>

    <!-- 发表评论 -->
    <view class="comment-bar" v-if="isLoggedIn">
      <input class="comment-bar__input" v-model="content" placeholder="写下你的评论" maxlength="500" />
      <button class="comment-bar__send" :disabled="!content.trim()" @click="send">发送</button>
    </view>
    <view class="comment-bar" v-else>
      <button class="btn-primary" style="flex:1;height:72rpx;line-height:72rpx;" @click="goLogin">登录后参与评论</button>
    </view>
  </view>
</template>

<script>
import { articleDetail, likeArticle, createComment } from '../../common/api.js';

export default {
  data() {
    return {
      article: {},
      comments: [],
      liked: false,
      content: ''
    };
  },
  computed: {
    id() {
      return this.$root.$mp.query.id;
    },
    isLoggedIn() {
      return this.$store.getters.isLoggedIn;
    },
    /**
     * 富文本内容图自适应：小程序端 rich-text 无法用外部样式约束内部节点，
     * 给没有内联样式的 img 统一注入 max-width，避免编辑器插入的大图横向溢出
     */
    richContent() {
      const html = this.article.content || '';
      return html.replace(/<img\b([^>]*)>/gi, (match, attrs) =>
        /style\s*=/i.test(attrs) ? match : '<img style="max-width:100%;height:auto;"' + attrs + '>');
    }
  },
  onLoad() {
    this.load();
  },
  methods: {
    async load() {
      try {
        const data = await articleDetail(this.id);
        this.article = data || {};
        this.comments = (data && data.comments) || [];
        this.liked = !!(data && data.liked);
      } catch (e) {
        uni.navigateBack();
      }
    },
    async like() {
      if (!this.isLoggedIn) {
        uni.navigateTo({ url: '/pagesUser/login/login' });
        return;
      }
      try {
        await likeArticle(this.id);
        if (!this.liked) {
          this.article.likeCount = (this.article.likeCount || 0) + 1;
          this.liked = true;
        }
      } catch (e) { /* */ }
    },
    async send() {
      const text = this.content.trim();
      if (!text) return;
      try {
        await createComment({ targetType: 'ARTICLE', targetId: this.id, content: text });
        this.content = '';
        uni.showToast({ title: '评论已提交，审核通过后展示', icon: 'none' });
      } catch (e) { /* */ }
    },
    goLogin() {
      uni.navigateTo({ url: '/pagesUser/login/login' });
    }
  }
};
</script>

<style scoped>
/* H5 端富文本内容图兜底（小程序端由 richContent 注入内联样式约束） */
/* #ifdef H5 */
.content ::v-deep img {
  max-width: 100%;
  height: auto;
}
/* #endif */

.head__title {
  display: block;
  font-size: 38rpx;
  font-weight: 600;
  color: #1F2733;
  line-height: 56rpx;
}

.head__meta {
  display: flex;
  gap: 24rpx;
  margin-top: 16rpx;
}

.head__author {
  font-size: 24rpx;
  color: #667080;
}

.head__time {
  font-size: 24rpx;
  color: #A9B0BC;
}

.cover {
  width: 100%;
  display: block;
  border-radius: 24rpx;
}

.content {
  font-size: 30rpx;
  line-height: 52rpx;
  color: #4E5969;
}

.like-row {
  display: flex;
  justify-content: center;
  padding: 8rpx 0 24rpx;
}

.like {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 40rpx;
  border-radius: 44rpx;
  background: #FFFFFF;
  border: 2rpx solid #E5E6EB;
  color: #667080;
  font-size: 30rpx;
}

.like.on {
  color: #B3122B;
  border-color: #F5455C;
  background: #FFECEF;
}

.comment {
  display: flex;
  padding: 24rpx 0;
  border-bottom: 2rpx solid #F7F8FA;
}

.comment:last-child {
  border-bottom: none;
}

.comment__avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background: #F2F3F5;
}

.comment__body {
  flex: 1;
  margin-left: 16rpx;
}

.comment__name {
  font-size: 24rpx;
  color: #667080;
}

.comment__content {
  display: block;
  margin-top: 6rpx;
  font-size: 28rpx;
  color: #1F2733;
  line-height: 42rpx;
}

.comment__time {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: #A9B0BC;
}

.comment-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  gap: 20rpx;
  align-items: center;
  padding: 16rpx 32rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
  border-top: 2rpx solid #E5E6EB;
}

.comment-bar__input {
  flex: 1;
  height: 72rpx;
  background: #F7F8FA;
  border-radius: 36rpx;
  padding: 0 28rpx;
  font-size: 28rpx;
}

.comment-bar__send {
  width: 140rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 36rpx;
  background: #FF7A45;
  color: #1F2733;
  font-size: 28rpx;
  padding: 0;
  margin: 0;
  border: none;
}

.comment-bar__send::after {
  border: none;
}

.comment-bar__send[disabled] {
  background: #F2F3F5;
  color: #A9B0BC;
}
</style>
