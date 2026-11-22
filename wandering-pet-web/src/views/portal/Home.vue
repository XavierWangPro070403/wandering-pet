<template>
  <div class="home">
    <!-- 第 1 屏：Hero（100svh，唯一一屏） -->
    <section class="hero" aria-label="首屏">
      <div class="hero__media" aria-hidden="true">
        <img src="/imgs/hero/hero-main.jpg" alt="志愿者牵着救助站的金毛犬散步" />
      </div>
      <div class="hero__scrim" aria-hidden="true"></div>
      <div class="wrap hero__content">
        <div class="hero__intro">
          <h1 class="hero__title">流浪猫狗领养，杭州与成都 2 家救助站</h1>
          <p class="hero__subtitle">每一只待领养的动物都已完成免疫与体检，领养后配套 4 次定期回访。</p>
          <div class="hero__actions">
            <el-button type="primary" size="large" @click="$router.push('/pets')">查看待领养</el-button>
            <el-button class="hero__ghost" size="large" @click="$router.push('/donation')">去捐赠中心</el-button>
          </div>
        </div>
        <dl class="hero__stats" aria-label="平台数据">
          <div class="hero__stat">
            <dt>在册待领养</dt>
            <dd class="num">{{ summary.availablePets || '—' }}<span>只</span></dd>
          </div>
          <div class="hero__stat">
            <dt>累计领养</dt>
            <dd class="num">{{ summary.adoptedPets || '—' }}<span>只</span></dd>
          </div>
          <div class="hero__stat">
            <dt>入驻救助站</dt>
            <dd class="num">{{ summary.shelterCount || '—' }}<span>家</span></dd>
          </div>
          <div class="hero__stat">
            <dt>覆盖城市</dt>
            <dd class="num">{{ summary.cityCount || '—' }}<span>座</span></dd>
          </div>
        </dl>
      </div>
    </section>

    <!-- 第 2 屏：精选待领养（#F7F8FA） -->
    <section class="section section--gray" aria-labelledby="sec-featured">
      <div class="wrap">
        <div class="section-head">
          <h2 id="sec-featured">精选待领养</h2>
          <p class="section-head__lead">由救助站推荐，健康档案与性格说明齐全。</p>
          <router-link to="/pets" class="link-underline section-head__more">查看全部待领养</router-link>
        </div>
        <div class="featured-grid">
          <PetCard v-for="pet in featured" :key="pet.id" :pet="pet" />
        </div>
        <div v-if="!featured.length" class="section-empty"><el-skeleton :rows="3" animated /></div>
      </div>
    </section>

    <!-- 第 3 屏：领养四步（白） -->
    <section class="section" aria-labelledby="sec-steps">
      <div class="wrap">
        <div class="section-head">
          <h2 id="sec-steps">领养流程</h2>
          <p class="section-head__lead">从提交申请到定期回访，全流程线上可查。</p>
        </div>
        <ol class="steps">
          <li v-for="(step, i) in steps" :key="i" class="step">
            <div class="step__media"><img :src="step.img" :alt="step.alt" loading="lazy" /></div>
            <span class="step__no num">{{ String(i + 1).padStart(2, '0') }}</span>
            <h3 class="step__title">{{ step.title }}</h3>
            <p class="step__desc">{{ step.desc }}</p>
          </li>
        </ol>
      </div>
    </section>

    <!-- 第 4-5 屏：救助故事（2 屏，#F7F8FA，记忆点：sticky 照片切换） -->
    <section class="section section--gray stories" aria-labelledby="sec-story">
      <div class="wrap stories__grid">
        <div class="stories__text">
          <h2 id="sec-story">救助故事</h2>
          <div class="story-block" v-for="(story, i) in stories" :key="i" :ref="`story-${i}`">
            <h3>{{ story.title }}</h3>
            <p>{{ story.text }}</p>
          </div>
          <router-link to="/articles?category=STORY" class="link-underline">阅读全部救助故事</router-link>
        </div>
        <div class="stories__photos" aria-hidden="true">
          <div class="stories__frame">
            <img
              v-for="(story, i) in stories"
              :key="i"
              :src="story.img"
              :alt="story.alt"
              :class="{ current: activeStory === i }"
              loading="lazy" />
          </div>
        </div>
      </div>
    </section>

    <!-- 第 6 屏：公益透明（捐赠 + 志愿，白） -->
    <section class="section" aria-labelledby="sec-transparent">
      <div class="wrap">
        <div class="section-head">
          <h2 id="sec-transparent">公益透明</h2>
          <p class="section-head__lead">捐赠明细与回访记录都在平台上，可逐笔查看。</p>
        </div>
        <div class="transparent-grid">
          <div class="donate-list">
            <router-link v-for="p in projects" :key="p.id" to="/donation" class="donate-card">
              <div class="donate-card__media"><img :src="p.cover" :alt="p.title" loading="lazy" /></div>
              <div class="donate-card__body">
                <h3>{{ p.title }}</h3>
                <p class="donate-card__shelter">{{ p.shelterName }}</p>
                <div class="donate-card__bar"><span :style="{ width: Math.min(p.progress, 100) + '%' }"></span></div>
                <p class="donate-card__meta num">已筹 ¥ {{ p.raisedAmount }} / 目标 ¥ {{ p.targetAmount }} · {{ p.donorCount }} 人捐赠</p>
              </div>
            </router-link>
            <EmptyState v-if="!projects.length && !loading" title="暂无进行中的募捐项目" action-text="浏览待领养" @action="$router.push('/pets')" />
          </div>
          <div class="volunteer-cta">
            <div class="volunteer-cta__media"><img src="/imgs/volunteer/volunteer.jpg" alt="志愿者陪救助站的狗狗玩耍" loading="lazy" /></div>
            <div class="volunteer-cta__body">
              <h3>报名志愿者</h3>
              <p>摄影、遛狗、协助送医，周末半天也可以帮上忙。</p>
              <el-button type="primary" @click="$router.push('/volunteer')">报名志愿者</el-button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 第 7 屏：资讯（#F7F8FA） -->
    <section class="section section--gray" aria-labelledby="sec-articles">
      <div class="wrap">
        <div class="section-head">
          <h2 id="sec-articles">资讯科普</h2>
          <p class="section-head__lead">饲养指导、领养知识与平台数据公示。</p>
          <router-link to="/articles" class="link-underline section-head__more">查看全部资讯</router-link>
        </div>
        <div class="article-grid">
          <router-link v-for="a in articles" :key="a.id" :to="`/articles/${a.id}`" class="article-card">
            <div class="article-card__media"><img :src="a.cover" :alt="`文章封面：${a.title}`" loading="lazy" /></div>
            <div class="article-card__body">
              <span class="article-card__category">{{ categoryLabel(a.category) }}</span>
              <h3>{{ a.title }}</h3>
              <p class="article-card__meta num">{{ a.publishTime && a.publishTime.slice(0, 10) }} · {{ a.viewCount }} 次阅读</p>
            </div>
          </router-link>
        </div>
        <div v-if="!articles.length" class="section-empty"><el-skeleton :rows="3" animated /></div>
      </div>
    </section>
  </div>
