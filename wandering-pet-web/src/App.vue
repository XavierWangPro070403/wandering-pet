<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',
  // Vue 挂载完成即资源加载结束，淡出移除 index.html 中的启动加载屏
  mounted() {
    if (typeof window.__removeBootLoader === 'function') {
      window.__removeBootLoader();
    }
  }
};
</script>
