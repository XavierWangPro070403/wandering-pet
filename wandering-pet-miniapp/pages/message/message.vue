

/* AI 宠物顾问常驻入口 */
.ai-session {
  border: 2rpx solid #FFE0CE;
  background: linear-gradient(135deg, #FFF6EF, #FFFFFF);
}

.ai-session__avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 14rpx;
  background: linear-gradient(145deg, #FF9A62, #FF7A45);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  flex-shrink: 0;
}

.ai-session__arrow {
  color: #C0C4CC;
  font-size: 32rpx;
}<template>
  <view class="page">
    <view v-if="!loggedIn" class="empty" style="padding-top:240rpx;">
      <text class="empty__title">登录后查看消息</text>
      <text class="empty__desc">站内消息与咨询回复都会出现在这里</text>
      <button class="btn-primary" style="width:320rpx;margin:32rpx auto 0;" @click="goLogin">登录</button>
    </view>

    <template v-else>
      <!-- Tab：通知 / 咨询 -->
      <view class="tabs">
        <view class="tabs__item" :class="{ active: tab === 'notice' }" @click="switchTab('notice')">
          <text>通知</text>
          <view v-if="noticeUnread" class="tabs__dot"><text>{{ noticeUnread }}</text></view>
        </view>
        <view class="tabs__item" :class="{ active: tab === 'chat' }" @click="switchTab('chat')">
          <text>咨询</text>
          <view v-if="chatUnread" class="tabs__dot"><text>{{ chatUnread }}</text></view>
        </view>
      </view>

      <!-- 通知列表 -->
      <view v-if="tab === 'notice'">
        <view class="notice-head" v-if="notices.length">
          <text class="notice-head__clear" @click="readAll">全部已读</text>
        </view>
        <view
          v-for="item in notices"
          :key="item.id"
          class="notice card"
          :class="{ unread: !item.isRead }"
          @click="onNotice(item)">
          <view class="notice__row">
            <text class="notice__title">{{ item.title }}</text>
            <text class="notice__time num">{{ item.createTime }}</text>
          </view>
          <text class="notice__content">{{ item.content }}</text>
          <view v-if="!item.isRead" class="notice__badge"><text>未读</text></view>
        </view>
        <view v-if="!notices.length" class="empty">
          <text class="empty__title">暂无通知</text>
          <text class="empty__desc">领养进度、回访提醒会出现在这里</text>
        </view>
      </view>

      <!-- 会话列表 -->
      <view v-else>
        <!-- AI 宠物顾问：常驻入口 -->
        <view class="session card ai-session" @click="goAi">
          <view class="ai-session__avatar"><text>🤖</text></view>
          <view class="session__body">
            <view class="session__row">
              <text class="session__name">AI 宠物顾问 · 小爪</text>
            </view>
            <view class="session__row">
              <text class="session__last">养宠、领养问题，秒问秒答</text>
            </view>
          </view>
          <text class="ai-session__arrow">›</text>
        </view>
        <view
          v-for="s in sessions"
          :key="s.id"
          class="session card"
          @click="openChat(s)">
          <image class="session__avatar" :src="s.shelterLogo || '/static/avatar-shelter.png'" mode="aspectFill" />
          <view class="session__body">
            <view class="session__row">
              <text class="session__name">{{ s.shelterName }}</text>
              <text class="session__time num">{{ s.lastTime }}</text>
            </view>
            <view class="session__row">
              <text class="session__last">{{ s.lastMessage }}</text>
              <view v-if="s.unread" class="session__unread num"><text>{{ s.unread }}</text></view>
            </view>
          </view>
        </view>
        <view v-if="!sessions.length" class="empty">
          <text class="empty__title">暂无咨询</text>
          <text class="empty__desc">在宠物档案页可以联系救助站</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script>
import { myNotifications, readNotification, readAllNotifications, unreadCount, mySessions, chatUnreadTotal } from '../../common/api.js';

export default {
  data() {
    return {
      tab: 'notice',
      notices: [],
      sessions: [],
      noticeUnread: 0,
      chatUnread: 0,
      timer: null
    };
  },
  computed: {
    loggedIn() {
      return this.$store.getters.isLoggedIn;
    }
  },
  onShow() {
    if (this.loggedIn) {
      this.load();
      this.timer = setInterval(this.refreshUnread, 15000);
    }
  },
  onHide() {
    if (this.timer) clearInterval(this.timer);
  },
  methods: {
    goAi() {
      uni.navigateTo({ url: '/pagesUser/ai-chat/ai-chat' });
    },
    async load() {
      await Promise.all([this.loadNotices(), this.loadSessions()]);
      this.refreshUnread();
    },
    async loadNotices() {
      try {
        const data = await myNotifications({ pageNum: 1, pageSize: 20 });
        this.notices = (data && data.list) || [];
      } catch (e) { /* */ }
    },
    async loadSessions() {
      try {
        this.sessions = (await mySessions()) || [];
      } catch (e) { /* */ }
    },
    async refreshUnread() {
      try {
        this.noticeUnread = (await unreadCount()) || 0;
        this.chatUnread = (await chatUnreadTotal()) || 0;
      } catch (e) { /* */ }
    },
    switchTab(tab) {
      this.tab = tab;
    },
    async readAll() {
      try {
        await readAllNotifications();
        this.loadNotices();
        this.refreshUnread();
      } catch (e) { /* */ }
    },
    async onNotice(item) {
      if (!item.isRead) {
        await readNotification(item.id).catch(() => {});
        item.isRead = 1;
        this.refreshUnread();
      }
      // 申请相关通知跳转进度详情
      if (item.bizType === 'APPLICATION' && item.bizId) {
        uni.navigateTo({ url: '/pagesUser/application-progress/application-progress?id=' + item.bizId });
      }
    },
    openChat(s) {
      uni.navigateTo({ url: '/pagesUser/chat/chat?sessionId=' + s.id });
    },
    goLogin() {
      uni.navigateTo({ url: '/pagesUser/login/login' });
    }
  }
};
</script>

<style scoped>
/* Tab：贴顶，切换时不跳动 */
.tabs {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  background: #FFFFFF;
  border-bottom: 2rpx solid #EDEFF2;
}

.tabs__item {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 92rpx;
  font-size: 30rpx;
  color: #667080;
}

.tabs__item.active {
  color: #1F2733;
  font-weight: 600;
}

.tabs__item.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: #FF7A45;
}

