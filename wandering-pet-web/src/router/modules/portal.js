/**
 * 用户端（门户）路由：12 页
 */
const portalRoutes = [
  { path: '', name: 'Home', component: () => import('@/views/portal/Home.vue'), meta: { title: '首页' } },
  { path: 'pets', name: 'PetList', component: () => import('@/views/portal/PetList.vue'), meta: { title: '待领养' } },
  { path: 'pets/:id', name: 'PetDetail', component: () => import('@/views/portal/PetDetail.vue'), meta: { title: '宠物档案' } },
  { path: 'shop', name: 'Shop', component: () => import('@/views/portal/Shop.vue'), meta: { title: '宠物商城' } },
  {
    path: 'adopt/apply/:petId', name: 'AdoptApply', component: () => import('@/views/portal/AdoptApply.vue'),
    meta: { title: '领养申请', requiresAuth: true, roles: ['USER'] }
  },
  {
    path: 'my/applications', name: 'MyApplications', component: () => import('@/views/portal/MyApplications.vue'),
    meta: { title: '我的申请', requiresAuth: true, roles: ['USER'] }
  },
  {
    path: 'my/applications/:id', name: 'ApplicationProgress', component: () => import('@/views/portal/ApplicationProgress.vue'),
    meta: { title: '领养进度', requiresAuth: true, roles: ['USER'] }
  },
  { path: 'articles', name: 'ArticleList', component: () => import('@/views/portal/ArticleList.vue'), meta: { title: '资讯' } },
  { path: 'articles/:id', name: 'ArticleDetail', component: () => import('@/views/portal/ArticleDetail.vue'), meta: { title: '资讯详情' } },
  {
    path: 'report', name: 'RescueReport', component: () => import('@/views/portal/RescueReport.vue'),
    meta: { title: '流浪动物上报', requiresAuth: true, roles: ['USER'] }
  },
  { path: 'donation', name: 'DonationCenter', component: () => import('@/views/portal/DonationCenter.vue'), meta: { title: '捐赠中心' } },
  {
    path: 'volunteer', name: 'Volunteer', component: () => import('@/views/portal/Volunteer.vue'),
    meta: { title: '志愿者报名', requiresAuth: true, roles: ['USER'] }
  },
  {
    path: 'me', name: 'Me', component: () => import('@/views/portal/Me.vue'),
    meta: { title: '个人中心', requiresAuth: true, roles: ['USER'] }
  }
];

export default portalRoutes;
