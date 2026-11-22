/**
 * 管理端路由：7 页（仅 ADMIN）
 */
const adminRoutes = [
  {
    path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue'),
    meta: { title: '数据看板', requiresAuth: true, roles: ['ADMIN'], perm: 'admin:dashboard' }
  },
  {
    path: 'shelters', name: 'AdminShelters', component: () => import('@/views/admin/Shelters.vue'),
    meta: { title: '机构审核', requiresAuth: true, roles: ['ADMIN'], perm: 'admin:shelter' }
  },
  {
    path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/Users.vue'),
    meta: { title: '用户管理', requiresAuth: true, roles: ['ADMIN'], perm: 'admin:user' }
  },
  {
    path: 'pets', name: 'AdminPets', component: () => import('@/views/admin/Pets.vue'),
    meta: { title: '宠物全量管理', requiresAuth: true, roles: ['ADMIN'], perm: 'admin:pet' }
  },
  {
    path: 'articles', name: 'AdminArticles', component: () => import('@/views/admin/Articles.vue'),
    meta: { title: '内容管理', requiresAuth: true, roles: ['ADMIN'], perm: 'admin:content' }
  },
  {
    path: 'rescues', name: 'AdminRescues', component: () => import('@/views/admin/Rescues.vue'),
    meta: { title: '线索管理', requiresAuth: true, roles: ['ADMIN'], perm: 'admin:rescue' }
  },
  {
    path: 'settings', name: 'AdminSettings', component: () => import('@/views/admin/Settings.vue'),
    meta: { title: '系统设置', requiresAuth: true, roles: ['ADMIN'], perm: 'admin:setting' }
  }
];

export default adminRoutes;
