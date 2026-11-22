const { defineConfig } = require('@vue/cli-service');

module.exports = defineConfig({
  transpileDependencies: false,
  productionSourceMap: false,
  lintOnSave: false,
  devServer: {
    port: 8081,
    open: false,
    proxy: {
      // 后端接口与演示图片统一代理到 8080
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/imgs': { target: 'http://localhost:8080', changeOrigin: true },
      // 聊天 WebSocket（开发环境代理到后端，ws: true 启用 WS 透传）
      '/ws': { target: 'ws://localhost:8080', ws: true, changeOrigin: true }
    }
  },
  css: {
    loaderOptions: {
      scss: {
        // 纯变量文件注入所有组件（无 CSS 输出）；Element 主题只在 element-variables.scss 编译一次
        additionalData: '@import "~@/styles/variables.scss";',
        sassOptions: {
          quietDeps: true
        }
      }
    }
  }
});
