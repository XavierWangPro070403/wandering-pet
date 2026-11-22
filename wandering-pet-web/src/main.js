import Vue from 'vue';
import ElementUI from 'element-ui';
// 主题：element-variables.scss 编译覆盖后的完整主题（无默认蓝）
import '@/styles/element-variables.scss';
import '@/styles/common.scss';
import App from './App.vue';
import router from './router';
import store from './store';
import permission from './directive/permission';
import { parseTime } from '@/utils/format';

Vue.use(ElementUI, { size: 'medium' });
Vue.use(permission);

Vue.config.productionTip = false;
Vue.prototype.$parseTime = parseTime;

new Vue({
  router,
  store,
  render: (h) => h(App)
}).$mount('#app');
