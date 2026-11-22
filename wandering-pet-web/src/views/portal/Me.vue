<template>
  <div class="me-page">
    <div class="wrap me-layout">
      <!-- 左侧菜单 -->
      <aside class="me-aside" aria-label="个人中心菜单">
        <div class="me-user">
          <img class="me-user__avatar" :src="profile.avatar || defaultAvatar" alt="我的头像" />
          <div class="me-user__info">
            <h2 class="me-user__name">{{ profile.nickname || '—' }}</h2>
            <p class="me-user__phone num">{{ profile.phone || '未绑定手机号' }}</p>
          </div>
        </div>
        <nav class="me-menu">
          <button
            v-for="m in menus"
            :key="m.key"
            class="me-menu__item"
            :class="{ active: activeTab === m.key }"
            @click="switchTab(m.key)">
            <i :class="m.icon" aria-hidden="true"></i>
            <span>{{ m.label }}</span>
            <span v-if="m.key === 'messages' && summary.unreadCount" class="me-menu__badge num">{{ summary.unreadCount }}</span>
          </button>
        </nav>
      </aside>

      <!-- 右侧内容 -->
      <section class="me-content">
        <!-- 数字摘要 -->
        <div class="me-summary" aria-label="账户摘要">
          <button class="me-summary__item" @click="switchTab('applications')">
            <span class="me-summary__num num">{{ summary.applicationCount || 0 }}</span>
            <span class="me-summary__label">申请总数</span>
          </button>
          <button class="me-summary__item" @click="switchTab('applications')">
            <span class="me-summary__num num">{{ summary.activeApplicationCount || 0 }}</span>
            <span class="me-summary__label">进行中申请</span>
          </button>
          <button class="me-summary__item" @click="switchTab('favorites')">
            <span class="me-summary__num num">{{ summary.favoriteCount || 0 }}</span>
            <span class="me-summary__label">我的收藏</span>
          </button>
          <button class="me-summary__item" @click="switchTab('messages')">
            <span class="me-summary__num num">{{ summary.unreadCount || 0 }}</span>
            <span class="me-summary__label">未读消息</span>
          </button>
        </div>

        <!-- 我的资料 -->
        <div v-if="activeTab === 'profile'" class="panel">
          <div class="panel__head">
            <h2>我的资料</h2>
            <p>头像与昵称会展示在评论和站内消息中。</p>
          </div>
          <el-form :model="profileForm" label-position="top" class="panel__form" @submit.native.prevent>
            <!-- 头像上传（独立区块，点击头像或按钮均可上传至 MinIO） -->
            <div class="avatar-block">
              <el-upload
                class="avatar-block__upload"
                :show-file-list="false"
                accept="image/jpeg,image/png,image/webp"
                :before-upload="beforeAvatarUpload"
                :http-request="uploadAvatarRequest">
                <div class="avatar-block__wrap">
                  <img class="avatar-block__preview" :src="profileForm.avatar || defaultAvatar" alt="头像预览" />
                  <span class="avatar-block__cam"><i class="el-icon-camera"></i></span>
                </div>
              </el-upload>
              <div class="avatar-block__text">
                <el-button size="small" round icon="el-icon-upload2" :loading="avatarUploading">
                  {{ profileForm.avatar ? '更换头像' : '上传头像' }}
                </el-button>
                <p class="avatar-block__tip">上传至 MinIO 对象存储，支持 jpg / png / webp，不超过 5MB</p>
              </div>
            </div>
            <el-row :gutter="24">
              <el-col :xs="24" :sm="12">
                <el-form-item label="昵称">
                  <el-input v-model="profileForm.nickname" maxlength="32" placeholder="填写昵称" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="性别">
                  <el-radio-group v-model="profileForm.gender">
                    <el-radio v-for="g in genderOptions" :key="g.value" :label="g.value">{{ g.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="城市">
                  <el-input v-model="profileForm.city" maxlength="32" placeholder="如：杭州" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="手机号">
                  <el-input :value="profile.phone" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存资料</el-button>
          </el-form>

          <!-- 账号串联：账号密码 / 手机号 / 微信三种登录方式绑定到同一个账号 -->
          <div class="link-box">
            <div class="link-box__head">
              <h3>账号串联</h3>
              <p>把账号密码、手机号、微信三种登录方式绑定到同一个账号，任意一种方式登录都是同一个身份，申请、收藏与消息完全互通。</p>
            </div>
            <div class="link-box__row">
              <span class="link-item">
                <i :class="profile.username ? 'el-icon-success ok' : 'el-icon-remove'" aria-hidden="true"></i>
                账号密码 {{ profile.username ? '已启用' : '未设置' }}
              </span>
              <span class="link-item">
                <i :class="profile.phone ? 'el-icon-success ok' : 'el-icon-remove'" aria-hidden="true"></i>
                手机号 {{ profile.phone || '未绑定' }}
              </span>
              <span class="link-item">
                <i :class="profile.wechatBound ? 'el-icon-success ok' : 'el-icon-remove'" aria-hidden="true"></i>
                微信 {{ profile.wechatBound ? '已绑定' : '未绑定（请在小程序端「我的 - 微信绑定」操作）' }}
              </span>
            </div>
            <template v-if="bindEditing">
              <div class="link-box__form">
                <el-input v-model="bindForm.phone" maxlength="11" placeholder="11 位手机号" class="link-box__input" />
                <el-input v-model="bindForm.code" maxlength="6" placeholder="短信验证码" class="link-box__input link-box__input--code" />
                <el-button :disabled="bindCooldown > 0" :loading="bindSending" @click="sendBindCode">
                  {{ bindCooldown > 0 ? bindCooldown + 's 后重发' : '获取验证码' }}
                </el-button>
                <el-button type="primary" :loading="bindSubmitting" @click="submitBindPhone">
                  {{ profile.phone ? '换绑手机号' : '绑定手机号' }}
                </el-button>
              </div>
              <p class="link-box__tip">绑定他人手机号时，两个账号会自动串联合并为一个账号（资料与记录全部保留）。</p>
            </template>
            <el-button v-else size="small" icon="el-icon-connection" @click="bindEditing = true">
              {{ profile.phone ? '换绑手机号' : '绑定手机号' }}
            </el-button>
          </div>
        </div>

        <!-- 实名认证 -->
        <div v-else-if="activeTab === 'realname'" class="panel">
          <div class="panel__head">
            <h2>实名认证</h2>
            <p>实名信息仅用于签署领养协议，不会公开展示。</p>
          </div>
          <div v-if="profile.realNameStatus === 1" class="real-done">
            <i class="el-icon-success" aria-hidden="true"></i>
            <h3>已完成实名认证</h3>
            <p>认证姓名：{{ profile.realName }}</p>
          </div>
          <el-form v-else ref="realNameForm" :model="realNameForm" :rules="realNameRules" label-position="top" class="panel__form" @submit.native.prevent>
            <el-row :gutter="24">
              <el-col :xs="24" :sm="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="realNameForm.realName" maxlength="32" placeholder="与身份证一致" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="身份证号" prop="idCardNo">
                  <el-input v-model="realNameForm.idCardNo" maxlength="18" placeholder="18 位身份证号" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-button type="primary" :loading="realNameSaving" @click="submitRealName">提交认证</el-button>
            <p class="panel__tip">提交后不可修改，请确认信息准确。</p>
          </el-form>
        </div>

        <!-- 领养资料 -->
        <div v-else-if="activeTab === 'adopter'" class="panel">
          <div class="panel__head">
            <h2>领养资料</h2>
            <p>补全后申请领养时自动带入，多只宠物免重复填写；每次提交申请也会自动更新这份资料。</p>
          </div>
          <el-form :model="adopterForm" label-position="top" class="panel__form" @submit.native.prevent>
            <el-row :gutter="24">
              <el-col :xs="24" :sm="12">
                <el-form-item label="申请人姓名">
                  <el-input v-model="adopterForm.adopterName" maxlength="32" placeholder="真实姓名" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="联系电话">
                  <el-input :value="profile.phone || '未绑定手机号'" disabled />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="年龄">
                  <el-input-number v-model="adopterForm.adopterAge" :min="18" :max="100" placeholder="须年满 18 周岁" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="现居住城市">
                  <el-input v-model="adopterForm.city" maxlength="32" placeholder="如：杭州" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="职业">
                  <el-input v-model="adopterForm.occupation" maxlength="32" placeholder="如：教师 / 工程师（选填）" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="住房类型">
                  <el-select v-model="adopterForm.housingType" placeholder="请选择住房类型" style="width:100%">
                    <el-option label="自有住房" value="OWN" />
                    <el-option label="租房" value="RENT" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="有养宠经验">
                  <el-radio-group v-model="adopterForm.hasPetExperience">
                    <el-radio :label="1">是</el-radio>
                    <el-radio :label="0">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="家人同意饲养">
                  <el-radio-group v-model="adopterForm.familyAgree">
                    <el-radio :label="1">是</el-radio>
                    <el-radio :label="0">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="家中有儿童">
                  <el-radio-group v-model="adopterForm.hasChildren">
                    <el-radio :label="1">是</el-radio>
                    <el-radio :label="0">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="家人有过敏史">
                  <el-radio-group v-model="adopterForm.hasAllergy">
                    <el-radio :label="1">是</el-radio>
                    <el-radio :label="0">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-button type="primary" :loading="adopterSaving" @click="saveAdopterProfile">保存领养资料</el-button>
          </el-form>
        </div>

        <!-- 商城订单 -->
        <div v-else-if="activeTab === 'shoporders'" class="panel">
          <div class="panel__head">
            <h2>商城订单</h2>
            <p>宠物用品购买记录；待支付订单可直接支付或取消。</p>
          </div>
          <el-radio-group v-model="shopOrderStatus" size="small" style="margin-bottom:16px" @change="loadShopOrders">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="PENDING_PAYMENT">待支付</el-radio-button>
            <el-radio-button label="PAID">已支付</el-radio-button>
            <el-radio-button label="SHIPPED">已发货</el-radio-button>
            <el-radio-button label="CANCELLED">已取消</el-radio-button>
          </el-radio-group>
          <div v-if="shopOrdersLoading"><el-skeleton :rows="4" animated /></div>
          <div v-else-if="shopOrders.length" class="shop-order-list">
            <div class="shop-order" v-for="o in shopOrders" :key="o.id">
              <div class="shop-order__head">
                <span class="num shop-order__no">{{ o.orderNo }}</span>
                <el-tag size="mini" :type="{ PENDING_PAYMENT: 'warning', PAID: 'success', SHIPPED: 'primary', CANCELLED: 'info' }[o.status]">
                  {{ { PENDING_PAYMENT: '待支付', PAID: '已支付', SHIPPED: '已发货', CANCELLED: '已取消' }[o.status] }}
                </el-tag>
              </div>
              <div class="shop-order__item" v-for="it in o.items" :key="it.id">
                <img class="shop-order__img" :src="it.productCover" :alt="it.productName" />
                <div class="shop-order__info">
                  <span>{{ it.productName }}</span>
                  <span class="num">¥{{ it.price }} × {{ it.quantity }}</span>
                </div>
              </div>
              <div class="shop-order__amounts">
                <span v-if="Number(o.discountAmount) > 0" class="shop-order__discount">券抵扣 ¥{{ o.discountAmount }}</span>
                <span>实付 <strong class="num">¥{{ o.payAmount }}</strong>（+{{ o.pointsEarned }} 积分）</span>
              </div>
              <div class="shop-order__actions" v-if="o.status === 'PENDING_PAYMENT'">
                <el-button size="small" round @click="cancelShopOrder(o)">取消订单</el-button>
                <el-button size="small" type="primary" round @click="payShopOrder(o)">立即支付</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="还没有商城订单" />
        </div>

        <!-- 优惠券 · 积分 -->
        <div v-else-if="activeTab === 'coupons'" class="panel">
          <div class="panel__head">
            <h2>优惠券 · 积分</h2>
            <p>1 元消费 = 1 积分；积分可兑换优惠券，下单自动抵扣。</p>
          </div>
          <div class="points-hero">
            <span>当前积分</span>
            <strong class="num">{{ shopPointsBalance }}</strong>
          </div>
          <h3 class="points-section">可兑换优惠券</h3>
          <div class="coupon-line" v-for="c in shopCouponsList" :key="c.id">
            <div class="coupon-line__left"><strong class="num">¥{{ c.discount }}</strong><span>{{ Number(c.minSpend) > 0 ? '满' + c.minSpend + '可用' : '无门槛' }}</span></div>
            <div class="coupon-line__body"><strong>{{ c.name }}</strong><span>需 {{ c.pointsCost }} 积分 · 已领 {{ c.receivedCount }}/{{ c.totalCount }}</span></div>
            <el-button size="small" type="primary" round :disabled="!c.canExchange || c.receivedCount >= c.totalCount" @click="exchangeCoupon(c)">
              {{ c.receivedCount >= c.totalCount ? '已抢完' : c.canExchange ? '积分兑换' : '积分不足' }}
            </el-button>
          </div>
          <h3 class="points-section">我的优惠券</h3>
          <div class="coupon-line mine" v-for="c in myCoupons" :key="c.id">
            <div class="coupon-line__left"><strong class="num">¥{{ c.discount }}</strong><span>{{ Number(c.minSpend) > 0 ? '满' + c.minSpend + '可用' : '无门槛' }}</span></div>
            <div class="coupon-line__body"><strong>{{ c.couponName }}</strong><span>有效期至 {{ (c.expireTime || '').replace('T', ' ').slice(0, 16) }}</span></div>
            <el-tag size="mini" :type="c.status === 'UNUSED' ? 'success' : 'info'">{{ c.status === 'UNUSED' ? '未使用' : '已使用' }}</el-tag>
          </div>
          <h3 class="points-section">积分流水</h3>
          <div class="points-log" v-for="l in shopPointsLogs" :key="l.id">
            <span>{{ l.remark || { EARN: '消费获得', EXCHANGE: '兑换优惠券' }[l.type] }}</span>
            <span class="num" :class="{ earn: l.change > 0 }">{{ l.change > 0 ? '+' : '' }}{{ l.change }} 分</span>
            <span class="num">{{ l.createTime }}</span>
          </div>
        </div>

        <!-- 我的收藏 -->
        <div v-else-if="activeTab === 'favorites'" class="panel">
          <div class="panel__head">
            <h2>我的收藏</h2>
            <p>收藏的宠物档案，状态变化会在消息中心通知你。</p>
          </div>
          <div v-if="favoritesLoading" class="fav-grid">
            <div v-for="i in 3" :key="i" class="fav-skeleton"><el-skeleton :rows="3" animated /></div>
          </div>
          <div v-else-if="favorites.length" class="fav-grid">
            <article
              v-for="f in favorites"
              :key="f.targetId"
              class="fav-card"
              role="button"
              tabindex="0"
              @click="$router.push(`/pets/${f.targetId}`)"
              @keydown.enter="$router.push(`/pets/${f.targetId}`)">
              <div class="fav-card__media">
                <img :src="f.coverImage" :alt="`${f.name}的照片`" loading="lazy" />
              </div>
              <div class="fav-card__body">
                <p class="fav-card__head">
                  <span class="fav-card__name">{{ f.name }}</span>
                  <StatusTag :status="f.status" />
                </p>
                <p class="fav-card__meta">{{ f.breedName || f.categoryName }} · {{ ageText(f.ageMonth) }}</p>
                <p class="fav-card__time num">收藏于 {{ f.createTime }}</p>
              </div>
            </article>
          </div>
          <EmptyState v-else title="还没有收藏" desc="浏览待领养列表，点收藏把喜欢的宠物先存起来。" action-text="浏览待领养" @action="$router.push('/pets')" />
          <el-pagination
            v-if="favoritesTotal > favoritesQuery.pageSize"
            class="pagination"
            layout="prev, pager, next"
            :total="favoritesTotal"
            :page-size="favoritesQuery.pageSize"
            :current-page.sync="favoritesQuery.pageNum"
            @current-change="loadFavorites" />
        </div>

        <!-- 我的申请（简版） -->
        <div v-else-if="activeTab === 'applications'" class="panel">
          <div class="panel__head">
            <h2>我的申请</h2>
            <el-button type="text" class="panel__more" @click="$router.push('/my/applications')">查看全部申请</el-button>
          </div>
          <div v-if="applicationsLoading"><el-skeleton :rows="4" animated /></div>
          <ul v-else-if="applications.length" class="mini-list">
            <li
              v-for="a in applications"
              :key="a.id"
              class="mini-list__item"
              role="button"
              tabindex="0"
              @click="$router.push(`/my/applications/${a.id}`)"
              @keydown.enter="$router.push(`/my/applications/${a.id}`)">
              <img class="mini-list__cover" :src="a.petCover" :alt="`${a.petName}的照片`" loading="lazy" />
              <div class="mini-list__info">
                <p class="mini-list__name">{{ a.petName }}</p>
                <p class="mini-list__no num">申请号 {{ a.applyNo }} · {{ a.createTime }}</p>
              </div>
              <StatusTag :status="a.status" />
              <i class="el-icon-arrow-right mini-list__arrow" aria-hidden="true"></i>
            </li>
          </ul>
          <EmptyState v-else title="还没有领养申请" action-text="浏览待领养" @action="$router.push('/pets')" />
        </div>

        <!-- 我的回访 -->
        <div v-else-if="activeTab === 'records'" class="panel">
          <div class="panel__head">
            <h2>我的回访</h2>
            <p>领养后第 7/30/90/180 天各有一次回访，配合完成即可。</p>
          </div>
          <div v-if="recordsLoading"><el-skeleton :rows="4" animated /></div>
          <div v-else-if="records.length" class="record-list">
            <article v-for="r in records" :key="r.id" class="record-card">
              <header class="record-card__head">
                <div class="record-card__pet">
                  <img :src="r.petCover" :alt="`${r.petName}的照片`" loading="lazy" />
                  <div>
                    <h3>{{ r.petName }}</h3>
                    <p class="num">领养单号 {{ r.adoptionNo }} · {{ r.shelterName }} · 签约 {{ r.signDate || '—' }}</p>
                  </div>
                </div>
                <StatusTag :status="r.status" />
              </header>
              <ul class="record-card__followups">
                <li v-for="fu in r.followUps || []" :key="fu.id" class="followup">
                  <div class="followup__head">
                    <span class="followup__round">第 {{ fu.round }} 次回访</span>
                    <span class="followup__plan num">计划 {{ fu.planDate || '—' }}</span>
                    <StatusTag :status="fu.status" :label="followUpStatusText(fu.status)" />
                  </div>
                  <p v-if="fu.status === 'PENDING'" class="followup__pending">
                    待填写：请通过小程序或联系机构完成本次回访登记。
                  </p>
                  <p v-else-if="fu.status === 'OVERDUE'" class="followup__overdue">
                    本次回访已逾期，请尽快联系机构安排上门或线上回访。
                  </p>
                  <p v-else class="followup__done">
                    已于 {{ fu.actualDate || '—' }} 完成<template v-if="fu.lifeDesc">：{{ fu.lifeDesc }}</template>
                  </p>
                </li>
              </ul>
              <EmptyState v-if="!(r.followUps || []).length" title="暂无回访任务" />
            </article>
          </div>
          <EmptyState v-else title="还没有领养记录" desc="完成签约领养后，回访任务会出现在这里。" action-text="浏览待领养" @action="$router.push('/pets')" />
        </div>

        <!-- 消息中心 -->
        <div v-else-if="activeTab === 'messages'" class="panel">
          <div class="panel__head">
            <h2>消息中心</h2>
            <el-button type="text" class="panel__more" @click="readAll">全部标记已读</el-button>
          </div>
          <div v-if="notificationsLoading"><el-skeleton :rows="5" animated /></div>
          <ul v-else-if="notifications.length" class="notif-list">
            <li
              v-for="n in notifications"
              :key="n.id"
              class="notif"
              :class="{ unread: !n.isRead }"
              role="button"
              tabindex="0"
              @click="readOne(n)"
              @keydown.enter="readOne(n)">
              <span class="notif__dot" aria-hidden="true"></span>
              <div class="notif__body">
                <p class="notif__title">{{ n.title }}</p>
                <p class="notif__content">{{ n.content }}</p>
                <p class="notif__time num">{{ n.createTime }}</p>
              </div>
            </li>
          </ul>
          <EmptyState v-else title="暂无消息" desc="申请进度与回访提醒都会发到这里。" />
          <el-pagination
            v-if="notificationsTotal > notificationsQuery.pageSize"
            class="pagination"
            layout="prev, pager, next"
            :total="notificationsTotal"
            :page-size="notificationsQuery.pageSize"
            :current-page.sync="notificationsQuery.pageNum"
            @current-change="loadNotifications" />
        </div>

        <!-- 收货地址 -->
        <div v-else-if="activeTab === 'addresses'" class="panel">
          <div class="panel__head">
            <h2>收货地址</h2>
            <el-button type="primary" size="small" icon="el-icon-plus" @click="openAddressDialog(null)">新增地址</el-button>
          </div>
          <div v-if="addressLoading"><el-skeleton :rows="3" animated /></div>
          <div v-else-if="addressList.length" class="addr-list">
            <div v-for="addr in addressList" :key="addr.id" class="addr-card">
              <div class="addr-card__body">
                <p class="addr-card__line">
                  <span class="addr-card__receiver">{{ addr.receiver }}</span>
                  <span class="num">{{ addr.phone }}</span>
                  <span v-if="addr.isDefault === 1" class="addr-card__default">默认地址</span>
                </p>
                <p class="addr-card__detail">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</p>
              </div>
              <div class="addr-card__actions">
                <el-button v-if="addr.isDefault !== 1" type="text" @click="setDefaultAddress(addr)">设为默认</el-button>
                <el-button type="text" @click="openAddressDialog(addr)">编辑地址</el-button>
                <el-button type="text" class="addr-card__delete" @click="removeAddress(addr)">删除地址</el-button>
              </div>
            </div>
          </div>
          <EmptyState v-else title="还没有收货地址" desc="参与线下活动或接收物资时需要一个收货地址。" />
        </div>

        <!-- 联系机构（简易聊天） -->
        <div v-else-if="activeTab === 'chat'" class="panel panel--chat">
          <div class="panel__head">
            <h2>联系机构</h2>
            <p>与救助站沟通申请进度、面谈安排与领养后续。</p>
          </div>
          <div class="chat">
            <div class="chat__list" aria-label="会话列表">
              <button
                v-for="s in sessions"
                :key="s.id"
                class="chat__session"
                :class="{ active: currentSessionId === s.id }"
                @click="selectSession(s)">
                <img class="chat__avatar" :src="s.shelterLogo || defaultAvatar" alt="" />
                <span class="chat__session-info">
                  <span class="chat__session-name">{{ s.shelterName }}</span>
                  <span class="chat__session-last">{{ s.lastMessage || '暂无消息' }}</span>
                </span>
                <span v-if="s.unread" class="chat__unread num">{{ s.unread }}</span>
              </button>
              <EmptyState v-if="!sessions.length && !chatLoading" title="暂无会话" desc="在领养进度页点击“联系机构”即可发起对话。" />
            </div>

            <div class="chat__window">
              <template v-if="currentSessionId">
                <div ref="msgBox" class="chat__messages">
                  <div
                    v-for="m in messages"
                    :key="m.id"
                    class="chat__msg"
                    :class="{ mine: m.senderType === 'USER' }">
                    <div
                      v-if="m.contentType === 'APPLICATION'"
                      class="chat__applycard"
                      @click="$router.push('/my/applications/' + cardOf(m).applicationId)">
                      <img class="chat__applycard__cover" :src="cardOf(m).petCover" alt="宠物照片" />
                      <div class="chat__applycard__info">
                        <span class="chat__applycard__badge">📋 领养申请</span>
                        <strong>{{ cardOf(m).petName }}</strong>
                        <span>查看申请进度 ›</span>
                      </div>
                    </div>
                    <div v-else class="chat__bubble">
                      <img v-if="m.contentType === 'IMAGE'" class="chat__img" :src="m.objectUrl" alt="图片消息" />
                      <p v-else>{{ m.content }}</p>
                    </div>
                    <span class="chat__time num">{{ m.createTime }}</span>
                  </div>
                </div>
                <div class="chat__composer">
                  <el-input
                    v-model="draft"
                    placeholder="输入消息内容"
                    maxlength="1000"
                    @keyup.enter.native="send" />
                  <el-button type="primary" :loading="sending" @click="send">发送消息</el-button>
                </div>
              </template>
              <EmptyState v-else-if="!chatLoading" title="选择一个会话" desc="左侧选择救助站即可查看与发送消息。" />
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 新增/编辑地址弹窗 -->
    <el-dialog :title="addressEditing ? '编辑地址' : '新增地址'" :visible.sync="addressVisible" width="520px" top="8vh">
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-position="top" @submit.native.prevent>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收件人" prop="receiver">
              <el-input v-model="addressForm.receiver" maxlength="32" placeholder="填写收件人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="addressForm.phone" maxlength="11" placeholder="11 位手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="省份" prop="province">
              <el-select v-model="addressForm.province" placeholder="省份" @change="onAddressProvinceChange">
                <el-option v-for="p in regionOptions" :key="p.name" :label="p.name" :value="p.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市" prop="city">
              <el-select v-model="addressForm.city" placeholder="城市" @change="onAddressCityChange">
                <el-option v-for="c in addressCityOptions" :key="c.name" :label="c.name" :value="c.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区县" prop="district">
              <el-select v-model="addressForm.district" placeholder="区县">
                <el-option v-for="d in addressDistrictOptions" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细地址" prop="detail">
              <el-input v-model="addressForm.detail" maxlength="255" placeholder="街道、门牌号（不含省市区）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
      </el-form>
      <template slot="footer">
        <el-button @click="addressVisible = false">取消</el-button>
        <el-button type="primary" :loading="addressSaving" @click="saveAddressForm">{{ addressEditing ? '保存修改' : '保存地址' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import EmptyState from '@/components/EmptyState.vue';
import StatusTag from '@/components/StatusTag.vue';
import { myFavorites } from '@/api/pet';
import { myApplications } from '@/api/adoption';
import { myRecords } from '@/api/adoption';
import { myProfile, updateProfile, realName, mySummary, addresses, saveAddress, updateAddress, deleteAddress, getAdopterProfile, updateAdopterProfile } from '@/api/user';
import { smsBind, sendSmsCode } from '@/api/auth';
import { uploadFile } from '@/api/file';
import { shopOrders as fetchShopOrders, shopPayOrder, shopCancelOrder, shopPoints, shopCoupons, shopExchangeCoupon, shopMyCoupons } from '@/api/shop';
import { myNotifications, readNotification, readAllNotifications } from '@/api/notification';
import { mySessions, sessionMessages, sendMessage, readSession, chatUnreadTotal } from '@/api/chat';
import { ageText, followUpStatusText } from '@/utils/format';
import { subscribeChatPush } from '@/utils/ws';

export default {
  name: 'MePage',
  components: { EmptyState, StatusTag },
  data() {
    return {
      defaultAvatar: '/imgs/avatar-default.svg',
      menus: [
        { key: 'profile', label: '我的资料', icon: 'el-icon-user' },
        { key: 'realname', label: '实名认证', icon: 'el-icon-postcard' },
        { key: 'adopter', label: '领养资料', icon: 'el-icon-notebook-2' },
        { key: 'favorites', label: '我的收藏', icon: 'el-icon-star-off' },
        { key: 'applications', label: '我的申请', icon: 'el-icon-tickets' },
        { key: 'records', label: '我的回访', icon: 'el-icon-date' },
        { key: 'shoporders', label: '商城订单', icon: 'el-icon-shopping-bag-1' },
        { key: 'coupons', label: '优惠券 · 积分', icon: 'el-icon-coin' },
        { key: 'messages', label: '消息中心', icon: 'el-icon-bell' },
        { key: 'addresses', label: '收货地址', icon: 'el-icon-location-information' },
        { key: 'chat', label: '联系机构', icon: 'el-icon-chat-dot-round' }
      ],
      genderOptions: [
        { value: 0, label: '保密' },
        { value: 1, label: '男' },
        { value: 2, label: '女' }
      ],
      regionOptions: [
        {
          name: '浙江省',
          cities: [
            { name: '杭州市', districts: ['上城区', '拱墅区', '西湖区', '滨江区', '余杭区', '临平区', '萧山区'] },
            { name: '宁波市', districts: ['海曙区', '江北区', '鄞州区'] },
            { name: '温州市', districts: ['鹿城区', '龙湾区', '瓯海区'] }
          ]
        },
        {
          name: '四川省',
          cities: [
            { name: '成都市', districts: ['锦江区', '青羊区', '金牛区', '武侯区', '成华区', '高新区', '双流区'] },
            { name: '绵阳市', districts: ['涪城区', '游仙区'] },
            { name: '德阳市', districts: ['旌阳区', '罗江区'] }
          ]
        }
      ],
      profile: {},
      profileForm: { nickname: '', avatar: '', gender: 0, city: '' },
      profileSaving: false,
      avatarUploading: false,
      bindEditing: false,
      bindForm: { phone: '', code: '' },
      bindSending: false,
      bindSubmitting: false,
      bindCooldown: 0,
      summary: {},
      realNameForm: { realName: '', idCardNo: '' },
      realNameRules: {
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        idCardNo: [
          { required: true, message: '请输入身份证号', trigger: 'blur' },
          { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
        ]
      },
      realNameSaving: false,
      adopterForm: {
        adopterName: '',
        city: '',
        adopterAge: null,
        occupation: '',
        housingType: '',
        hasPetExperience: null,
        familyAgree: null,
        hasChildren: null,
        hasAllergy: null
      },
      adopterSaving: false,
      shopOrders: [],
      shopOrdersLoading: false,
      shopOrdersTotal: 0,
      shopOrderStatus: '',
      shopPointsBalance: 0,
      shopPointsLogs: [],
      shopCouponsList: [],
      favorites: [],
      favoritesLoading: false,
      favoritesTotal: 0,
      favoritesQuery: { pageNum: 1, pageSize: 6 },
      applications: [],
      applicationsLoading: false,
      records: [],
      recordsLoading: false,
      notifications: [],
      notificationsLoading: false,
      notificationsTotal: 0,
      notificationsQuery: { pageNum: 1, pageSize: 8 },
      addressList: [],
      addressLoading: false,
      addressVisible: false,
      addressEditing: null,
      addressSaving: false,
      addressForm: {
        receiver: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        detail: '',
        isDefault: false
      },
      addressRules: {
        receiver: [{ required: true, message: '请输入收件人姓名', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        province: [{ required: true, message: '请选择省份', trigger: 'change' }],
        city: [{ required: true, message: '请选择城市', trigger: 'change' }],
        district: [{ required: true, message: '请选择区县', trigger: 'change' }],
        detail: [{ required: true, message: '请填写详细地址', trigger: 'blur' }]
      },
      sessions: [],
      currentSessionId: null,
      messages: [],
      draft: '',
      chatLoading: false,
      sending: false,
      unsubscribePush: null
    };
  },
  computed: {
    activeTab() {
      const t = this.$route.query.tab;
      return this.menus.some((m) => m.key === t) ? t : 'profile';
    },
    addressCityOptions() {
      const province = this.regionOptions.find((p) => p.name === this.addressForm.province);
      return province ? province.cities : [];
    },
    addressDistrictOptions() {
      const city = this.addressCityOptions.find((c) => c.name === this.addressForm.city);
      return city ? city.districts : [];
    }
  },
  watch: {
    activeTab(tab) {
      if (tab === 'chat') {
        this.enterChat();
      }
      this.loadTabData(tab);
    }
  },
  async mounted() {
    if (!this.$store.state.user.token) {
      this.$router.replace(`/login?redirect=${encodeURIComponent(this.$route.fullPath)}`);
      return;
    }
    await Promise.all([this.loadProfile(), this.loadSummary()]);
    this.loadTabData(this.activeTab);
    if (this.activeTab === 'chat') this.enterChat();
    // 实时接收：WebSocket 推送替代 3 秒轮询
    this.unsubscribePush = subscribeChatPush((data) => this.onChatPush(data));
  },
  beforeDestroy() {
    if (this.unsubscribePush) this.unsubscribePush();
  },
  methods: {
    ageText,
    followUpStatusText,
    switchTab(key) {
      if (key === this.activeTab) {
        this.loadTabData(key);
        return;
      }
      this.$router.replace({ query: { ...this.$route.query, tab: key } });
    },
    loadTabData(tab) {
      switch (tab) {
        case 'profile':
        case 'realname':
          this.loadProfile();
          break;
        case 'adopter':
          this.loadAdopterProfile();
          break;
        case 'favorites':
          this.loadFavorites();
          break;
        case 'applications':
          this.loadMyApplications();
          break;
        case 'records':
          this.loadRecords();
          break;
        case 'shoporders':
          this.loadShopOrders();
          break;
        case 'coupons':
          this.loadCouponPanel();
          break;
        case 'messages':
          this.loadNotifications();
          break;
        case 'addresses':
          this.loadAddresses();
          break;
        default:
          break;
      }
    },
    async loadProfile() {
      try {
        this.profile = await myProfile();
        this.profileForm = {
          nickname: this.profile.nickname || '',
          avatar: this.profile.avatar || '',
          gender: this.profile.gender || 0,
          city: this.profile.city || ''
        };
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    /** 领养资料加载（补全表单） */
    async loadAdopterProfile() {
      try {
        const p = await getAdopterProfile();
        if (!p) return;
        const f = this.adopterForm;
        f.adopterName = p.adopterName || '';
        f.city = p.city || '';
        f.adopterAge = p.adopterAge || null;
        f.occupation = p.occupation || '';
        f.housingType = p.housingType || '';
        f.hasPetExperience = p.hasPetExperience ?? null;
        f.familyAgree = p.familyAgree ?? null;
        f.hasChildren = p.hasChildren ?? null;
        f.hasAllergy = p.hasAllergy ?? null;
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    /** 保存领养资料（补全），保存后申请领养自动带入 */
    async saveAdopterProfile() {
      if (!this.adopterForm.adopterName) {
        this.$message.warning('请填写申请人姓名');
        return;
      }
      this.adopterSaving = true;
      try {
        await updateAdopterProfile(this.adopterForm);
        this.$message.success('领养资料已保存，申请领养时将自动带入');
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.adopterSaving = false;
      }
    },
    // ---------- 商城：订单 ----------
    async loadShopOrders() {
      this.shopOrdersLoading = true;
      try {
        const data = await fetchShopOrders({ pageNum: 1, pageSize: 20, status: this.shopOrderStatus || undefined });
        this.shopOrders = data.list || [];
        this.shopOrdersTotal = data.total || 0;
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.shopOrdersLoading = false;
      }
    },
    async payShopOrder(o) {
      try {
        const paid = await shopPayOrder(o.id);
        this.$message.success(`支付成功！获得 ${paid.pointsEarned} 积分`);
        this.loadShopOrders();
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async cancelShopOrder(o) {
      try {
        await shopCancelOrder(o.id);
        this.$message.success('订单已取消，库存与优惠券已退回');
        this.loadShopOrders();
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    // ---------- 商城：优惠券与积分 ----------
    async loadCouponPanel() {
      try {
        const p = await shopPoints();
        this.shopPointsBalance = p.balance;
        this.shopPointsLogs = p.logs || [];
        this.shopCouponsList = (await shopCoupons()) || [];
        this.myCoupons = (await shopMyCoupons()) || [];
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async exchangeCoupon(c) {
      try {
        await shopExchangeCoupon(c.id);
        this.$message.success('兑换成功');
        this.loadCouponPanel();
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async loadSummary() {
      try {
        this.summary = (await mySummary()) || {};
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    /** 头像上传前校验：格式 + 大小（后端同样做魔数与 5MB 校验） */
    beforeAvatarUpload(file) {
      const okType = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type);
      const okSize = file.size <= 5 * 1024 * 1024;
      if (!okType) this.$message.warning('仅支持 jpg / png / webp 格式');
      else if (!okSize) this.$message.warning('图片不能超过 5MB');
      return okType && okSize;
    },
    /** 自定义上传：直传 MinIO（bizType=avatar），成功后回填头像并同步导航栏 */
    async uploadAvatarRequest({ file }) {
      this.avatarUploading = true;
      try {
        const res = await uploadFile(file, 'avatar');
        this.profileForm.avatar = res.url;
        const info = this.$store.state.user.userInfo;
        if (info) {
          this.$store.commit('user/SET_USER_INFO', { ...info, avatar: res.url });
        }
        this.$message.success('头像已上传，点击「保存资料」生效');
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.avatarUploading = false;
      }
    },
    async saveProfile() {
      this.profileSaving = true;
      try {
        await updateProfile({
          nickname: this.profileForm.nickname,
          avatar: this.profileForm.avatar,
          gender: this.profileForm.gender,
          city: this.profileForm.city
        });
        this.$message.success('资料已保存');
        const info = this.$store.state.user.userInfo;
        if (info) {
          this.$store.commit('user/SET_USER_INFO', {
            ...info,
            nickname: this.profileForm.nickname,
            avatar: this.profileForm.avatar
          });
        }
        this.loadProfile();
        this.loadSummary();
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.profileSaving = false;
      }
    },
    /** 发送绑定手机号的短信验证码 */
    async sendBindCode() {
      if (!/^1[3-9]\d{9}$/.test(this.bindForm.phone)) {
        this.$message.warning('请输入正确的 11 位手机号');
        return;
      }
      this.bindSending = true;
      try {
        await sendSmsCode({ phone: this.bindForm.phone, scene: 'bind' });
        this.$message.success('验证码已发送');
        this.bindCooldown = 60;
        this._bindTimer = setInterval(() => {
          this.bindCooldown -= 1;
          if (this.bindCooldown <= 0) clearInterval(this._bindTimer);
        }, 1000);
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.bindSending = false;
      }
    },
    /** 绑定/换绑手机号；目标手机号属于其他账号时后端会串联合并并换发 token */
    async submitBindPhone() {
      if (!/^1[3-9]\d{9}$/.test(this.bindForm.phone)) {
        this.$message.warning('请输入正确的 11 位手机号');
        return;
      }
      if (!/^\d{4,6}$/.test(this.bindForm.code)) {
        this.$message.warning('请输入短信验证码');
        return;
      }
      this.bindSubmitting = true;
      try {
        const vo = await smsBind({ phone: this.bindForm.phone, code: this.bindForm.code });
        if (vo && vo.token) {
          // 发生了账号串联合并：换发新 token 并整体刷新登录态
          this.$store.commit('user/SET_TOKEN', vo.token);
          this.$store.commit('user/SET_USER_INFO', vo);
          this.$message.success('绑定成功，账号已串联为同一身份');
        } else {
          this.$message.success('绑定成功');
        }
        this.bindEditing = false;
        this.bindForm.phone = '';
        this.bindForm.code = '';
        this.loadProfile();
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.bindSubmitting = false;
      }
    },
    async submitRealName() {
      this.$refs.realNameForm.validate(async (valid) => {
        if (!valid) return;
        this.realNameSaving = true;
        try {
          await realName({ realName: this.realNameForm.realName, idCardNo: this.realNameForm.idCardNo });
          this.$message.success('实名认证已提交');
          this.loadProfile();
        } catch (e) {
          /* 拦截器已提示 */
        } finally {
          this.realNameSaving = false;
        }
      });
    },
    async loadFavorites() {
      this.favoritesLoading = true;
      try {
        const data = await myFavorites(this.favoritesQuery);
        this.favorites = data.list || [];
        this.favoritesTotal = data.total || 0;
      } catch (e) {
        this.favorites = [];
        this.favoritesTotal = 0;
      } finally {
        this.favoritesLoading = false;
      }
    },
    async loadMyApplications() {
      this.applicationsLoading = true;
      try {
        const data = await myApplications({ pageNum: 1, pageSize: 5 });
        this.applications = data.list || [];
      } catch (e) {
        this.applications = [];
      } finally {
        this.applicationsLoading = false;
      }
    },
    async loadRecords() {
      this.recordsLoading = true;
      try {
        this.records = (await myRecords()) || [];
      } catch (e) {
        this.records = [];
      } finally {
        this.recordsLoading = false;
      }
    },
    async loadNotifications() {
      this.notificationsLoading = true;
      try {
        const data = await myNotifications(this.notificationsQuery);
        this.notifications = data.list || [];
        this.notificationsTotal = data.total || 0;
      } catch (e) {
        this.notifications = [];
        this.notificationsTotal = 0;
      } finally {
        this.notificationsLoading = false;
      }
    },
    async readOne(n) {
      if (n.isRead) return;
      try {
        await readNotification(n.id);
        n.isRead = 1;
        this.loadSummary();
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async readAll() {
      try {
        await readAllNotifications();
        this.$message.success('消息已全部标记为已读');
        this.loadNotifications();
        this.loadSummary();
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async loadAddresses() {
      this.addressLoading = true;
      try {
        this.addressList = (await addresses()) || [];
      } catch (e) {
        this.addressList = [];
      } finally {
        this.addressLoading = false;
      }
    },
    openAddressDialog(addr) {
      this.addressEditing = addr || null;
      this.addressForm = addr
        ? {
            receiver: addr.receiver,
            phone: addr.phone,
            province: addr.province,
            city: addr.city,
            district: addr.district,
            detail: addr.detail,
            isDefault: addr.isDefault === 1
          }
        : { receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false };
      this.addressVisible = true;
      this.$nextTick(() => {
        this.$refs.addressFormRef && this.$refs.addressFormRef.clearValidate();
      });
    },
    onAddressProvinceChange() {
      this.addressForm.city = '';
      this.addressForm.district = '';
    },
    onAddressCityChange() {
      this.addressForm.district = '';
    },
    saveAddressForm() {
      this.$refs.addressFormRef.validate(async (valid) => {
        if (!valid) return;
        this.addressSaving = true;
        const data = { ...this.addressForm, isDefault: this.addressForm.isDefault ? 1 : 0 };
        try {
          if (this.addressEditing) {
            await updateAddress(this.addressEditing.id, data);
            this.$message.success('地址已更新');
          } else {
            await saveAddress(data);
            this.$message.success('地址已新增');
          }
          this.addressVisible = false;
          this.loadAddresses();
        } catch (e) {
          /* 拦截器已提示 */
        } finally {
          this.addressSaving = false;
        }
      });
    },
    async removeAddress(addr) {
      try {
        await this.$confirm('删除后不可恢复，确定删除该地址吗？', '删除地址', {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning'
        });
      } catch (e) {
        return;
      }
      try {
        await deleteAddress(addr.id);
        this.$message.success('地址已删除');
        this.loadAddresses();
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async setDefaultAddress(addr) {
      try {
        await updateAddress(addr.id, {
          receiver: addr.receiver,
          phone: addr.phone,
          province: addr.province,
          city: addr.city,
          district: addr.district,
          detail: addr.detail,
          isDefault: 1
        });
        this.$message.success('已设为默认地址');
        this.loadAddresses();
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    // ---------- 联系机构（聊天） ----------
    enterChat() {
      this.loadSessions().then(() => {
        const wanted = this.$route.query.session;
        let target = null;
        if (wanted) {
          target = this.sessions.find((s) => String(s.shelterId) === String(wanted));
        }
        if (!target && this.sessions.length) {
          target = this.sessions[0];
        }
        if (target) {
          this.selectSession(target);
        }
      });
    },
    async loadSessions() {
      this.chatLoading = true;
      try {
        this.sessions = (await mySessions()) || [];
      } catch (e) {
        this.sessions = [];
      } finally {
        this.chatLoading = false;
      }
    },
    async selectSession(s) {
      this.currentSessionId = s.id;
      s.unread = 0;
      readSession(s.id).catch(() => {});
      await this.loadMessages();
    },
    /** 申请卡片消息解析（content 为 JSON 文本） */
    cardOf(m) {
      try {
        return JSON.parse(m.content);
      } catch (e) {
        return {};
      }
    },
    async loadMessages() {
      if (!this.currentSessionId) return;
      try {
        const data = await sessionMessages(this.currentSessionId, { pageNum: 1, pageSize: 50 });
        // 后端按时间倒序返回，渲染前反转为正序
        this.messages = (data.list || []).slice().reverse();
        this.$nextTick(() => {
          const box = this.$refs.msgBox;
          if (box) box.scrollTop = box.scrollHeight;
        });
      } catch (e) {
        /* 拦截器已提示 */
      }
    },
    async send() {
      const content = this.draft.trim();
      if (!content) {
        this.$message.warning('请输入消息内容');
        return;
      }
      if (!this.currentSessionId) {
        this.$message.warning('请先选择一个会话');
        return;
      }
      this.sending = true;
      try {
        await sendMessage(this.currentSessionId, { contentType: 'TEXT', content });
        this.draft = '';
        await this.loadMessages();
        this.loadSessions();
      } catch (e) {
        /* 拦截器已提示 */
      } finally {
        this.sending = false;
      }
    },
    /** WS 实时推送处理：新消息 / 撤回事件 */
    onChatPush(data) {
      if (data.type === 'CHAT_MESSAGE') {
        const msg = data.message;
        const inCurrentTab = this.activeTab === 'chat';
        const inSession = inCurrentTab && String(msg.sessionId) === String(this.currentSessionId);
        if (inSession) {
          // 发送方自己的消息由 HTTP 响应带回，这里只追加对方发来的
          if (!this.messages.some((m) => m.id === msg.id)) {
            this.messages.push(msg);
            this.$nextTick(() => {
              const box = this.$refs.msgBox;
              if (box) box.scrollTop = box.scrollHeight;
            });
            // 正在会话中，立刻标记已读
            readSession(this.currentSessionId).catch(() => {});
          }
        } else {
          // 不在当前会话：刷新会话列表与未读角标
          this.loadSessions().catch(() => {});
          chatUnreadTotal().then((n) => {
            if (this.summary) this.summary.unreadCount = n || 0;
          }).catch(() => {});
        }
      } else if (data.type === 'CHAT_RECALL') {
        if (String(data.sessionId) === String(this.currentSessionId)) {
          const target = this.messages.find((m) => m.id === data.messageId);
          if (target) {
            target.recalled = true;
            target.content = '';
            target.objectUrl = null;
          }
        }
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

.me-page {
  background: $bg-page;
  min-height: calc(100vh - #{$portal-header-h});
  padding: 32px 0 80px;
}

.me-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 24px;
  align-items: start;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
  }
}

// 左侧
.me-aside {
  @include card;

  padding: 20px;
  position: sticky;
  top: calc(#{$portal-header-h} + 24px);

  @media (max-width: 991px) {
    position: static;
  }
}

.me-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid $border-color;

  &__avatar {
    width: 48px;
    height: 48px;
    border-radius: $radius-circle;
    object-fit: cover;
    background: $bg-page;
    flex-shrink: 0;
  }

  &__info {
    min-width: 0;
  }

  &__name {
    font-size: 16px;
    line-height: 24px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__phone {
    margin-top: 2px;
    font-size: 12px;
    color: $text-secondary;
  }
}

.me-menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 12px;

  &__item {
    display: flex;
    align-items: center;
    gap: 10px;
    height: 40px;
    padding: 0 12px;
    border: none;
    border-radius: $radius-base;
    background: transparent;
    color: $text-regular;
    font-size: 14px;
    cursor: pointer;
    text-align: left;
    transition: all $duration-hover-in $ease-out-quart;

    i {
      font-size: 16px;
    }

    &:hover {
      background: $bg-page;
      color: $text-primary;
    }

    &.active {
      background: $color-primary-light;
      color: $color-primary-text;
      font-weight: 500;
    }
  }

  &__badge {
    margin-left: auto;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    border-radius: $radius-circle;
    background: $color-danger;
    color: $bg-card;
    font-size: 12px;
    line-height: 18px;
    text-align: center;
  }
}

// 右侧
.me-content {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.me-summary {
  @include card;

  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 8px 0;

  @media (max-width: 640px) {
    grid-template-columns: repeat(2, 1fr);
  }

  &__item {
    border: none;
    background: transparent;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 16px 8px;
    cursor: pointer;
    border-right: 1px solid $border-color;
    transition: background $duration-hover-in $ease-out-quart;

    &:last-child {
      border-right: none;
    }

    &:hover {
      background: $bg-page;
    }
  }

  &__num {
    font-size: 24px;
    line-height: 32px;
    font-weight: 600;
    color: $text-primary;
  }

  &__label {
    font-size: 13px;
    color: $text-secondary;
  }
}

.panel {
  @include card;

  padding: 24px;

  // 表单网格规整化：el-col 用 float 排列时，前后表单项高度差会引发「float 卡位」
  // 错行（如城市被挤到右列）。改用 grid 强制对齐，窄屏自动单列。
  // 注意：Element 给 .el-row 自带 ::before/::after（display:table 清浮动），
  // 在 grid 容器里它们会占据第一个网格单元，把首项挤到右列，必须关闭。
  .panel__form .el-row {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    column-gap: 24px;

    &::before,
    &::after {
      display: none;
    }
  }

  .panel__form .el-row > .el-col {
    float: none !important;
    width: 100% !important;
  }

  @media (max-width: 767px) {
    .panel__form .el-row {
      grid-template-columns: 1fr;
    }
  }

  &--chat {
    padding-bottom: 0;
  }

  &__head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 20px;

    h2 {
      font-size: 20px;
      line-height: 30px;
    }

    p {
      margin-top: 4px;
      font-size: 13px;
      color: $text-secondary;
    }
  }

  &__more {
    padding: 0;
  }

  &__tip {
    margin-top: 12px;
    font-size: 13px;
    color: $text-secondary;
  }
}

.avatar-block {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 4px 0 18px;

  &__upload {
    // el-upload 的默认行为会让内部元素错位，重置为普通块
    display: block;
    line-height: 1;
  }

  &__wrap {
    position: relative;
    width: 72px;
    height: 72px;
    cursor: pointer;
  }

  &__preview {
    width: 72px;
    height: 72px;
    border-radius: $radius-circle;
    object-fit: cover;
    background: $bg-page;
    display: block;
    border: 2px solid $border-color;
  }

  // 右下角相机角标（微信风格）
  &__cam {
    position: absolute;
    right: -2px;
    bottom: -2px;
    width: 26px;
    height: 26px;
    border-radius: 50%;
    background: $color-primary;
    border: 2px solid #FFFFFF;
    display: flex;
    align-items: center;
    justify-content: center;

    i {
      font-size: 13px;
      color: #FFFFFF;
    }
  }

  &__text {
    display: flex;
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  &__tip {
    margin: 0;
    font-size: 12px;
    color: $text-secondary;
  }
}

// 账号串联（账号密码 / 手机号 / 微信绑定）
.link-box {
  margin-top: 24px;
  padding: 20px;
  border: 1px solid $border-color;
  border-radius: $radius-base;
  background: #FFF9F5;

  &__head {
    h3 {
      font-size: 16px;
      color: $text-primary;
    }

    p {
      margin-top: 6px;
      font-size: 13px;
      line-height: 20px;
      color: $text-secondary;
    }
  }

  &__row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 24px;
    margin: 14px 0 16px;
  }

  .link-item {
    font-size: 13px;
    color: $text-regular;

    i {
      margin-right: 5px;
      color: #C0C4CC;
    }

    .ok {
      color: $color-secondary;
    }
  }

  &__form {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;

    .el-input {
      width: 190px;
    }

    .link-box__input--code,
    .el-input--code {
      width: 130px;
    }
  }

  &__tip {
    margin-top: 10px;
    font-size: 12px;
    color: $text-secondary;
  }
}

.shop-order {
  padding: 16px;
  border: 1px solid $border-color;
  border-radius: $radius-base;
  margin-bottom: 14px;

  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 10px;
    border-bottom: 1px solid $border-color;
  }

  &__no { font-size: 12px; color: $text-secondary; }

  &__item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 0;
  }

  &__img {
    width: 56px;
    height: 56px;
    border-radius: $radius-small;
    object-fit: cover;
    background: $bg-page;
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 14px;
    color: $text-primary;

    .num { font-size: 12px; color: $text-secondary; }
  }

  &__amounts {
    display: flex;
    justify-content: flex-end;
    gap: 14px;
    align-items: baseline;
    font-size: 13px;
    color: $text-regular;

    .shop-order__discount { color: #0F6F63; }
  }

  &__actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding-top: 8px;
  }
}

.points-hero {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 18px 20px;
  border-radius: $radius-base;
  background: linear-gradient(135deg, #FFF6EF, #FFFFFF);
  border: 1px solid #FFE0CE;

  span { font-size: 13px; color: $text-secondary; }
  strong { font-size: 28px; color: $color-primary-text; }
}

.points-section {
  margin: 20px 0 10px;
  font-size: 14px;
  color: $text-primary;
}

.coupon-line {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border: 1px solid $border-color;
  border-radius: $radius-base;
  margin-bottom: 10px;

  &.mine { background: $bg-page; }

  &__left {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-right: 12px;
    border-right: 1px dashed $border-color;

    strong { color: $color-primary-text; font-size: 16px; }
    span { font-size: 11px; color: $text-secondary; }
  }

  &__body {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;

    strong { font-size: 13px; color: $text-primary; }
    span { font-size: 12px; color: $text-secondary; }
  }
}

.points-log {
  display: flex;
  align-items: baseline;
  gap: 14px;
  padding: 8px 0;
  font-size: 13px;
  color: $text-regular;
  border-bottom: 1px dashed $border-color;

  &:first-of-type { border-top: 1px dashed $border-color; }

  .earn { color: #0F6F63; }

  span:first-of-type { flex: 1; }
}

.real-done {
  text-align: center;
  padding: 32px 0;

  i {
    font-size: 48px;
    color: $color-secondary;
  }

  h3 {
    margin-top: 12px;
    font-size: 16px;
  }

  p {
    margin-top: 8px;
    font-size: 14px;
    color: $text-regular;
  }
}

// 收藏
.fav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;

  @media (max-width: 1199px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

.fav-skeleton {
  @include card;
  padding: 16px;
}

.fav-card {
  @include card;
  @include card-hover;

  overflow: hidden;
  cursor: pointer;

  &:focus-visible {
    @include focus-ring;
  }

  &__media {
    aspect-ratio: 4 / 3;
    // 防止 height:100% 子图按自然比例撑破比例框（竖图会把卡片撑高）
    min-height: 0;
    background: $bg-page;
    @include img-zoom;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__body {
    padding: 12px 16px 16px;
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  &__name {
    font-size: 15px;
    font-weight: 600;
    color: $text-primary;
  }

  &__meta {
    margin-top: 4px;
    font-size: 13px;
    color: $text-secondary;
  }

  &__time {
    margin-top: 4px;
    font-size: 12px;
    color: $text-secondary;
  }
}

// 简版申请列表
.mini-list {
  margin: 0;
  padding: 0;
  list-style: none;

  &__item {
    display: grid;
    grid-template-columns: 48px minmax(0, 1fr) auto auto;
    align-items: center;
    gap: 12px;
    padding: 12px 8px;
    border-bottom: 1px solid $border-color;
    cursor: pointer;
    border-radius: $radius-base;
    transition: background $duration-hover-in $ease-out-quart;

    &:hover {
      background: $bg-page;
    }

    &:last-child {
      border-bottom: none;
    }
  }

  &__cover {
    width: 48px;
    height: 48px;
    border-radius: $radius-base;
    object-fit: cover;
    background: $bg-page;
    display: block;
  }

  &__name {
    font-size: 14px;
    color: $text-primary;
    font-weight: 500;
  }

  &__no {
    margin-top: 2px;
    font-size: 12px;
    color: $text-secondary;
  }

  &__arrow {
    color: $text-secondary;
  }
}

// 回访
.record-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-card {
  @include card;

  padding: 20px;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  &__pet {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;

    img {
      width: 56px;
      height: 56px;
      border-radius: $radius-base;
      object-fit: cover;
      background: $bg-page;
      flex-shrink: 0;
    }

    h3 {
      font-size: 15px;
    }

    p {
      margin-top: 2px;
      font-size: 12px;
      color: $text-secondary;
    }
  }

  &__followups {
    margin: 16px 0 0;
    padding: 0;
    list-style: none;
  }
}

.followup {
  padding: 12px 0;
  border-top: 1px solid $border-color;

  &__head {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  &__round {
    font-size: 14px;
    font-weight: 500;
    color: $text-primary;
  }

  &__plan {
    font-size: 13px;
    color: $text-secondary;
  }

  &__pending {
    margin-top: 8px;
    font-size: 13px;
    padding: 8px 12px;
    border-radius: $radius-small;
    background: $color-warning-light;
    color: $color-warning-text;
  }

  &__overdue {
    margin-top: 8px;
    font-size: 13px;
    padding: 8px 12px;
    border-radius: $radius-small;
    background: $color-danger-light;
    color: $color-danger-text;
  }

  &__done {
    margin-top: 8px;
    font-size: 13px;
    color: $text-regular;
  }
}

// 消息
.notif-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
}

.notif {
  display: flex;
  gap: 12px;
  padding: 14px 8px;
  border-bottom: 1px solid $border-color;
  cursor: pointer;
  border-radius: $radius-base;
  transition: background $duration-hover-in $ease-out-quart;

  &:hover {
    background: $bg-page;
  }

  &:last-child {
    border-bottom: none;
  }

  &__dot {
    width: 8px;
    height: 8px;
    margin-top: 8px;
    border-radius: $radius-circle;
    background: transparent;
    flex-shrink: 0;
  }

  &.unread {
    .notif__dot {
      background: $color-danger;
    }

    .notif__title {
      font-weight: 600;
    }
  }

  &__body {
    min-width: 0;
    flex: 1;
  }

  &__title {
    font-size: 14px;
    color: $text-primary;
  }

  &__content {
    margin-top: 4px;
    font-size: 13px;
    line-height: 22px;
    color: $text-regular;
    word-break: break-word;
  }

  &__time {
    margin-top: 4px;
    font-size: 12px;
    color: $text-secondary;
  }
}

// 地址
.addr-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.addr-card {
  @include card;

  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;

  @media (max-width: 640px) {
    flex-direction: column;
    align-items: flex-start;
  }

  &__body {
    min-width: 0;
  }

  &__line {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  &__receiver {
    font-size: 15px;
    font-weight: 600;
    color: $text-primary;
  }

  &__default {
    padding: 2px 8px;
    border-radius: $radius-small;
    background: $color-primary-light;
    color: $color-primary-text;
    font-size: 12px;
    line-height: 20px;
  }

  &__detail {
    margin-top: 6px;
    font-size: 14px;
    color: $text-regular;
  }

  &__actions {
    display: flex;
    gap: 4px;
    flex-shrink: 0;

    .el-button {
      padding: 0 8px;
    }
  }

  &__delete {
    color: $color-danger-text;

    &:hover {
      color: $color-danger;
    }
  }
}

// 聊天
.chat {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 20px;
  height: 480px;
  padding-bottom: 24px;

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
    height: auto;
  }
}

.chat__list {
  border-right: 1px solid $border-color;
  overflow-y: auto;
  padding-right: 8px;

  @media (max-width: 640px) {
    border-right: none;
    max-height: 180px;
    border-bottom: 1px solid $border-color;
    padding-bottom: 8px;
  }
}

.chat__session {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: none;
  border-radius: $radius-base;
  background: transparent;
  cursor: pointer;
  text-align: left;
  transition: background $duration-hover-in $ease-out-quart;

  &:hover {
    background: $bg-page;
  }

  &.active {
    background: $color-primary-light;
  }
}

.chat__avatar {
  width: 40px;
  height: 40px;
  border-radius: $radius-circle;
  object-fit: cover;
  background: $bg-page;
  flex-shrink: 0;
}

.chat__session-info {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chat__session-name {
  font-size: 14px;
  font-weight: 500;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat__session-last {
  font-size: 12px;
  color: $text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat__unread {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: $radius-circle;
  background: $color-danger;
  color: $bg-card;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
  flex-shrink: 0;
}

.chat__window {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat__messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 4px;
}

.chat__msg {
  max-width: 72%;
  display: flex;
  flex-direction: column;
  gap: 4px;

  &.mine {
    align-self: flex-end;
    align-items: flex-end;

    .chat__bubble {
      background: $color-primary-light;
      color: $color-primary-text;
    }
  }

  .chat__bubble {
    background: $bg-page;
    color: $text-regular;
    padding: 10px 14px;
    border-radius: $radius-base;
    font-size: 14px;
    line-height: 22px;
    word-break: break-word;
  }
}

.chat__img {
  max-width: 220px;
  max-height: 220px;
  border-radius: $radius-small;
  display: block;
}

.chat__time {
  font-size: 12px;
  color: $text-secondary;
}

.chat__composer {
  display: flex;
  gap: 12px;
  padding: 16px 4px 4px;
  border-top: 1px solid $border-color;

  .el-input {
    flex: 1;
  }
}

.pagination {
  margin-top: 24px;
  text-align: right;
}
</style>
