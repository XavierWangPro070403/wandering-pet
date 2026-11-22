<template>
  <div class="chat">
    <!-- 左侧会话列表 -->
    <aside class="session-panel" aria-label="咨询会话列表">
      <h2 class="session-panel__title">在线咨询</h2>
      <div v-if="sessionsLoading" class="session-skeleton">
        <el-skeleton :rows="4" animated />
      </div>
      <div v-else-if="sessions.length" class="session-list">
        <button
          v-for="s in sessions"
          :key="s.id"
          type="button"
          class="session-item"
          :class="{ active: s.id === activeSessionId }"
          @click="openSession(s)">
          <img class="session-item__avatar" :src="s.userAvatar || '/imgs/avatar-default.svg'" :alt="`${s.userNickname}的头像`" />
          <span class="session-item__meta">
            <span class="session-item__row">
              <span class="session-item__name">{{ s.userNickname || '用户' }}</span>
              <span class="session-item__time num">{{ s.lastTime || '' }}</span>
            </span>
            <span class="session-item__row">
              <span class="session-item__last">{{ s.lastMessage || '暂无消息' }}</span>
              <span v-if="s.unread" class="session-item__badge num">{{ s.unread > 99 ? '99+' : s.unread }}</span>
            </span>
          </span>
        </button>
      </div>
      <EmptyState v-else title="暂无咨询" desc="用户从宠物详情页发起咨询后会出现在这里。" />
    </aside>

    <!-- 右侧聊天窗 -->
    <section class="chat-panel" aria-label="聊天窗口">
      <template v-if="activeSession.id">
        <div class="chat-panel__head">
          <span class="chat-panel__peer">{{ activeSession.userNickname || '用户' }}</span>
          <span class="chat-panel__tip">咨询消息仅双方可见</span>
        </div>

        <div ref="msgList" class="msg-list" role="log" aria-live="polite">
          <div v-if="msgLoading && !messages.length" class="msg-list__loading">
            <el-skeleton :rows="4" animated />
          </div>
          <EmptyState
            v-else-if="!messages.length"
            title="暂无消息"
            desc="向领养咨询的用户发送第一条消息吧。" />
          <template v-else>
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="msg-row"
              :class="{ own: isOwn(msg), recalled: msg.recalled }">
              <!-- 已撤回：像微信一样显示一条居中提示 -->
              <div v-if="msg.recalled" class="msg-row__recalled">
                {{ isOwn(msg) ? '你撤回了一条消息' : '对方撤回了一条消息' }}
              </div>
              <template v-else>
                <img class="msg-row__avatar" :src="avatarOf(msg)" alt="" />
                <div class="msg-row__body">
                  <!-- 领养申请卡片（提交申请后自动发送，点击处理申请） -->
                  <div
                    v-if="msg.contentType === 'APPLICATION'"
                    class="msg-row__applycard"
                    @click="$router.push('/shelter/applications')">
                    <img class="msg-row__applycard__cover" :src="cardOf(msg).petCover" alt="宠物照片" />
                    <div class="msg-row__applycard__info">
                      <span class="msg-row__applycard__badge">📋 领养申请</span>
                      <strong>{{ cardOf(msg).petName }}</strong>
                      <span>申请人：{{ cardOf(msg).applicantName }} {{ cardOf(msg).applicantPhone }}</span>
                      <span class="msg-row__applycard__reason">{{ cardOf(msg).reason }}</span>
                      <span class="msg-row__applycard__more">处理申请 ›</span>
                    </div>
                  </div>
                  <div class="msg-row__bubble" v-else>
                    <img
                      v-if="msg.contentType === 'IMAGE' && msg.objectUrl"
                      class="msg-row__image"
                      :src="msg.objectUrl"
                      alt="图片消息"
                      @click="previewImage(msg.objectUrl)" />
                    <span v-else class="msg-row__text">{{ msg.content }}</span>
                  </div>
                  <span class="msg-row__time num">{{ msg.createTime }}</span>
                </div>
                <!-- 悬停出现：撤回（自己 + 2 分钟内）/ 删除 -->
                <el-dropdown
                  class="msg-row__more"
                  trigger="click"
                  placement="bottom"
                  @command="(cmd) => onMsgCommand(cmd, msg)">
                  <button type="button" class="msg-row__more-btn" aria-label="消息操作">···</button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item v-if="canRecall(msg)" command="recall">撤回</el-dropdown-item>
                    <el-dropdown-item command="delete">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </template>
            </div>
          </template>
        </div>

        <div class="input-row">
          <el-upload
            class="input-row__upload"
            action=""
            accept="image/jpeg,image/png,image/webp"
            :show-file-list="false"
            :http-request="sendImage">
            <button type="button" class="input-row__image-btn" aria-label="发送图片">
              <i class="el-icon-picture-outline" aria-hidden="true"></i>
            </button>
          </el-upload>
          <el-input
            v-model="draft"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="输入回复内容，Enter 发送，Shift + Enter 换行"
            @keydown.enter.native="onEnter" />
          <el-button type="primary" class="input-row__send" :loading="sending" @click="sendText">发送</el-button>
        </div>
      </template>

      <div v-else class="chat-panel__placeholder">
        <EmptyState title="暂无咨询" desc="在左侧选择一个会话，回复用户的领养咨询。" />
      </div>
    </section>
  </div>
