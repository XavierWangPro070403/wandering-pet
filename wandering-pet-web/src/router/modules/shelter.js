/**
 * 机构端路由：9 页（仅 SHELTER）
 */
const shelterRoutes = [
  {
    path: 'workbench', name: 'ShelterWorkbench', component: () => import('@/views/shelter/Workbench.vue'),
    meta: { title: '机构工作台', requiresAuth: true, roles: ['SHELTER'], perm: 'shelter:workbench' }
  },
  {
    path: 'pets', name: 'ShelterPets', component: () => import('@/views/shelter/PetManage.vue'),
    meta: { title: '宠物管理', requiresAuth: true, roles: ['SHELTER'], perm: 'shelter:pet' }
  },
  {
    path: 'pets/edit/:id?', name: 'ShelterPetEdit', component: () => import('@/views/shelter/PetEdit.vue'),
    meta: { title: '宠物发布/编辑', requiresAuth: true, roles: ['SHELTER'], perm: 'shelter:pet' }
  },
  {
    path: 'applications', name: 'ShelterApplications', component: () => import('@/views/shelter/Applications.vue'),
    meta: { title: '领养申请审核', requiresAuth: true, roles: ['SHELTER'], perm: 'shelter:adoption' }
  },
  {
    path: 'followups', name: 'ShelterFollowUps', component: () => import('@/views/shelter/FollowUps.vue'),
    meta: { title: '回访管理', requiresAuth: true, roles: ['SHELTER'], perm: 'shelter:followup' }
  },
  {
    path: 'donations', name: 'ShelterDonations', component: () => import('@/views/shelter/Donations.vue'),
    meta: { title: '募捐项目管理', requiresAuth: true, roles: ['SHELTER'], perm: 'shelter:donation' }
  },
  {
    path: 'donation-records', name: 'ShelterDonationRecords', component: () => import('@/views/shelter/DonationRecords.vue'),
    meta: { title: '捐赠明细', requiresAuth: true, roles: ['SHELTER'], perm: 'shelter:donation' }
  },
  {
    path: 'volunteers', name: 'ShelterVolunteers', component: () => import('@/views/shelter/Volunteers.vue'),
    meta: { title: '志愿者审核', requiresAuth: true, roles: ['SHELTER'], perm: 'shelter:volunteer' }
  },
  {
    path: 'chat', name: 'ShelterChat', component: () => import('@/views/shelter/Chat.vue'),
    meta: { title: '在线咨询', requiresAuth: true, roles: ['SHELTER'], perm: 'shelter:chat' }
  }
];

export default shelterRoutes;