</template>

<script>
import PetCard from '@/components/PetCard.vue';
import EmptyState from '@/components/EmptyState.vue';
import { pagePets } from '@/api/pet';
import { ongoingProjects } from '@/api/donation';
import { pageArticles } from '@/api/article';
import { portalSummary } from '@/api/statistics';
import { articleCategoryText } from '@/utils/format';

export default {
  name: 'PortalHome',
  components: { PetCard, EmptyState },
  data() {
    return {
      loading: true,
      summary: {},
      featured: [],
      projects: [],
      articles: [],
      activeStory: 0,
      observer: null,
      steps: [
        { title: '挑选与收藏', desc: '按物种、体型、城市筛选，先收藏感兴趣的档案。', img: '/imgs/story/story-1.jpg', alt: '猫在救助站窗台休息' },
        { title: '提交申请', desc: '填写居住条件与养宠经验，救助站 3 个工作日内初筛。', img: '/imgs/story/story-2.jpg', alt: '救助站的狗狗等待面谈' },
        { title: '面谈与家访', desc: '到站见面，必要时安排家访确认居住环境。', img: '/imgs/story/story-3.jpg', alt: '志愿者与狗狗互动' },
        { title: '签约与回访', desc: '签署领养协议，第 7/30/90/180 天完成 4 次回访。', img: '/imgs/story/story-4.jpg', alt: '领养后的兔在新家' }
      ],
      stories: [
        {
          title: '高架桥下的团子',
          text: '它在文一西路高架下守着一只翻倒的垃圾桶过冬。救助站接回、接生、绝育，10 个月后在领养人家的飘窗上晒太阳。',
          img: '/imgs/story/story-1.jpg', alt: '狸花猫在窗台晒太阳'
        },
        {
          title: '42 天的麦兜',
          text: '后腿受伤的柯基被三位志愿者三班倒照看，从无法站立到追着网球跑，用了 42 天。',
          img: '/imgs/story/story-2.jpg', alt: '柯基在草地上奔跑'
        },
        {
          title: '重新排队的大壮',
          text: '工地撤场后被遗弃的金毛，皮肤病治愈后通过全部审核环节，现在每周准时出现在回访照片里。',
          img: '/imgs/story/story-3.jpg', alt: '金毛犬回到救助站探望'
        },
        {
          title: '不再是奖品的糯米',
          text: '校门口抽奖剩下的垂耳兔，被老师买下送来救助站。它现在等着一个做好电线防护的家。',
          img: '/imgs/story/story-4.jpg', alt: '白色垂耳兔在草垫上'
        }
      ]
    };
  },
  computed: {
    categoryLabel: () => articleCategoryText
  },
  async mounted() {
    this.loadSummary();
    this.loadFeatured();
    this.loadProjects();
    this.loadArticles();
    this.setupStoryObserver();
  },
  beforeDestroy() {
    if (this.observer) this.observer.disconnect();
  },
  methods: {
    async loadSummary() {
      try {
        this.summary = await portalSummary();
      } catch (e) { /* 拦截器已提示 */ }
    },
    async loadFeatured() {
      try {
        const data = await pagePets({ pageNum: 1, pageSize: 4, orderBy: 'favorite_count' });
        this.featured = data.list || [];
      } catch (e) { /* */ } finally {
        this.loading = false;
      }
    },
    async loadProjects() {
      try {
        const list = await ongoingProjects();
        this.projects = (list || []).slice(0, 2);
      } catch (e) { /* */ }
    },
    async loadArticles() {
      try {
        const data = await pageArticles({ pageNum: 1, pageSize: 3 });
        this.articles = data.list || [];
      } catch (e) { /* */ }
    },
    setupStoryObserver() {
      if (typeof IntersectionObserver === 'undefined') return;
      this.observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const idx = Number((entry.target.dataset.idx || '0'));
            this.activeStory = idx;
          }
        });
      }, { rootMargin: '-45% 0px -45% 0px' });
      this.$nextTick(() => {
        (this.$refs.storyTextBlocks || []);
        const blocks = this.$el.querySelectorAll('.story-block');
        blocks.forEach((el, i) => {
          el.dataset.idx = String(i);
          this.observer.observe(el);
        });
      });
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

