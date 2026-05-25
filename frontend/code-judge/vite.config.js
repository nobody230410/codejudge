import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import monacoEditorPluginPkg from 'vite-plugin-monaco-editor'

/** CJS 包在 ESM 下常为 `{ default: fn }` */
const monacoEditorPlugin =
  typeof monacoEditorPluginPkg === 'function'
    ? monacoEditorPluginPkg
    : monacoEditorPluginPkg.default

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    monacoEditorPlugin({}),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true,
      },
    },
  },
})
