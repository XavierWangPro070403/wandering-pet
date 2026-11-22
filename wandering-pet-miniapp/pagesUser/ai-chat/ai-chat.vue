<template>
  <view class="page">
    <!-- 顶部工具条：新对话 / 历史会话 -->
    <view class="toolbar">
      <view class="toolbar__btn" :class="{ active: !currentId }" @click="newSession">
        <text class="toolbar__icon">＋</text><text>新对话</text>
      </view>
      <view class="toolbar__spacer"></view>
      <view class="toolbar__btn" @click="openHistory">
        <text class="toolbar__icon">🕘</text><text>历史会话</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view scroll-y class="chat" :scroll-top="scrollTop" scroll-with-animation>
      <view class="chat__inner">
        <view class="time">AI 宠物顾问 · 小爪</view>

        <!-- 空状态：欢迎语 + 快捷问题 -->
        <block v-if="!messages.length">
          <view class="msg msg--ai">
            <view class="msg__avatar msg__avatar--ai">🤖</view>
            <view class="msg__bubble msg__bubble--ai">
              <text>你好，我是 AI 宠物顾问小爪 🐾
流浪动物救助、领养流程、猫狗养护、健康问题都可以问我～</text>
            </view>
          </view>
          <view class="chips">
            <view class="chip" v-for="q in quickQuestions" :key="q" @click="send(q)">
              <text>{{ q }}</text>
            </view>
          </view>
        </block>

        <block v-for="(m, i) in messages" :key="i">
          <view class="time" v-if="showTime(i)">{{ m.time }}</view>
          <!-- 用户消息：气泡在左、头像在最右（微信样式） -->
          <view class="msg" :class="m.role === 'user' ? 'msg--user' : 'msg--ai'">
            <view class="msg__avatar msg__avatar--ai" v-if="m.role === 'assistant'">🤖</view>
            <view class="msg__bubble" :class="m.role === 'user' ? 'msg__bubble--user' : 'msg__bubble--ai'" @longpress="copyText(m.content)">
              <text class="msg__text">{{ m.content }}</text>
            </view>
            <view class="msg__avatar msg__avatar--me" v-if="m.role === 'user'">
              <image class="msg__avatar-img" :src="myAvatar" mode="aspectFill" />
            </view>
          </view>
        </block>

        <!-- 思考中 -->
        <view class="msg msg--ai" v-if="thinking">
          <view class="msg__avatar msg__avatar--ai">🤖</view>
          <view class="msg__bubble msg__bubble--ai msg__bubble--thinking">
            <view class="dots"><view class="dot d1"></view><view class="dot d2"></view><view class="dot d3"></view></view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部输入栏（微信风格） -->
    <view class="input-bar">
      <input
        class="input-bar__field"
        v-model="draft"
        placeholder="问问养宠、领养的问题…"
        confirm-type="send"
        :adjust-position="true"
        :cursor-spacing="16"
        maxlength="1000"
        @confirm="send()"
      />
      <button class="input-bar__send" :class="{ on: draft.trim() }" :disabled="thinking" @click="send()">发送</button>
    </view>

    <!-- 历史会话弹层：支持关键词查找 -->
    <view class="hist-mask" v-if="showHistory" @click="showHistory = false">
      <view class="hist-panel" @click.stop>
        <view class="hist-panel__head">
          <text class="hist-panel__title">历史会话</text>
          <text class="hist-panel__close" @click="showHistory = false">✕</text>
        </view>
        <input class="hist-panel__search" v-model="searchKey" placeholder="搜索对话内容…" confirm-type="search" />
        <scroll-view scroll-y class="hist-list">
          <block v-if="filteredHistory.length">
            <view class="hist-item" v-for="s in filteredHistory" :key="s.id" @click="openHistorySession(s)">
              <text class="hist-item__title">{{ s.title }}</text>
              <text class="hist-item__meta">{{ s.updated }} · {{ s.messages.length }} 条对话</text>
            </view>
          </block>
          <view v-else class="hist-empty"><text>没有找到匹配的对话</text></view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script>
import { askAi } from '../../common/api.js';

/** 本地会话存储 key（按用户隔离） */
const storeKey = (uid) => 'ai_chat_sessions_' + (uid || 'guest');
const MIN_KEEP = 50; // 本地最多保留 50 个会话