.tabs__dot {
  min-width: 32rpx;
  height: 32rpx;
  margin-left: 8rpx;
  padding: 0 8rpx;
  border-radius: 16rpx;
  background: #F5455C;
  color: #FFFFFF;
  font-size: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice-head {
  display: flex;
  justify-content: flex-end;
  padding: 20rpx 32rpx 4rpx;
}

.notice-head__clear {
  font-size: 24rpx;
  color: #B23A0E;
}

/* 通知卡：未读左侧主色条 */
.notice {
  position: relative;
  border-left: 6rpx solid transparent;
}

.notice.unread {
  border-left-color: #FF7A45;
}

.notice__row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 16rpx;
}

.notice__title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2733;
}

.notice__time {
  font-size: 22rpx;
  color: #A9B0BC;
  flex-shrink: 0;
}

.notice__content {
  display: block;
  margin-top: 10rpx;
  padding-right: 64rpx;
  font-size: 26rpx;
  line-height: 40rpx;
  color: #4E5969;
}

.notice__badge {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  font-size: 20rpx;
  color: #B23A0E;
}

/* 会话行 */
.session {
  display: flex;
  align-items: center;
}

.session__avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background: #F2F3F5;
}

.session__body {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
}

.session__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session__name {
  font-size: 30rpx;
  font-weight: 500;
  color: #1F2733;
}

.session__time {
  font-size: 22rpx;
  color: #A9B0BC;
}

.session__last {
  flex: 1;
  margin-top: 6rpx;
  font-size: 26rpx;
  color: #667080;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session__unread {
  min-width: 36rpx;
  height: 36rpx;
  margin-left: 12rpx;
  padding: 0 10rpx;
  border-radius: 18rpx;
  background: #F5455C;
  color: #FFFFFF;
  font-size: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
