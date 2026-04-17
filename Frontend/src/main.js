import { createApp } from 'vue'
import App from './App.vue'
import { installMockBackend } from './mocks/mockBackend.js'
import { router } from './router.js'

if (import.meta.env.VITE_ALERTS_MODE !== 'real') {
  installMockBackend()
}

createApp(App).use(router).mount('#app')
