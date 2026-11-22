<template>
  <view class="page">
    <!-- 用户卡 -->
    <view class="profile card">
      <view class="profile__avatar-ring" @click="changeAvatar">
        <image class="profile__avatar" :src="avatar" mode="aspectFill" />
        <view class="profile__avatar-edit" v-if="isLoggedIn"><text>📷</text></view>
      </view>
      <view class="profile__body">
        <text class="profile__name">{{ nickname }}</text>
        <text class="profile__sub">{{ subTitle }}</text>
      </view>
      <button v-if="!isLoggedIn" class="profile__login" @click="go('/pagesUser/login/login')">登录</button>
    </view>

    <!-- 数字摘要 -->
    <view v-if="isLoggedIn" class="summary card">
      <view class="summary__item" @click="go('/pagesUser/my-applications/my-applications')">
        <text class="summary__num num">{{ summary.applicationCount || 0 }}</text>
        <text class="summary__label">我的申请</text>
      </view>
      <view class="summary__item" @click="go('/pagesUser/donation/donation')">
        <text class="summary__num num">{{ summary.favoriteCount || 0 }}</text>
        <text class="summary__label">我的收藏</text>
      </view>
      <view class="summary__item" @click="go('/pagesUser/report/report')">
        <text class="summary__num num">{{ summary.reportCount || 0 }}</text>
        <text class="summary__label">我的上报</text>
      </view>
    </view>

    <!-- 菜单 -->
    <text class="group__label">常用功能</text>
    <view class="menu card">
      <view class="menu__item" @click="go('/pagesUser/article-list/article-list')">
        <text>资讯科普</text><text class="menu__arrow">›</text>
      </view>
      <view class="menu__item" @click="go('/pagesUser/donation/donation')">
        <text>爱心捐赠</text><text class="menu__arrow">›</text>
      </view>
      <view class="menu__item" @click="go('/pagesUser/volunteer/volunteer')">
        <text>志愿者报名</text><text class="menu__arrow">›</text>
      </view>
      <view class="menu__item" @click="go('/pagesUser/report/report')">
        <text>流浪动物上报</text><text class="menu__arrow">›</text>
      </view>
    </view>

    <text v-if="isLoggedIn" class="group__label">账号</text>
    <view v-if="isLoggedIn" class="menu card">
      <view class="menu__item" @click="go('/pagesUser/ai-chat/ai-chat')">
        <text>🤖 AI 宠物顾问</text><text class="menu__arrow">›</text>
      </view>
      <view class="menu__item" @click="go('/pagesShop/coupons/coupons')">
        <text>我的优惠券（积分 {{ userInfo.points || 0 }}）</text><text class="menu__arrow">›</text>
      </view>
      <view class="menu__item" @click="go('/pagesUser/login/login')">
        <text>绑定 / 更换手机号</text><text class="menu__arrow">›</text>
      </view>
      <view class="menu__item" @click="bindWechat">
        <text>微信绑定</text>
        <text class="menu__value" :class="{ ok: userInfo.wechatBound }">
          {{ userInfo.wechatBound ? '已绑定 ✓' : '未绑定，点此绑定' }}
        </text>
      </view>
      <view class="menu__item" @click="go('/pagesUser/adopter-profile/adopter-profile')">
        <text>领养资料</text><text class="menu__arrow">›</text>
      </view>
      <view class="menu__item" @click="go('/pagesUser/my-applications/my-applications')">
        <text>我的回访</text><text class="menu__arrow">›</text>
      </view>
      <view class="menu__item" @click="logout">
        <text style="color:#B3122B;">退出登录</text>
      </view>
    </view>

    <view class="about">
      <text>流浪宠物领养 · 演示版 v1.0.0</text>
    </view>
  </view>
</template>

<script>
import { mySummary, bindWechat as bindWechatApi } from '../../common/api.js';