// ---------------- Hero（恰一屏：顶栏 + 首屏 = 100svh） ----------------
.hero {
  position: relative;
  height: calc(100vh - #{$portal-header-h});
  height: calc(100svh - #{$portal-header-h});
  min-height: 560px;
  display: flex;
  align-items: stretch;
  overflow: hidden;

  &__media,
  &__scrim {
    position: absolute;
    inset: 0;
  }

  &__media img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: center 30%;
    // 首屏背景只做轻微推镜，不淡出（避免首秒整屏发灰）
    animation: heroMediaIn 1.6s $ease-out-quart both;
  }

  &__scrim {
    background: linear-gradient(180deg, rgba(31, 39, 51, 0.62) 0%, rgba(31, 39, 51, 0.42) 42%, rgba(31, 39, 51, 0.64) 100%);
  }

  // 上区（主张）垂直居中，下区（数据带）贴底
  &__content {
    position: relative;
    display: grid;
    // 单列收在容器内（否则 auto 列会被长标题撑出屏幕）
    grid-template-columns: minmax(0, 1fr);
    grid-template-rows: 1fr auto;
    color: #fff;
  }

  &__intro {
    align-self: center;
    justify-self: center;
    width: 100%;
    max-width: 900px;
    padding: 32px 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  &__title {
    font-size: clamp(38px, 5.4vw, #{$font-hero});
    line-height: 1.14;
    letter-spacing: -0.015em;
    color: #fff;
    max-width: 16em;
    text-wrap: balance;
    text-shadow: 0 2px 24px rgba(31, 39, 51, 0.28);
    animation: heroIn 900ms $ease-out-quart both;
  }

  &__subtitle {
    margin-top: 24px;
    font-size: clamp(17px, 1.25vw, #{$font-hero-lead});
    line-height: 1.75;
    color: rgba(255, 255, 255, 0.93);
    max-width: 26em;
    text-shadow: 0 1px 16px rgba(31, 39, 51, 0.25);
    animation: heroIn 900ms $ease-out-quart 140ms both;
  }

  &__actions {
    margin-top: 40px;
    display: flex;
    justify-content: center;
    gap: 16px;
    animation: heroIn 900ms $ease-out-quart 280ms both;
  }

  &__ghost {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.85);
    color: #fff;
    backdrop-filter: blur(2px);

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      border-color: #fff;
      color: #fff;
    }
  }

  &__stats {
    margin: 0 0 28px;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
    padding-top: 24px;
    border-top: 1px solid rgba(255, 255, 255, 0.35);
    animation: heroIn 900ms $ease-out-quart 420ms both;
  }

  &__stat {
    dt {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.85);
    }

    dd {
      margin: 4px 0 0;
      font-size: 34px;
      line-height: 42px;
      font-weight: 600;
      color: #fff;
      font-family: $font-family-number;
      @include tabular-nums;

      span {
        font-size: 14px;
        font-weight: 400;
        margin-left: 4px;
        color: rgba(255, 255, 255, 0.85);
      }
    }
  }

  // 首屏内容底部距视口 ≥ 24px
  @media (max-width: 767px) {
    min-height: 480px;

    &__intro {
      padding: 24px 0;
    }

    &__title {
      font-size: clamp(30px, 8.4vw, 40px);
    }

    &__subtitle {
      margin-top: 16px;
    }

    &__actions {
      margin-top: 28px;
      width: 100%;
      flex-direction: column;

      .el-button {
        width: 100%;
        margin: 0;
      }
    }

    &__stats {
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
    }
  }
}

@keyframes heroIn {
  from { opacity: 0; transform: translateY(28px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes heroMediaIn {
  from { transform: scale(1.05); }
  to { transform: scale(1); }
}

@media (prefers-reduced-motion: reduce) {
  .hero__media img,
  .hero__title,
  .hero__subtitle,
  .hero__actions,
  .hero__stats {
    animation: none;
  }
}

// ---------------- 章节通用 ----------------
.section {
  background: $bg-card;

  &--gray {
    background: $bg-page;
  }

  > .wrap {
    padding-top: 120px;
    padding-bottom: 120px;

    @media (max-width: 767px) {
      padding-top: 72px;
      padding-bottom: 72px;
    }
  }
}

.section-head {
  display: flex;
  align-items: baseline;
  gap: 24px;
  margin-bottom: 40px;
  flex-wrap: wrap;

  h2 {
    font-size: $font-h2;
    line-height: $lh-h2;
  }

  &__lead {
    font-size: 16px;
    line-height: 28px;
    color: $text-secondary;
    flex: 1;
    min-width: 220px;
  }

  &__more {
    font-size: 14px;
  }
}

.section-empty {
  padding: 24px 0;
}

// ---------------- 精选 4 卡 ----------------
.featured-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;

  @media (max-width: 1199px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 767px) { grid-template-columns: 1fr; }
}

// ---------------- 领养四步 ----------------
.steps {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  counter-reset: step;

  @media (max-width: 991px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 575px) { grid-template-columns: 1fr; }
}

.step {
  @include card;
  @include card-hover;
  @include img-zoom;

  overflow: hidden;

  &__media {
    aspect-ratio: 4 / 3;
    // 防止 height:100% 子图按自然比例撑破比例框（竖图会把卡片撑高）
    min-height: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__no {
    display: block;
    margin: 20px 24px 0;
    font-size: 13px;
    color: $color-primary-text;
    font-weight: 600;
  }

  &__title {
    margin: 4px 24px 0;
    font-size: 18px;
    line-height: 26px;
  }

  &__desc {
    margin: 8px 24px 24px;
    font-size: 14px;
    line-height: 22px;
    color: $text-regular;
  }
}

// ---------------- 救助故事（sticky 照片记忆点） ----------------
.stories__grid {
  display: grid;
  grid-template-columns: minmax(0, 5fr) minmax(0, 6fr);
  gap: 64px;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
  }
}

.stories__text {
  h2 {
    font-size: $font-h2;
    line-height: $lh-h2;
    margin-bottom: 32px;
  }

  .story-block {
    padding: 32px 0;
    border-top: 1px solid $border-color;

    h3 {
      font-size: 20px;
      line-height: 28px;
      margin-bottom: 8px;
      color: $text-secondary;
      transition: color $duration-hover-in $ease-out-quart;
    }

    p {
      font-size: 16px;
      line-height: 28px;
      color: $text-regular;
    }

    &.is-active h3,
    &:hover h3 {
      color: $color-primary-text;
    }
  }

  > .link-underline {
    display: inline-block;
    margin-top: 8px;
    font-size: 14px;
  }
}

.stories__photos {
  position: sticky;
  top: calc(#{$portal-header-h} + 32px);
  align-self: start;
}

.stories__frame {
  position: relative;
  aspect-ratio: 3 / 2;
  border-radius: $radius-card;
  overflow: hidden;
  box-shadow: $shadow-1;
  background: $bg-page;

  img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    opacity: 0;
    transform: scale(1.02);
    transition: opacity 600ms $ease-out-quart, transform 600ms $ease-out-quart;

    &.current {
      opacity: 1;
      transform: scale(1);
    }
  }
}

@media (prefers-reduced-motion: reduce) {
  .stories__frame img {
    transition: none;
  }
}

// ---------------- 公益透明 ----------------
.transparent-grid {
  display: grid;
  grid-template-columns: minmax(0, 7fr) minmax(0, 5fr);
  gap: 24px;
  align-items: stretch;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
  }
}

.donate-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;

  @media (max-width: 575px) {
    grid-template-columns: 1fr;
  }
}

.donate-card {
  @include card;
  @include card-hover;
  @include img-zoom;

  display: flex;
  flex-direction: column;
  overflow: hidden;

  &__media {
    aspect-ratio: 4 / 3;
    // 防止 height:100% 子图按自然比例撑破比例框（竖图会把卡片撑高）
    min-height: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__body {
    padding: 20px;
  }

  h3 {
    font-size: 18px;
    line-height: 26px;
  }

  &__shelter {
    margin-top: 4px;
    font-size: 13px;
    color: $text-secondary;
  }

  &__bar {
    margin-top: 16px;
    height: 6px;
    border-radius: 3px;
    background: $bg-page;
    overflow: hidden;

    span {
      display: block;
      height: 100%;
      border-radius: 3px;
      background: $color-primary;
      transition: width 600ms $ease-out-quart;
    }
  }

  &__meta {
    margin-top: 8px;
    font-size: 13px;
    color: $text-regular;
    @include tabular-nums;
  }
}

.volunteer-cta {
  @include card;

  display: flex;
  flex-direction: column;
  overflow: hidden;

  &__media {
    flex: 1;
    min-height: 220px;
    // 图片绝对定位填充：高度跟随卡片（与相邻卡片对齐），不被图片自然高度撑开
    position: relative;

    img {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__body {
    padding: 24px;

    h3 {
      font-size: 20px;
      line-height: 28px;
    }

    p {
      margin: 8px 0 16px;
      font-size: 14px;
      line-height: 22px;
      color: $text-regular;
    }
  }
}

// ---------------- 资讯 ----------------
.article-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;

  @media (max-width: 991px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 575px) { grid-template-columns: 1fr; }
}

.article-card {
  @include card;
  @include card-hover;
  @include img-zoom;

  display: flex;
  flex-direction: column;
  overflow: hidden;

  &__media {
    aspect-ratio: 4 / 3;
    // 防止 height:100% 子图按自然比例撑破比例框（竖图会把卡片撑高）
    min-height: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__body {
    padding: 20px;
  }

  &__category {
    font-size: 12px;
    color: $color-primary-text;
    background: $color-primary-light;
    border-radius: $radius-small;
    padding: 2px 8px;
  }

  h3 {
    margin-top: 8px;
    font-size: 17px;
    line-height: 25px;
  }

  &__meta {
    margin-top: 12px;
    font-size: 13px;
    color: $text-secondary;
    @include tabular-nums;
  }
}
</style>