export default {
  data() {
    return {
      messages: [],
      draft: '',
      thinking: false,
      scrollTop: 0,
      quickQuestions: ['领养一只猫需要什么条件？', '幼猫疫苗怎么打？', '遇到流浪猫怎么救助？', '狗狗驱虫多久一次？'],
      // 会话管理
      sessions: [],
      currentId: null,
      showHistory: false,
      searchKey: ''
    };
  },
  computed: {
    uid() {
      const u = this.$store.state.userInfo;
      return (u && u.id) || 'guest';
    },
    myAvatar() {
      return (this.$store.state.userInfo && this.$store.state.userInfo.avatar) || '/static/avatar-default.png';
    },
    /** 历史会话按关键词过滤（标题 + 消息内容） */
    filteredHistory() {
      const kw = this.searchKey.trim().toLowerCase();
      const list = this.sessions.slice();
      if (!kw) return list;
      return list.filter((s) => {
        if ((s.title || '').toLowerCase().includes(kw)) return true;
        return (s.messages || []).some((m) => (m.content || '').toLowerCase().includes(kw));
      });
    }
  },
  onLoad() {
    this.loadSessions();
  },
  methods: {
    // ---------- 会话存储 ----------
    storageKey() {
      return storeKey(this.uid);
    },
    loadSessions() {
      const raw = uni.getStorageSync(this.storageKey());
      this.sessions = raw || [];
      // 默认载入最近的一个会话；没有则保持空（欢迎语状态）
      if (this.sessions.length) {
        const latest = this.sessions[0];
        this.currentId = latest.id;
        this.messages = (latest.messages || []).slice();
        this.scrollBottom();
      }
    },
    persist() {
      // 超量裁剪：只保留最近 MIN_KEEP 个会话
      if (this.sessions.length > MIN_KEEP) {
        this.sessions = this.sessions.slice(0, MIN_KEEP);
      }
      uni.setStorageSync(this.storageKey(), this.sessions);
    },
    /** 把当前对话内容同步进会话列表（无会话则新建）并持久化 */
    syncCurrent() {
      if (!this.messages.length) return;
      let s = this.sessions.find((x) => x.id === this.currentId);
      if (!s) {
        this.currentId = 'c' + Date.now();
        const firstUser = this.messages.find((m) => m.role === 'user');
        s = {
          id: this.currentId,
          title: firstUser ? firstUser.content.slice(0, 20) : '新对话',
          updated: '',
          messages: []
        };
        this.sessions.unshift(s);
      }
      s.messages = this.messages.slice();
      s.updated = this.now();
      const firstUser = this.messages.find((m) => m.role === 'user');
      if (firstUser) s.title = firstUser.content.slice(0, 20);
      this.persist();
    },
    /** 开启新对话：保存当前 → 清空显示 */
    newSession() {
      this.syncCurrent();
      this.currentId = null;
      this.messages = [];
      this.scrollBottom();
    },
    /** 历史弹层打开（打开时同步一次当前内容） */
    openHistory() {
      this.syncCurrent();
      this.showHistory = true;
    },
    /** 载入某个历史会话查看 */
    openHistorySession(s) {
      this.syncCurrent();
      this.currentId = s.id;
      this.messages = (s.messages || []).slice();
      this.showHistory = false;
      this.scrollBottom();
    },
    // ---------- 消息 ----------
    showTime(i) {
      if (i === 0) return true;
      const prev = this.messages[i - 1];
      const cur = this.messages[i];
      if (!prev.time || !cur.time) return false;
      return new Date(cur.time.replace(/-/g, '/')) - new Date(prev.time.replace(/-/g, '/')) > 5 * 60 * 1000;
    },
    now() {
      const d = new Date();
      const pad = (n) => String(n).padStart(2, '0');
      return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
    },
    scrollBottom() {
      this.$nextTick(() => {
        this.scrollTop = this.messages.length * 800 + 99999;
      });
    },
    copyText(text) {
      if (!text) return;
      uni.setClipboardData({
        data: text,
        success: () => uni.showToast({ title: '已复制', icon: 'none' })
      });
    },
    /** 发送：用户气泡 → 思考中 → AI 回复 → 自动保存当前会话 */
    async send(text) {
      const content = (text || this.draft).trim();
      if (!content || this.thinking) return;
      this.draft = '';
      this.messages.push({ role: 'user', content, time: this.now() });
      this.thinking = true;
      this.scrollBottom();
      try {
        const history = this.messages
          .filter((m) => m.role === 'user' || m.role === 'assistant')
          .slice(-7, -1)
          .map((m) => ({ role: m.role, content: m.content }));
        const vo = await askAi({ message: content, history });
        this.messages.push({
          role: 'assistant',
          content: vo.reply,
          demo: vo.demo,
          time: this.now()
        });
      } catch (e) {
        this.messages.push({
          role: 'assistant',
          content: '哎呀，我这边网络开小差了，请再试一次 🐾',
          time: this.now()
        });
      } finally {
        this.thinking = false;
        this.syncCurrent();
        this.scrollBottom();
      }
    }
  }
};
</script>

<style scoped>
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #EDEDED;
}

/* ---------- 顶部工具条 ---------- */
.toolbar {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 14rpx 24rpx;
  background: #F7F7F7;
  border-bottom: 2rpx solid #E5E5E5;
}

.toolbar__btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  background: #FFFFFF;
  border: 2rpx solid #E5E6EB;
  font-size: 24rpx;
  color: #4E5969;
}

