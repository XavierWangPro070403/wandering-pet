<template>
  <view class="page chat-page">
    <!-- AI 顾问入口横幅 -->
    <view class="ai-entry" @click="goAi">
      <text class="ai-entry__icon">🤖</text>
      <view class="ai-entry__body">
        <text class="ai-entry__title">AI 宠物顾问 · 小爪</text>
        <text class="ai-entry__sub">养宠、领养问题，秒问秒答</text>
      </view>
      <text class="ai-entry__arrow">›</text>
    </view>
    <!-- 消息区 -->
    <scroll-view
      class="chat__scroll"
      scroll-y
      :scroll-top="scrollTop"
      :scroll-with-animation="true">
      <view class="chat__inner">
        <view
          v-for="m in messages"
          :key="m.id"
          class="msg"
          :class="{ mine: isMine(m), recalled: m.recalled }"
          @longpress="onLongPress(m)">
          <!-- 已撤回：居中一条灰字提示，和微信一致 -->
          <view v-if="m.recalled" class="msg__recalled">
            <text>{{ isMine(m) ? '你撤回了一条消息' : '对方撤回了一条消息' }}</text>
          </view>
          <template v-else>
            <image class="msg__avatar" :src="isMine(m) ? myAvatar : shelterLogo" mode="aspectFill" />
            <view class="msg__body">
              <!-- 领养申请卡片（提交申请后自动发送，Boss直聘式建联） -->
              <view v-if="m.contentType === 'APPLICATION'" class="msg__applycard" @click.stop="goApply(cardOf(m))">
                <image class="msg__applycard__cover" :src="cardOf(m).petCover" mode="aspectFill" />
                <view class="msg__applycard__info">
                  <view class="msg__applycard__badge"><text>📋 领养申请</text></view>
                  <text class="msg__applycard__pet">{{ cardOf(m).petName }}</text>
                  <text class="msg__applycard__line">申请人：{{ cardOf(m).applicantName }} {{ cardOf(m).applicantPhone }}</text>
                  <text class="msg__applycard__more">查看申请进度 ›</text>
                </view>
              </view>
              <image v-else-if="m.contentType === 'IMAGE'" class="msg__img" :src="m.objectUrl" mode="aspectFill" @click="preview(m.objectUrl)" />
              <view v-else class="msg__bubble"><text>{{ m.content }}</text></view>
              <text class="msg__time num">{{ m.createTime }}</text>
            </view>
          </template>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区 -->
    <view class="chat__bar">
      <view class="chat__img-btn" @click="chooseImage"><text>＋</text></view>
      <input class="chat__input" v-model="content" confirm-type="send" maxlength="1000" placeholder="输入消息" @confirm="send" />
      <button class="chat__send" :disabled="!content.trim() || sending" @click="send">发送</button>
    </view>
  </view>
</template>

<script>
import { sessionMessages, sendMessage, readSession, recallMessage, deleteMessage } from '../../common/api.js';
import http from '../../common/request.js';
import { subscribeChatPush } from '../../common/ws.js';

