<template>
  <div class="dashboard">
    <!-- 页面头 -->
    <div class="page-head">
      <div>
        <h1>数据看板</h1>
        <p class="page-head__desc">平台宠物档案、领养与用户运营的整体数据。</p>
      </div>
      <el-button type="primary" :loading="loading" @click="load">刷新数据</el-button>
    </div>

    <!-- 顶部 6 个总量统计卡 -->
    <div v-if="loading && !statCards.length" class="stat-row">
      <div v-for="i in 6" :key="i" class="stat-card stat-card--skeleton">
        <el-skeleton :rows="2" animated />
      </div>
    </div>
    <div v-else class="stat-row">
      <div v-for="card in statCards" :key="card.key" class="stat-card">
        <span class="stat-card__icon" aria-hidden="true"><i :class="card.icon"></i></span>
        <div class="stat-card__body">
          <p class="stat-card__value num">{{ card.value == null ? '—' : card.value }}</p>
          <p class="stat-card__label">{{ card.label }}</p>
          <p v-if="card.extra" class="stat-card__extra">{{ card.extra }}</p>
        </div>
      </div>
    </div>

    <!-- 加载失败：重试 -->
    <div v-if="error && !loading" class="error-block">
      <EmptyState title="看板数据加载失败" desc="网络开小差了，请稍后重试。" action-text="重新加载" @action="retry" />
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <div class="chart-card">
        <div class="chart-card__head">
          <h2 class="chart-card__title">领养趋势</h2>
          <span class="chart-card__sub">近 6 个月领养完成数量</span>
        </div>
        <div v-if="trendData.length" ref="trendChart" class="chart-box"></div>
        <EmptyState v-else-if="!loading" title="暂无数据" />
        <div v-else class="chart-box chart-box--skeleton"><el-skeleton :rows="5" animated /></div>
        <p class="chart-card__note" aria-live="polite">{{ trendNote }}</p>
      </div>

      <div class="chart-card">
        <div class="chart-card__head">
          <h2 class="chart-card__title">物种占比</h2>
          <span class="chart-card__sub">在册宠物（不含已下架）</span>
        </div>
        <div v-if="categoryData.length" ref="categoryChart" class="chart-box"></div>
        <EmptyState v-else-if="!loading" title="暂无数据" />
        <div v-else class="chart-box chart-box--skeleton"><el-skeleton :rows="5" animated /></div>
        <p class="chart-card__note" aria-live="polite">{{ categoryNote }}</p>
      </div>

      <div class="chart-card">
        <div class="chart-card__head">
          <h2 class="chart-card__title">城市分布</h2>
          <span class="chart-card__sub">待领养宠物所在城市</span>
        </div>
        <div v-if="cityData.length" ref="cityChart" class="chart-box"></div>
        <EmptyState v-else-if="!loading" title="暂无数据" />
        <div v-else class="chart-box chart-box--skeleton"><el-skeleton :rows="5" animated /></div>
        <p class="chart-card__note" aria-live="polite">{{ cityNote }}</p>
      </div>

      <div class="chart-card">
        <div class="chart-card__head">
          <h2 class="chart-card__title">领养转化</h2>
          <span class="chart-card__sub">申请从提交到完成的漏斗</span>
        </div>
        <div v-if="funnelData.length" ref="funnelChart" class="chart-box"></div>
        <EmptyState v-else-if="!loading" title="暂无数据" />
        <div v-else class="chart-box chart-box--skeleton"><el-skeleton :rows="5" animated /></div>
        <p class="chart-card__note" aria-live="polite">{{ funnelNote }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { adminDashboard } from '@/api/statistics';
import EmptyState from '@/components/EmptyState.vue';

// 图表仅用三色（规范限制），轴线/文字色与 tokens.scss 中性色保持一致
const CHART_COLORS = ['#FF7A45', '#2BAE9B', '#C9CDD4'];
const CHART_TEXT = '#4E5969';
const CHART_AXIS = '#E5E6EB';
const CHART_SPLIT = '#E5E6EB';

/** 总量卡元信息：key 对应后端 StatPointVO.name，extra 由接口下发 */
const TOTAL_META = {
  petTotal: { label: '宠物档案', icon: 'el-icon-notebook-2' },
  adoptedPets: { label: '累计领养', icon: 'el-icon-medal' },
  shelterCount: { label: '入驻机构', icon: 'el-icon-office-building' },
  userCount: { label: '注册用户', icon: 'el-icon-user' },
  applicationCount: { label: '领养申请', icon: 'el-icon-document-copy' },
  articleCount: { label: '发布文章', icon: 'el-icon-document' }
};

/**
 * 管理端数据看板（/admin/dashboard）
 */
export default {
  name: 'AdminDashboard',
  components: { EmptyState },
  data() {
    return {
      loading: true,
      error: false,
      totals: [],
      trendData: [],
      categoryData: [],
      cityData: [],
      funnelData: [],
      charts: {}
    };
  },
  computed: {
    statCards() {
      return this.totals.map((item) => {
        const meta = TOTAL_META[item.name] || {};
        return {
          key: item.name,
          value: item.value,
          label: meta.label || item.extra || item.name,
          extra: meta.label ? item.extra : '',
          icon: meta.icon || 'el-icon-data-line'
        };
      });
    },
    trendNote() {
      if (!this.trendData.length) return '暂无数据';
      const sum = this.trendData.reduce((acc, item) => acc + (item.value || 0), 0);
      return `最近 6 个月共领养 ${sum} 只`;
    },
    categoryNote() {
      if (!this.categoryData.length) return '暂无数据';
      const total = this.categoryData.reduce((acc, item) => acc + (item.value || 0), 0);
      const top = this.categoryData[0];
      const pct = total ? Math.round(((top.value || 0) / total) * 100) : 0;
      return `在册宠物 ${total} 只，「${top.name}」最多，占 ${pct}%`;
    },
    cityNote() {
      if (!this.cityData.length) return '暂无数据';
      const top = this.cityData[0];
      return `待领养宠物集中在「${top.name}」，共 ${top.value} 只`;
    },
    funnelNote() {
      if (!this.funnelData.length) return '暂无数据';
      const submitted = this.funnelData[0].value || 0;
      const done = this.funnelData[this.funnelData.length - 1].value || 0;
      const rate = submitted ? Math.round((done / submitted) * 100) : 0;
      return `共收到 ${submitted} 份申请，完成领养 ${done} 份，转化率 ${rate}%`;
    }
  },
  mounted() {
    this.load();
    window.addEventListener('resize', this.resizeCharts);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts);
    Object.keys(this.charts).forEach((key) => {
      if (this.charts[key]) this.charts[key].dispose();
    });
    this.charts = {};
  },
  methods: {
    async load() {
      this.loading = true;
      this.error = false;
      try {
        const data = await adminDashboard();
        // 后端 Long 统一序列化为字符串，图表与聚合计算前统一转数值
        const toNum = (list) => (list || []).map((it) => ({ ...it, value: Number(it.value) || 0 }));
        this.totals = toNum((data && data.totals) || []);
        this.trendData = toNum(data && data.adoptionTrend);
        this.categoryData = toNum(data && data.categoryDistribution);
        this.cityData = toNum(data && data.cityDistribution);
        this.funnelData = toNum(data && data.funnel);
        this.$nextTick(this.renderCharts);
      } catch (e) {
        this.error = true;
      } finally {
        this.loading = false;
      }
    },
    retry() {
      this.load();
    },
    resizeCharts() {
      Object.keys(this.charts).forEach((key) => {
        if (this.charts[key]) this.charts[key].resize();
      });
    },
    getChart(key, refName) {
      const el = this.$refs[refName];
      if (!el) return null;
      if (!this.charts[key]) this.charts[key] = echarts.init(el);
      return this.charts[key];
    },
    renderCharts() {
      this.renderTrend();
      this.renderCategory();
      this.renderCity();
      this.renderFunnel();
    },
    renderTrend() {
      const chart = this.getChart('trend', 'trendChart');
      if (!chart) return;
      chart.setOption({
        color: CHART_COLORS,
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 24, top: 24, bottom: 28 },
        xAxis: {
          type: 'category',
          data: this.trendData.map((item) => this.monthLabel(item.name)),
          axisLine: { lineStyle: { color: CHART_AXIS } },
          axisLabel: { color: CHART_TEXT },
          axisTick: { show: false }
        },
        yAxis: {
          type: 'value',
          minInterval: 1,
          axisLabel: { color: CHART_TEXT },
          splitLine: { lineStyle: { color: CHART_SPLIT } }
        },
        series: [
          {
            name: '领养完成',
            type: 'line',
            smooth: true,
            symbolSize: 7,
            data: this.trendData.map((item) => item.value || 0),
            lineStyle: { color: '#FF7A45', width: 2.5 },
            itemStyle: { color: '#FF7A45' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(255, 122, 69, 0.22)' },
                { offset: 1, color: 'rgba(255, 122, 69, 0)' }
              ])
            }
          }
        ]
      });
    },
    renderCategory() {
      const chart = this.getChart('category', 'categoryChart');
      if (!chart) return;
      chart.setOption({
        color: CHART_COLORS,
        tooltip: { trigger: 'item', formatter: '{b}：{c} 只（{d}%）' },
        legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: CHART_TEXT } },
        series: [
          {
            name: '物种占比',
            type: 'pie',
            radius: ['48%', '70%'],
            center: ['50%', '44%'],
            avoidLabelOverlap: true,
            label: { color: CHART_TEXT, formatter: '{d}%' },
            labelLine: { lineStyle: { color: CHART_AXIS } },
            data: this.categoryData.map((item, index) => ({
              name: item.name,
              value: item.value || 0,
              itemStyle: { color: CHART_COLORS[index % CHART_COLORS.length] }
            }))
          }
        ]
      });
    },
    renderCity() {
      const chart = this.getChart('city', 'cityChart');
      if (!chart) return;
      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: 40, right: 24, top: 24, bottom: 28 },
        xAxis: {
          type: 'category',
          data: this.cityData.map((item) => item.name),
          axisLine: { lineStyle: { color: CHART_AXIS } },
          axisLabel: { color: CHART_TEXT },
          axisTick: { show: false }
        },
        yAxis: {
          type: 'value',
          minInterval: 1,
          axisLabel: { color: CHART_TEXT },
          splitLine: { lineStyle: { color: CHART_SPLIT } }
        },
        series: [
          {
            name: '待领养',
            type: 'bar',
            barWidth: 28,
            data: this.cityData.map((item) => item.value || 0),
            itemStyle: { color: '#FF7A45', borderRadius: [4, 4, 0, 0] }
          }
        ]
      });
    },
    renderFunnel() {
      const chart = this.getChart('funnel', 'funnelChart');
      if (!chart) return;
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}：{c} 份' },
        series: [
          {
            name: '领养转化',
            type: 'funnel',
            left: '12%',
            width: '76%',
            top: 12,
            bottom: 12,
            minSize: '28%',
            gap: 4,
            label: { color: '#1F2733', formatter: '{b}  {c}' },
            data: this.funnelData.map((item, index) => {
              const alpha = 1 - index * (0.6 / Math.max(this.funnelData.length - 1, 1));
              return {
                name: item.name,
                value: item.value || 0,
                itemStyle: { color: `rgba(255, 122, 69, ${alpha.toFixed(2)})`, borderColor: '#FFFFFF' }
              };
            })
          }
        ]
      });
    },
    /** '2026-06' → '6月' */
    monthLabel(name) {
      const parts = String(name || '').split('-');
      return parts.length === 2 ? `${Number(parts[1])}月` : name;
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

.stat-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: $space-4;
  margin-bottom: $space-6;

  @media (max-width: 1366px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 640px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  @include card;
  display: flex;
  align-items: flex-start;
  gap: $space-3;
  padding: $space-5;

  &__icon {
    width: 40px;
    height: 40px;
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-base;
    background: $color-primary-light;
    color: $color-primary-text;
    font-size: 18px;
  }

  &__body {
    min-width: 0;
  }

  &__value {
    font-size: $font-w-h1;
    line-height: $lh-w-h1;
    font-weight: 600;
    color: $text-primary;
  }

  &__label {
    margin-top: 2px;
    font-size: $font-w-body;
    color: $text-regular;
  }

  &__extra {
    margin-top: 2px;
    font-size: $font-w-caption;
    line-height: $lh-w-caption;
    color: $text-secondary;
  }

  &--skeleton {
    min-height: 104px;
  }
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-4;

  @media (max-width: 991px) {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  @include card;
  padding: $space-5;

  &__head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: $space-3;
  }

  &__title {
    font-size: $font-w-h4;
    line-height: $lh-w-h4;
    color: $text-primary;
  }

  &__sub {
    font-size: $font-w-caption;
    color: $text-secondary;
  }

  &__note {
    margin-top: $space-3;
    padding-top: $space-3;
    border-top: 1px solid $border-color;
    font-size: $font-w-body;
    color: $text-regular;
  }
}

.chart-box {
  height: 280px;
  margin-top: $space-4;

  &--skeleton {
    display: flex;
    align-items: center;
    padding: 0 $space-4;
  }
}

.error-block {
  @include card;
  margin-bottom: $space-6;
}
</style>