.toolbar__btn.active {
  background: #FFF1EB;
  border-color: #FF7A45;
  color: #B23A0E;
}

.toolbar__icon {
  font-size: 24rpx;
}

.toolbar__spacer {
  flex: 1;
}

/* ---------- 消息列表 ---------- */
.chat {
  flex: 1;
  min-height: 0;
}

.chat__inner {
  padding: 20rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
}

.time {
  text-align: center;
  font-size: 22rpx;
  color: #B2B2B2;
  padding: 16rpx 0;
}

/* ---------- 消息气泡 ---------- */
.msg {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin: 20rpx 0;
}

/* 用户消息：整体靠右（DOM 顺序 = 气泡在前、头像在后） */
.msg--user {
  justify-content: flex-end;
}

.msg__avatar {
  flex-shrink: 0;
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  overflow: hidden;
}

.msg__avatar--ai {
  background: linear-gradient(145deg, #FF9A62, #FF7A45);
}

.msg__avatar-img {
  width: 100%;
  height: 100%;
}

.msg__bubble {
  max-width: 480rpx;
  padding: 18rpx 24rpx;
  border-radius: 12rpx;
  position: relative;
  word-break: break-all;
}

/* 用户气泡：微信绿，小三角在右 */
.msg__bubble--user {
  background: #95EC69;
  color: #1F2733;
}

.msg--user .msg__bubble--user::before {
  content: '';
  position: absolute;
  right: -10rpx;
  top: 26rpx;
  border: 10rpx solid transparent;
  border-left-color: #95EC69;
  border-right: none;
}

/* AI 气泡：白底，小三角在左 */
.msg__bubble--ai {
  background: #FFFFFF;
  color: #1F2733;
}

.msg--ai .msg__bubble--ai::before {
  content: '';
  position: absolute;
  left: -10rpx;
  top: 26rpx;
  border: 10rpx solid transparent;
  border-right-color: #FFFFFF;
  border-left: none;
}

.msg__text {
  font-size: 30rpx;
  line-height: 44rpx;
}

/* ---------- 思考中三点动画 ---------- */
.msg__bubble--thinking {
  padding: 24rpx 30rpx;
}

.dots {
  display: flex;
  gap: 12rpx;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #C4C4C4;
  animation: ai-dot 1.2s ease-in-out infinite;
}

.dot.d2 { animation-delay: 0.2s; }
.dot.d3 { animation-delay: 0.4s; }

@keyframes ai-dot {
  0%, 100% { opacity: 0.3; transform: translateY(0); }
  50% { opacity: 1; transform: translateY(-6rpx); }
}

/* ---------- 快捷问题 ---------- */
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  padding: 8rpx 0 8rpx 96rpx;
}

.chip {
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  background: #FFFFFF;
  border: 2rpx solid #E5E6EB;
  font-size: 24rpx;
  color: #4E5969;
}

.chip:active {
  background: #FFF1EB;
  border-color: #FF7A45;
  color: #B23A0E;
}

/* ---------- 历史会话弹层 ---------- */
.hist-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 200;
  display: flex;
  align-items: flex-end;
}

.hist-panel {
  width: 100%;
  max-height: 75vh;
  background: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  padding: 28rpx 28rpx calc(28rpx + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
}

.hist-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hist-panel__title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2733;
}

.hist-panel__close {
  font-size: 32rpx;
  color: #A9B0BC;
  padding: 0 8rpx;
}

.hist-panel__search {
  margin-top: 20rpx;
  height: 72rpx;
  padding: 0 24rpx;
  border-radius: 12rpx;
  background: #F7F8FA;
  font-size: 26rpx;
  color: #1F2733;
}

.hist-list {
  max-height: 56vh;
  margin-top: 16rpx;
}

.hist-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  padding: 22rpx 16rpx;
  border-bottom: 2rpx solid #F2F3F5;
}

.hist-item__title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2733;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hist-item__meta {
  font-size: 22rpx;
  color: #A9B0BC;
}

.hist-empty {
  text-align: center;
  padding: 60rpx 0;
  font-size: 26rpx;
  color: #A9B0BC;
}

/* ---------- 底部输入栏 ---------- */
.input-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #F7F7F7;
  border-top: 2rpx solid #E5E5E5;
}

.input-bar__field {
  flex: 1;
  height: 72rpx;
  padding: 0 28rpx;
  border-radius: 12rpx;
  background: #FFFFFF;
  font-size: 28rpx;
  color: #1F2733;
}

.input-bar__send {
  flex-shrink: 0;
  margin: 0;
  width: 128rpx;
  height: 72rpx;
  line-height: 72rpx;
  padding: 0;
  border: none;
  border-radius: 12rpx;
  background: #E5E6EB;
  color: #A9B0BC;
  font-size: 28rpx;
  font-weight: 600;
}

.input-bar__send.on {
  background: #FF7A45;
  color: #FFFFFF;
}
</style>