</template>

<script>
import EmptyState from '@/components/EmptyState.vue';
import { mySessions, sessionMessages, sendMessage, readSession, recallMessage, deleteMessage } from '@/api/chat';
import { uploadFile } from '@/api/file';
import { subscribeChatPush } from '@/utils/ws';

/**
 * 在线咨询：会话列表 + 聊天窗 + WebSocket 实时接收（发送走 HTTP）
 */
export default {
  name: 'ShelterChat',
  components: { EmptyState },
  data() {
    return {
      sessionsLoading: true,
      sessions: [],
      activeSession: {},
      activeSessionId: '',
      msgLoading: false,
      messages: [],
      draft: '',
      sending: false,
      unsubscribePush: null
    };
  },
  mounted() {
    this.loadSessions();
    // 实时接收：WebSocket 推送替代 3 秒轮询
    this.unsubscribePush = subscribeChatPush((data) => this.onChatPush(data));
  },
  beforeDestroy() {
    if (this.unsubscribePush) this.unsubscribePush();
  },
  methods: {
    /** WS 实时推送处理：新消息 / 撤回事件 */
    onChatPush(data) {
      if (data.type === 'CHAT_MESSAGE') {
        const msg = data.message;
        if (String(msg.sessionId) !== String(this.activeSessionId)) {
          // 其他会话来了新消息：刷新列表未读
          this.loadSessionsSilent();
          return;
        }
        // 发送方自己的消息由 HTTP 响应带回，这里只追加用户发来的
        if (!this.messages.some((m) => m.id === msg.id)) {
          this.messages.push(msg);
          this.scrollToBottom();
          readSession(this.activeSessionId).then(() => {
            this.loadSessionsSilent();
          }).catch(() => {});
        }
      } else if (data.type === 'CHAT_RECALL') {
        if (String(data.sessionId) === String(this.activeSessionId)) {
          const target = this.messages.find((m) => m.id === data.messageId);
          if (target) {
            target.recalled = true;
            target.content = '';
            target.objectUrl = null;
          }
        }
      }
    },
    /** 申请卡片消息解析（content 为 JSON 文本） */
    cardOf(msg) {
      try {
        return JSON.parse(msg.content);
      } catch (e) {
        return {};
      }
    },
    isOwn(msg) {
      return msg.senderType === 'SHELTER';
    },
    avatarOf(msg) {
      if (this.isOwn(msg)) {
        return this.activeSession.shelterLogo || '/imgs/logo.svg';
      }
      return this.activeSession.userAvatar || '/imgs/avatar-default.svg';
    },
    async loadSessions() {
      try {
        this.sessions = (await mySessions()) || [];
        // 默认选中第一个会话
        if (!this.activeSessionId && this.sessions.length) {
          this.openSession(this.sessions[0]);
        }
        // 同步未读数到左侧列表
        if (this.activeSessionId) {
          const current = this.sessions.find((s) => s.id === this.activeSessionId);
          if (current) this.activeSession = current;
        }
      } catch (e) {
        this.sessions = [];
      } finally {
        this.sessionsLoading = false;
      }
    },
    openSession(session) {
      // 切换会话：先清理旧轮询状态再加载，避免串台
      this.activeSession = session;
      this.activeSessionId = session.id;
      this.messages = [];
      this.loadMessages();
      readSession(session.id).then(() => {
        session.unread = 0;
        this.loadSessionsSilent();
      }).catch(() => {});
    },
    loadSessionsSilent() {
      mySessions()
        .then((list) => {
          this.sessions = list || [];
          const current = this.sessions.find((s) => s.id === this.activeSessionId);
          if (current) this.activeSession = current;
        })
        .catch(() => {});
    },
    loadMessages() {
      if (!this.activeSessionId) return;
      this.msgLoading = true;
      sessionMessages(this.activeSessionId, { pageNum: 1, pageSize: 50 })
        .then((page) => {
          // 接口按时间倒序返回，展示时转为正序
          const records = (page && (page.records || page.list)) || [];
          this.messages = records.slice().reverse();
          this.scrollToBottom();
        })
        .catch(() => {
          this.messages = [];
        })
        .finally(() => {
          this.msgLoading = false;
        });
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const el = this.$refs.msgList;
        if (el) {
          el.scrollTop = el.scrollHeight;
        }
      });
    },
    onEnter(e) {
      if (e.shiftKey) return;
      e.preventDefault();
      this.sendText();
    },
    sendText() {
      const content = (this.draft || '').trim();
      if (!content) {
        this.$message.warning('请输入回复内容');
        return;
      }
      this.sending = true;
      sendMessage(this.activeSessionId, { contentType: 'TEXT', content })
        .then(() => {
          this.draft = '';
          this.refreshMessagesSilent();
          this.loadSessionsSilent();
        })
        .catch(() => {})
        .finally(() => {
          this.sending = false;
        });
    },
    async sendImage({ file, onError }) {
      try {
        const data = await uploadFile(file, 'dynamic');
        await sendMessage(this.activeSessionId, { contentType: 'IMAGE', content: '图片消息', objectUrl: data.url });
        this.refreshMessagesSilent();
        this.loadSessionsSilent();
      } catch (e) {
        onError(e);
      }
    },
    previewImage(url) {
      // 简易图片预览：新窗口打开原始图片
      window.open(url, '_blank');
    },
    /** 只有自己发的、2 分钟内的消息可以撤回（后端同样校验） */
    canRecall(msg) {
      if (!this.isOwn(msg) || !msg.createTime) return false;
      const sent = new Date(String(msg.createTime).replace(/-/g, '/')).getTime();
      return Date.now() - sent <= 2 * 60 * 1000;
    },
    onMsgCommand(cmd, msg) {
      if (cmd === 'recall') return this.doRecall(msg);
      return this.doDelete(msg);
    },
    async doRecall(msg) {
      try {
        await this.$confirm('撤回后双方都看不到这条消息', '撤回消息', {
          confirmButtonText: '撤回', cancelButtonText: '取消', type: 'warning'
        });
      } catch (e) {
        return;
      }
      try {
        await recallMessage(msg.id);
        this.$message.success('已撤回');
        this.loadMessages();
        this.loadSessionsSilent();
      } catch (e) { /* 拦截器已提示 */ }
    },
    async doDelete(msg) {
      try {
        await this.$confirm('删除后仅你的聊天记录中不再显示，对方不受影响', '删除消息', {
          confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
        });
      } catch (e) {
        return;
      }
      try {
        await deleteMessage(msg.id);
        this.messages = this.messages.filter((m) => m.id !== msg.id);
        this.$message.success('已删除');
      } catch (e) { /* 拦截器已提示 */ }
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.chat {
  @include card;

  display: flex;
  height: calc(100vh - #{$admin-topbar-h} - 48px);
  min-height: 480px;
  overflow: hidden;
}

.session-panel {
  width: 240px;
  flex-shrink: 0;
  border-right: 1px solid $border-color;
  display: flex;
  flex-direction: column;

  &__title {
    padding: $space-4 $space-4 $space-3;
    font-size: $font-w-body-l;
    border-bottom: 1px solid $border-color;
  }

  .session-skeleton {
    padding: $space-4;
  }
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: $space-2;
}

.session-item {
  width: 100%;
  display: flex;
  gap: $space-2;
  padding: $space-3;
  border: none;
  background: transparent;
  border-radius: $radius-base;
  cursor: pointer;
  text-align: left;
  transition: background-color $duration-hover-in $ease-out-quart;

  &:hover {
    background: $bg-page;
  }

  &.active {
    background: $color-primary-light;
  }

  &__avatar {
    width: 40px;
    height: 40px;
    border-radius: $radius-circle;
    object-fit: cover;
    flex-shrink: 0;
  }

  &__meta {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $space-2;
  }

  &__name {
    font-size: $font-w-body;
    font-weight: 600;
    color: $text-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__time {
    font-size: $font-w-caption;
    color: $text-secondary;
    flex-shrink: 0;
  }

  &__last {
    flex: 1;
    min-width: 0;
    font-size: $font-w-caption;
    color: $text-secondary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__badge {
    flex-shrink: 0;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    border-radius: $radius-circle;
    background: $color-danger;
    color: #fff;
    font-size: $font-w-caption;
    line-height: 18px;
    text-align: center;
  }
}

.chat-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;

  &__head {
    height: 56px;
    padding: 0 $space-5;
    border-bottom: 1px solid $border-color;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $space-3;
  }

  &__peer {
    font-size: $font-w-body-l;
    font-weight: 600;
    color: $text-primary;
  }

  &__tip {
    font-size: $font-w-caption;
    color: $text-secondary;
  }

  &__placeholder {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: $space-5;
  display: flex;
  flex-direction: column;
  gap: $space-4;

  &__loading {
    padding: $space-4;
  }
}

.msg-row {
  display: flex;
  gap: $space-2;
  max-width: 76%;

  // 撤回后的系统提示：占满整行居中，不带头像
  &.recalled {
    max-width: 100%;
    align-self: stretch;
    justify-content: center;
  }

  &__recalled {
    padding: 2px 0;
    font-size: $font-w-caption;
    color: $text-disabled;
    text-align: center;
  }

  // 悬停才出现的「···」消息菜单
  &__more {
    align-self: center;
    opacity: 0;
    transition: opacity $duration-hover-in $ease-out-quart;
  }

  &:hover &__more {
    opacity: 1;
  }

  &__more-btn {
    width: 28px;
    height: 28px;
    padding: 0;
    border: none;
    border-radius: $radius-base;
    background: transparent;
    color: $text-secondary;
    font-size: 16px;
    line-height: 1;
    cursor: pointer;
    transition: background-color $duration-hover-in $ease-out-quart,
                color $duration-hover-in $ease-out-quart;

    &:hover {
      background: $bg-page;
      color: $text-primary;
    }
  }

  &.own {
    align-self: flex-end;
    flex-direction: row-reverse;

    .msg-row__bubble {
      background: $color-primary-light;
      border: none;
      color: $text-primary;
    }
  }

  &:not(.own) {
    align-self: flex-start;

    .msg-row__bubble {
      background: $bg-card;
      border: 1px solid $border-color;
      color: $text-regular;
    }
  }

  &__avatar {
    width: 36px;
    height: 36px;
    border-radius: $radius-circle;
    object-fit: cover;
    flex-shrink: 0;
    background: $bg-page;
  }

  &__body {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  &__bubble {
    padding: $space-2 $space-3;
    border-radius: $radius-base;
    font-size: $font-w-body;
    line-height: $lh-w-body;
    word-break: break-word;
  }

  &__image {
    display: block;
    max-width: 220px;
    max-height: 220px;
    border-radius: $radius-small;
    cursor: zoom-in;
  }

  &__applycard {
    display: flex;
    gap: 12px;
    width: 300px;
    padding: 12px;
    border-radius: $radius-base;
    background: #FFFFFF;
    border-left: 4px solid $color-primary;
    box-shadow: 0 2px 8px rgba(31, 39, 51, 0.08);
    cursor: pointer;

    &:hover { box-shadow: 0 4px 12px rgba(255, 122, 69, 0.25); }

    &__cover {
      width: 64px;
      height: 64px;
      border-radius: $radius-small;
      object-fit: cover;
      background: $bg-page;
      flex-shrink: 0;
    }

    &__info {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 4px;

      strong { font-size: 14px; color: $text-primary; }
      span { font-size: 12px; color: $text-secondary; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    }

    &__badge {
      align-self: flex-start;
      padding: 1px 8px;
      border-radius: 999px;
      background: #FFF1EB;
      font-size: 11px;
      color: $color-primary-text;
    }

    &__reason {
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      white-space: normal !important;
    }

    &__more {
      color: $color-primary-text;
      font-size: 12px;
    }
  }

  &__time {
    font-size: $font-w-caption;
    color: $text-disabled;
  }
}

.own .msg-row__time {
  text-align: right;
}

.input-row {
  border-top: 1px solid $border-color;
  padding: $space-3 $space-5;
  display: flex;
  align-items: flex-end;
  gap: $space-3;

  &__upload {
    line-height: 1;

    ::v-deep .el-upload {
      line-height: 1;
    }
  }

  &__image-btn {
    width: 40px;
    height: 40px;
    border: 1px solid $border-color;
    border-radius: $radius-base;
    background: $bg-card;
    color: $text-regular;
    font-size: 18px;
    cursor: pointer;
    transition: border-color $duration-hover-in $ease-out-quart, color $duration-hover-in $ease-out-quart;

    &:hover {
      border-color: $color-primary;
      color: $color-primary-text;
    }
  }

  .el-textarea {
    flex: 1;
  }

  &__send {
    flex-shrink: 0;
  }
}
</style>
