import { createRouter, createWebHistory } from 'vue-router'
import AlertsPage from './pages/AlertsPage.vue'
import ConfigPage from './pages/ConfigPage.vue'
import ServiceInfoPage from './pages/ServiceInfoPage.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/config',
    },
    {
      path: '/config',
      component: ConfigPage,
    },
    {
      path: '/alerts',
      component: AlertsPage,
    },
    {
      path: '/info',
      redirect: '/info/data-processing',
    },
    {
      path: '/info/:serviceKey',
      component: ServiceInfoPage,
      props: true,
    },
  ],
})