export default {
  data() {
    return {
      sessionId: null,
      messages: [],
      content: '',
      sending: false,
      scrollTop: 0,
      unsubscribe: null,
      lastSig: null
    };
  },
  computed: {
    isMine() {
      const myType = this.$store.state.userInfo && this.$store.state.userInfo.userType || 'USER';
      return (m) => (m.senderType === myType) || (this.$store.getters.isShelter && m.senderType === 'SHELTER');
    },
    myAvatar() {
      return (this.$store.state.userInfo && this.$store.state.userInfo.avatar) || '/static/avatar-default.png';
    },
    shelterLogo() {
      return '/static/avatar-shelter.png';
    }
  },
  onLoad(query) {
    this.sessionId = query.sessionId;
  },
  onReady() {
    this.load(true);
    readSession(this.sessionId).catch(() => {});
    // 实时接收：WebSocket 推送替代 3 秒轮询
    this.unsubscribe = subscribeChatPush((data) => this.onRealtime(data));
  },
  onUnload() {
    if (this.unsubscribe) this.unsubscribe();
  },
  methods: {
    goAi() {
      uni.navigateTo({ url: '/pagesUser/ai-chat/ai-chat' });
    },
    /** 申请卡片消息解析（content 为 JSON 文本） */
    cardOf(m) {
      try {
        return JSON.parse(m.content);
      } catch (e) {
        return {};
      }
    },
    /** 申请卡片 → 查看申请进度 */
    goApply(card) {
      if (card.applicationId) {
        uni.navigateTo({ url: '/pagesUser/application-progress/application-progress?id=' + card.applicationId });
      }
    },
    /** WS 实时推送处理：新消息 / 撤回事件 */
    onRealtime(data) {
      if (data.type === 'CHAT_MESSAGE') {
        const msg = data.message;
        if (String(msg.sessionId) !== String(this.sessionId)) return;
        // 发送方自己的消息由 HTTP 响应带回，这里只处理对方发来的
        if (this.messages.some((m) => m.id === msg.id)) return;
        this.messages.push(msg);
        this.$nextTick(() => {
          this.scrollTop = this.messages.length * 600 + 9999;
        });
        // 正在会话中，立刻标记已读
        readSession(this.sessionId).catch(() => {});
      } else if (data.type === 'CHAT_RECALL') {
        if (String(data.sessionId) !== String(this.sessionId)) return;
        const target = this.messages.find((m) => m.id === data.messageId);
        if (target) {
          target.recalled = 1;
          target.content = '';
          target.objectUrl = null;
        }
      }
    },
    async load(first) {
      try {
        const page = await sessionMessages(this.sessionId, { pageNum: 1, pageSize: 50 });
        // 后端按时间倒序返回，翻转为正序
        const records = (page && (page.records || page.list)) || [];
        const list = records.slice().reverse();
        // 撤回会改变已有消息，签名里带上撤回标记才能刷新
        const signature = list.map((m) => m.id + (m.recalled ? 'R' : '')).join(',');
        if (first || signature !== this.lastSig) {
          this.lastSig = signature;
          this.messages = list;
          if (first || this.nearBottom()) {
            this.$nextTick(() => {
              this.scrollTop = this.messages.length * 600 + 9999;
            });
          }
        }
      } catch (e) { /* */ }
    },
    nearBottom() {
      // 简化处理：默认自动滚到底
      return true;
    },
    async send() {
      const text = this.content.trim();
      if (!text || this.sending) return;
      this.sending = true;
      try {
        await sendMessage(this.sessionId, { contentType: 'TEXT', content: text });
        this.content = '';
        await this.load(true);
      } catch (e) { /* */ } finally {
        this.sending = false;
      }
    },
    async chooseImage() {
      const res = await new Promise((resolve) => {
        uni.chooseImage({ count: 1, sizeType: ['compressed'], success: resolve, fail: () => resolve(null) });
      });
      if (!res || !res.tempFilePaths || !res.tempFilePaths.length) return;
      try {
        const data = await http.upload(res.tempFilePaths[0], 'dynamic');
        await sendMessage(this.sessionId, { contentType: 'IMAGE', content: '[图片]', objectUrl: data.url });
        await this.load(true);
      } catch (e) { /* */ }
    },
    preview(url) {
      uni.previewImage({ urls: [url] });
    },
    /** 只有自己发的、2 分钟内的消息可以撤回（后端同样校验） */
    canRecall(m) {
      if (!this.isMine(m) || !m.createTime) return false;
      const sent = new Date(String(m.createTime).replace(/-/g, '/')).getTime();
      return Date.now() - sent <= 2 * 60 * 1000;
    },
    /** 长按消息：弹出「撤回 / 删除」（和微信一致） */
    onLongPress(m) {
      if (m.recalled) {
        // 已撤回的消息只允许删除自己这一侧的记录
        return this.doDelete(m);
      }
      const actions = [];
      if (this.canRecall(m)) actions.push({ label: '撤回', key: 'recall' });
      actions.push({ label: '删除', key: 'delete' });
      uni.showActionSheet({
        itemList: actions.map((a) => a.label),
        success: (res) => {
          const action = actions[res.tapIndex];
          if (!action) return;
          if (action.key === 'recall') this.doRecall(m);
          else this.doDelete(m);
        }
      });
    },
    doRecall(m) {
      uni.showModal({
        title: '撤回消息',
        content: '撤回后双方都看不到这条消息',
        confirmText: '撤回',
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await recallMessage(m.id);
            uni.showToast({ title: '已撤回', icon: 'none' });
            await this.load(true);
          } catch (e) { /* 拦截器已提示 */ }
        }
      });
    },
    async doDelete(m) {
      const confirmed = await new Promise((resolve) => {
        uni.showModal({
          title: '删除消息',
          content: '仅你的聊天记录中不再显示，对方不受影响',
          confirmText: '删除',
          success: (res) => resolve(res.confirm)
        });
      });
      if (!confirmed) return;
      try {
        await deleteMessage(m.id);
        this.messages = this.messages.filter((item) => item.id !== m.id);
        this.lastSig = null;
        uni.showToast({ title: '已删除', icon: 'none' });
      } catch (e) { /* 拦截器已提示 */ }
    }
  }
};
</script>