export default {
  data() {
    return {
      summary: {}
    };
  },
  computed: {
    isLoggedIn() {
      return this.$store.getters.isLoggedIn;
    },
    userInfo() {
      return this.$store.state.userInfo || {};
    },
    nickname() {
      return this.$store.getters.nickname;
    },
    avatar() {
      return this.userInfo.avatar || '/static/avatar-default.png';
    },
    subTitle() {
      if (!this.isLoggedIn) return '登录后同步申请与收藏';
      if (this.userInfo.phone) return '已绑定 ' + this.userInfo.phone + ' · 积分 ' + (this.userInfo.points || 0);
      return this.userInfo.city || '还没绑定手机号，点下方绑定';
    }
  },
  onShow() {
    if (this.isLoggedIn) {
      this.loadSummary();
      this.$store.dispatch('loadMe');
    }
  },
  methods: {
    /** 更换头像：选图 → 上传 MinIO（bizType=avatar）→ 保存到个人资料。
     *  上传层自带 20s 超时，无论成功/失败/超时都会结束加载态，不会卡在「上传中」。 */
    changeAvatar() {
      if (!this.isLoggedIn) {
        uni.navigateTo({ url: '/pagesUser/login/login' });
        return;
      }
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: (res) => {
          const path = res.tempFilePaths && res.tempFilePaths[0];
          if (!path) return;
          let ok = false;
          // 加载框/进度/超时由 http.upload 统一管理，此处不再重复 show
          http.upload(path, 'avatar')
            .then(async (data) => {
              await updateProfile({ avatar: data.url });
              await this.$store.dispatch('loadMe');
              ok = true;
            })
            .catch((e) => {
              // 失败原因（超时/网络/权限）都给到用户可见的提示，不再静默
              uni.showToast({ title: '头像上传失败：' + (e && e.message ? e.message : '请检查网络'), icon: 'none' });
            })
            .finally(() => {
              // 微信端 hideLoading 会连带关掉 toast，故关闭后再补提示
              uni.hideLoading();
              if (ok) setTimeout(() => uni.showToast({ title: '头像已更新', icon: 'success' }), 150);
            });
        },
        fail: (err) => {
          // 用户取消选图或相册权限被拒绝
          if (err && err.errMsg && err.errMsg.indexOf('cancel') === -1) {
            uni.showToast({ title: '选择图片失败，请检查相册权限', icon: 'none' });
          }
        }
      });
    },
    async loadSummary() {
      try {
        this.summary = (await mySummary()) || {};
      } catch (e) { /* */ }
    },
    /** 绑定微信：wx.login 拿 code 换 openid 绑到当前账号；临时微信账号会被串联合并进来 */
    bindWechat() {
      if (this.userInfo.wechatBound) {
        uni.showToast({ title: '已绑定微信', icon: 'none' });
        return;
      }
      uni.login({
        provider: 'weixin',
        success: async (res) => {
          try {
            uni.showLoading({ title: '绑定中', mask: true });
            const vo = await bindWechatApi(res.code);
            if (vo && vo.token) {
              await this.$store.dispatch('loginSuccess', vo);
            } else {
              await this.$store.dispatch('loadMe');
            }
            uni.hideLoading();
            uni.showToast({ title: '微信绑定成功', icon: 'success' });
          } catch (e) {
            uni.hideLoading();
          }
        },
        fail: () => {
          uni.showToast({ title: '获取微信授权失败', icon: 'none' });
        }
      });
    },
    async logout() {
      const confirmed = await new Promise((resolve) => {
        uni.showModal({
          title: '退出登录',
          content: '确定退出当前账号吗？',
          success: (res) => resolve(res.confirm)
        });
      });
      if (!confirmed) return;
      await this.$store.dispatch('logout');
      uni.showToast({ title: '已退出', icon: 'none' });
    },
    go(url) {
      uni.navigateTo({ url });
    }
  }
};
</script>

<style scoped>
/* 用户卡：浅主色渐变 + 头像环 */
.profile {
  position: relative;
  display: flex;
  align-items: center;
  margin-top: 24rpx;
  padding: 32rpx;
  background: linear-gradient(135deg, #FFF1EB 0%, #FFFFFF 64%);
}

.profile__avatar-ring {
  position: relative;
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background: #FFFFFF;
  box-shadow: 0 6rpx 18rpx rgba(255, 122, 69, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.profile__avatar-edit {
  position: absolute;
  right: -4rpx;
  bottom: -4rpx;
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #FF7A45;
  border: 4rpx solid #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile__avatar-edit text {
  font-size: 20rpx;
  line-height: 1;
}

.profile__avatar {
  width: 116rpx;
  height: 116rpx;
  border-radius: 50%;
  background: #F2F3F5;
}

.profile__body {
  flex: 1;
  min-width: 0;
  margin-left: 24rpx;
}

.profile__name {
  font-size: 36rpx;
  font-weight: 600;
  color: #1F2733;
}

.profile__sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #667080;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile__login {
  height: 68rpx;
  line-height: 68rpx;
  padding: 0 32rpx;
  border: none;
  border-radius: 34rpx;
  background: #FF7A45;
  color: #1F2733;
  font-size: 28rpx;
  font-weight: 600;
}

.profile__login::after {
  border: none;
}

/* 数字摘要 */
.summary {
  display: flex;
  padding: 28rpx 0;
}

.summary__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary__item + .summary__item {
  border-left: 2rpx solid #F0F1F4;
}

.summary__num {
  font-size: 40rpx;
  font-weight: 600;
  color: #1F2733;
  font-variant-numeric: tabular-nums;
}

.summary__label {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: #667080;
}

/* 菜单 */
.menu {
  padding: 0 28rpx;
}

.menu__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 104rpx;
  font-size: 30rpx;
  color: #4E5969;
  border-bottom: 2rpx solid #F7F8FA;
}

.menu__value {
  font-size: 26rpx;
  color: #A9B0BC;
}

.menu__value.ok {
  color: #0F6F63;
}

.menu__item:last-child {
  border-bottom: none;
}

.menu__arrow {
  color: #A9B0BC;
  font-size: 32rpx;
}

.about {
  text-align: center;
  padding: 48rpx 0;
  color: #A9B0BC;
  font-size: 22rpx;
}
</style>
