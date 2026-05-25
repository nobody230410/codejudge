import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import LoginView from '@/views/LoginView.vue'
import AdminLoginView from '@/views/AdminLoginView.vue'
import ProblemListView from '@/views/ProblemListView.vue'
import AdminProblemListView from '@/views/AdminProblemListView.vue'
import AdminProblemCreateView from '@/views/AdminProblemCreateView.vue'
import AdminUserListView from '@/views/AdminUserListView.vue'
import AdminSubmissionListView from '@/views/AdminSubmissionListView.vue'
import AdminSubmissionDetailView from '@/views/AdminSubmissionDetailView.vue'
import UserProfileView from '@/views/UserProfileView.vue'
import ProblemDetailView from '@/views/ProblemDetailView.vue'
import RunCodeView from '@/views/RunCodeView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'root-redirect',
          redirect: () => {
            const auth = useAuthStore()
            return auth.isAdmin ? { name: 'admin-problems' } : { name: 'problems' }
          },
        },
        {
          path: 'problems',
          name: 'problems',
          component: ProblemListView,
        },
        {
          path: 'account',
          name: 'user-account',
          component: UserProfileView,
        },
        {
          path: 'run',
          name: 'code-run',
          component: RunCodeView,
        },
        {
          path: 'admin/problems/new',
          name: 'admin-problem-create',
          component: AdminProblemCreateView,
          meta: { requiresAdmin: true },
        },
        {
          path: 'admin/problems',
          name: 'admin-problems',
          component: AdminProblemListView,
          meta: { requiresAdmin: true },
        },
        {
          path: 'admin/users',
          name: 'admin-users',
          component: AdminUserListView,
          meta: { requiresAdmin: true },
        },
        {
          path: 'admin/submissions',
          name: 'admin-submissions',
          component: AdminSubmissionListView,
          meta: { requiresAdmin: true },
        },
        {
          path: 'admin/submissions/:submissionId',
          name: 'admin-submission-detail',
          component: AdminSubmissionDetailView,
          meta: { requiresAdmin: true },
        },
        {
          path: 'problems/:problemId',
          name: 'problem-detail',
          component: ProblemDetailView,
        },
      ],
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guestOnly: true },
    },
    {
      path: '/login/admin',
      name: 'login-admin',
      component: AdminLoginView,
      meta: { guestOnly: true },
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)
  const guestOnly = to.matched.some((r) => r.meta.guestOnly)
  const requiresAdmin = to.matched.some((r) => r.meta.requiresAdmin)

  if (requiresAuth && !authStore.isAuthenticated) {
    const needsAdminLogin = to.matched.some((r) => r.meta.requiresAdmin)
    return {
      name: needsAdminLogin ? 'login-admin' : 'login',
      query: { redirect: to.fullPath },
    }
  }
  if (requiresAdmin && !authStore.isAdmin) {
    return { name: 'problems' }
  }
  if (guestOnly && authStore.isAuthenticated) {
    return authStore.isAdmin ? { name: 'admin-problems' } : { name: 'problems' }
  }
  return true
})

export default router
