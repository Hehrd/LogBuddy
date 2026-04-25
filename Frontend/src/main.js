import { createApp } from 'vue'
import App from './App.vue'
import { isMockMode } from './config/runtime.js'
import { router } from './router.js'

async function bootstrap() {
  if (isMockMode) {
    const { worker } = await import('./mocks/browser.js')
    await worker.start({
      onUnhandledRequest: 'bypass',
      serviceWorker: {
        url: '/mockServiceWorker.js',
      },
    })
  }

  createApp(App).use(router).mount('#app')
}

bootstrap()