<style scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F7F8FA;
}

.chat__scroll {
  flex: 1;
  height: 0;
}

/* 领养申请卡片 */
.msg__applycard {
  width: 460rpx;
  display: flex;
  gap: 16rpx;
  padding: 18rpx;
  border-radius: 14rpx;
  background: #FFFFFF;
  border-left: 6rpx solid #FF7A45;
  box-shadow: 0 4rpx 14rpx rgba(31, 39, 51, 0.08);
}

.msg__applycard__cover {
  width: 110rpx;
  height: 110rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
  background: #F2F3F5;
}

.msg__applycard__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  min-width: 0;
}

.msg__applycard__badge {
  align-self: flex-start;
  padding: 2rpx 12rpx;
  border-radius: 999rpx;
  background: #FFF1EB;
}

.msg__applycard__badge text {
  font-size: 20rpx;
  color: #B23A0E;
}

.msg__applycard__pet {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2733;
}

.msg__applycard__line {
  font-size: 22rpx;
  color: #667080;
}

.msg__applycard__more {
  font-size: 22rpx;
  color: #FF7A45;
}

/* AI 顾问入口横幅 */
.ai-entry {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin: 16rpx 24rpx 0;
  padding: 18rpx 24rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #FFF6EF, #FFFFFF);
  border: 2rpx solid #FFE0CE;
}

.ai-entry__icon {
  font-size: 40rpx;
  line-height: 1;
}

.ai-entry__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.ai-entry__title {
  font-size: 26rpx;
  font-weight: 600;
  color: #1F2733;
}

.ai-entry__sub {
  font-size: 22rpx;
  color: #A9B0BC;
}

.ai-entry__arrow {
  color: #C0C4CC;
  font-size: 32rpx;
}

.chat__inner {
  padding: 24rpx 32rpx;
}

.msg {
  display: flex;
  margin-bottom: 28rpx;
}

.msg__avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background: #F2F3F5;
}

.msg__body {
  margin-left: 16rpx;
  max-width: 70%;
}

.mine {
  flex-direction: row-reverse;
}

/* 撤回提示：整行居中显示 */
.msg.recalled {
  justify-content: center;
}

.msg__recalled {
  padding: 8rpx 0;
  text-align: center;
}

.msg__recalled text {
  font-size: 22rpx;
  color: #A9B0BC;
}

.mine .msg__body {
  margin-left: 0;
  margin-right: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.msg__bubble {
  background: #FFFFFF;
  border-radius: 4rpx 24rpx 24rpx 24rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  color: #1F2733;
  line-height: 42rpx;
  word-break: break-all;
}

.mine .msg__bubble {
  background: #FFF1EB;
  border-radius: 24rpx 4rpx 24rpx 24rpx;
}

.msg__img {
  width: 320rpx;
  height: 240rpx;
  border-radius: 16rpx;
}

.msg__time {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  color: #A9B0BC;
}

.chat__bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 32rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
  border-top: 2rpx solid #E5E6EB;
}

.chat__img-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #F2F3F5;
  color: #667080;
  font-size: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.chat__input {
  flex: 1;
  height: 72rpx;
  background: #F7F8FA;
  border-radius: 36rpx;
  padding: 0 28rpx;
  font-size: 28rpx;
}

.chat__send {
  width: 132rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 36rpx;
  background: #FF7A45;
  color: #1F2733;
  font-size: 28rpx;
  padding: 0;
  margin: 0;
  border: none;
  flex-shrink: 0;
}

.chat__send::after {
  border: none;
}

.chat__send[disabled] {
  background: #F2F3F5;
  color: #A9B0BC;
}
</style>
