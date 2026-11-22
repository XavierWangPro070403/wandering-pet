-- =====================================================================================
-- 流浪宠物领养系统（wandering-pet）演示数据初始化脚本
-- 前置：先执行 init.sql（建库建表）
-- 账号：admin / shelter1 / shelter2 / shelter3 / demo 密码统一为 123456（BCrypt 密文）
-- 幂等：每次执行前清空演示数据表，可反复运行
-- =====================================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `wandering_pet`;

-- 清空演示数据（保持幂等）
TRUNCATE TABLE `sys_role_permission`;
TRUNCATE TABLE `sys_permission`;
TRUNCATE TABLE `sys_role`;
TRUNCATE TABLE `sys_dict`;
TRUNCATE TABLE `sys_user`;
TRUNCATE TABLE `user_address`;
TRUNCATE TABLE `chat_session`;
TRUNCATE TABLE `rescue_report`;
TRUNCATE TABLE `volunteer_application`;
TRUNCATE TABLE `donation_record`;
TRUNCATE TABLE `donation_project`;
TRUNCATE TABLE `notification`;
TRUNCATE TABLE `favorite`;
TRUNCATE TABLE `comment`;
TRUNCATE TABLE `article`;
TRUNCATE TABLE `follow_up`;
TRUNCATE TABLE `adoption_record`;
TRUNCATE TABLE `adoption_flow_log`;
TRUNCATE TABLE `adoption_application`;
TRUNCATE TABLE `pet_health_record`;
TRUNCATE TABLE `pet_image`;
TRUNCATE TABLE `pet`;
TRUNCATE TABLE `pet_breed`;
TRUNCATE TABLE `pet_category`;
TRUNCATE TABLE `shelter`;
TRUNCATE TABLE `user`;

-- =====================================================================================
-- 一、角色 / 权限 / 字典
-- =====================================================================================

INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`) VALUES
(1, 'ADMIN',   '平台管理员', '负责机构审核、用户管理、内容审核与数据看板'),
(2, 'SHELTER', '救助站',     '负责本机构宠物档案、领养审核、回访与募捐'),
(3, 'USER',    '普通用户',   '可申请领养、收藏、评论、捐赠、上报线索、报名志愿者'),
(4, 'GUEST',   '游客',       '未登录访客，仅可浏览宠物列表/详情/资讯');

INSERT INTO `sys_permission` (`id`, `perm_code`, `perm_name`, `perm_type`, `parent_id`, `path`, `sort`) VALUES
-- 管理端菜单
(1,  'admin:dashboard',    '数据看板',       'MENU', 0, '/admin/dashboard', 1),
(2,  'admin:shelter',      '机构审核',       'MENU', 0, '/admin/shelters',  2),
(3,  'admin:user',         '用户管理',       'MENU', 0, '/admin/users',     3),
(4,  'admin:pet',          '宠物全量管理',   'MENU', 0, '/admin/pets',      4),
(5,  'admin:content',      '内容管理',       'MENU', 0, '/admin/articles',  5),
(6,  'admin:rescue',       '线索管理',       'MENU', 0, '/admin/rescues',   6),
(7,  'admin:setting',      '系统设置',       'MENU', 0, '/admin/settings',  7),
-- 机构端菜单
(8,  'shelter:workbench',  '机构工作台',     'MENU', 0, '/shelter/workbench',   1),
(9,  'shelter:pet',        '宠物管理',       'MENU', 0, '/shelter/pets',        2),
(10, 'shelter:adoption',   '领养审核',       'MENU', 0, '/shelter/applications',3),
(11, 'shelter:followup',   '回访管理',       'MENU', 0, '/shelter/followups',   4),
(12, 'shelter:donation',   '募捐管理',       'MENU', 0, '/shelter/donations',   5),
(13, 'shelter:volunteer',  '志愿者审核',     'MENU', 0, '/shelter/volunteers',  6),
(14, 'shelter:chat',       '在线咨询',       'MENU', 0, '/shelter/chat',        7),
-- 宠物域接口/按钮
(15, 'pet:create',          '发布宠物档案',   'API', 0, '', 1),
(16, 'pet:update',          '编辑宠物档案',   'API', 0, '', 2),
(17, 'pet:delete',          '删除宠物档案',   'API', 0, '', 3),
(18, 'pet:status',          '宠物上下架',     'API', 0, '', 4),
(19, 'pet:manage-all',      '宠物全量管理',   'API', 0, '', 5),
(20, 'health-record:manage','健康档案维护',   'API', 0, '', 6),
-- 领养域接口/按钮
(21, 'adoption:review',     '领养申请审核',   'API', 0, '', 1),
(22, 'adoption:sign',       '签署领养协议',   'API', 0, '', 2),
(23, 'adoption:complete',   '完成领养归档',   'API', 0, '', 3),
(24, 'adoption:manage-all', '领养全量管理',   'API', 0, '', 4),
(25, 'followup:manage',     '回访任务管理',   'API', 0, '', 5),
-- 内容域接口/按钮
(26, 'article:manage',      '资讯文章管理',   'API', 0, '', 1),
(27, 'comment:audit',       '评论审核',       'API', 0, '', 2),
-- 机构域接口/按钮
(28, 'shelter:audit',       '机构入驻审核',   'API', 0, '', 1),
(29, 'shelter:update',      '机构信息维护',   'API', 0, '', 2),
-- 系统域接口/按钮
(30, 'user:manage',         'C 端用户管理',   'API', 0, '', 1),
(31, 'sys-user:manage',     '后台账号管理',   'API', 0, '', 2),
(32, 'rescue:manage',       '线索分派处理',   'API', 0, '', 3),
(33, 'donation:manage',     '募捐项目管理',   'API', 0, '', 4),
(34, 'donation:record:view','捐赠明细查看',   'API', 0, '', 5),
(35, 'volunteer:audit',     '志愿者审核',     'API', 0, '', 6),
(36, 'chat:use',            '在线咨询使用',   'API', 0, '', 7),
(37, 'notification:push',   '消息批量推送',   'API', 0, '', 8),
(38, 'dict:manage',         '数据字典管理',   'API', 0, '', 9),
(39, 'role:manage',         '角色权限管理',   'API', 0, '', 10),
(40, 'oplog:view',          '操作日志查看',   'API', 0, '', 11),
(41, 'statistics:view',     '统计看板查看',   'API', 0, '', 12),
(42, 'file:upload',         '文件上传',       'API', 0, '', 13);

-- 管理员：全部权限
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`)
SELECT 1, `id` FROM `sys_permission`;

-- 救助站：机构端菜单 + 机构业务权限
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(2, 8), (2, 9), (2, 10), (2, 11), (2, 12), (2, 13), (2, 14),
(2, 15), (2, 16), (2, 17), (2, 18), (2, 20), (2, 21), (2, 22),
(2, 23), (2, 25), (2, 29), (2, 33), (2, 34), (2, 35), (2, 36),
(2, 41), (2, 42);

INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_label`, `sort`, `status`) VALUES
('pet_gender', 'MALE', '公', 1, 1),
('pet_gender', 'FEMALE', '母', 2, 1),
('pet_gender', 'UNKNOWN', '未知', 3, 1),
('pet_size', 'SMALL', '小型', 1, 1),
('pet_size', 'MEDIUM', '中型', 2, 1),
('pet_size', 'LARGE', '大型', 3, 1),
('pet_status', 'AVAILABLE', '可领养', 1, 1),
('pet_status', 'PENDING', '审核中', 2, 1),
('pet_status', 'ADOPTED', '已领养', 3, 1),
('pet_status', 'MEDICAL', '治疗中', 4, 1),
('pet_status', 'OFFLINE', '已下架', 5, 1),
('pet_health', 'HEALTHY', '健康', 1, 1),
('pet_health', 'GENERAL', '一般', 2, 1),
('pet_health', 'ILL', '治疗中', 3, 1),
('health_record_type', 'VACCINE', '疫苗接种', 1, 1),
('health_record_type', 'DEWORM', '体内外驱虫', 2, 1),
('health_record_type', 'STERILIZE', '绝育手术', 3, 1),
('health_record_type', 'TREATMENT', '疾病治疗', 4, 1),
('health_record_type', 'CHECK', '健康体检', 5, 1),
('application_status', 'DRAFT', '草稿', 1, 1),
('application_status', 'SUBMITTED', '已提交', 2, 1),
('application_status', 'REVIEWING', '审核中', 3, 1),
('application_status', 'INTERVIEW', '待面谈', 4, 1),
('application_status', 'HOME_VISIT', '待家访', 5, 1),
('application_status', 'APPROVED', '审核通过', 6, 1),
('application_status', 'SIGNED', '已签约', 7, 1),
('application_status', 'ADOPTED', '领养完成', 8, 1),
('application_status', 'REJECTED', '已驳回', 9, 1),
('application_status', 'CANCELED', '已撤回', 10, 1),
('application_status', 'EXPIRED', '已过期', 11, 1),
('application_status', 'RETURNED', '已退养', 12, 1),
('housing_type', 'OWN', '自有住房', 1, 1),
('housing_type', 'RENT', '租房', 2, 1),
('article_category', 'RAISE', '饲养指导', 1, 1),
('article_category', 'KNOWLEDGE', '科普百科', 2, 1),
('article_category', 'NEWS', '平台动态', 3, 1),
('article_category', 'STORY', '救助故事', 4, 1),
('report_urgency', '1', '一般', 1, 1),
('report_urgency', '2', '紧急', 2, 1),
('report_urgency', '3', '危急', 3, 1),
('report_status', 'PENDING', '待处理', 1, 1),
('report_status', 'ASSIGNED', '已分派', 2, 1),
('report_status', 'PROCESSING', '处理中', 3, 1),
('report_status', 'RESOLVED', '已解决', 4, 1),
('report_status', 'CLOSED', '已关闭', 5, 1),
('follow_up_status', 'PENDING', '待回访', 1, 1),
('follow_up_status', 'DONE', '已完成', 2, 1),
('follow_up_status', 'OVERDUE', '已逾期', 3, 1),
('shelter_audit_status', '0', '待审核', 1, 1),
('shelter_audit_status', '1', '已通过', 2, 1),
('shelter_audit_status', '2', '已驳回', 3, 1),
('volunteer_status', 'PENDING', '待审核', 1, 1),
('volunteer_status', 'APPROVED', '已通过', 2, 1),
('volunteer_status', 'REJECTED', '未通过', 3, 1);

-- =====================================================================================
-- 二、机构与账号
-- =====================================================================================

INSERT INTO `shelter` (`id`, `name`, `logo`, `license_no`, `contact_name`, `contact_phone`, `province`, `city`, `district`, `address`, `longitude`, `latitude`, `intro`, `audit_status`, `audit_time`, `audit_by`) VALUES
(1, '毛窝窝流浪动物救助站', 'http://localhost:8080/imgs/shelter/s01.jpg', '91330106MA27XW111A', '陈远山', '13800000003', '浙江省', '杭州', '西湖区', '西湖区文一西路辅路 100 号院内', 120.088520, 30.289400,
 '成立于 2017 年的民间救助机构，现有在册志愿者 60 余人，主要接收杭州城区的流浪猫犬，执行“免疫-绝育-领养”标准流程，领养后提供 4 次定期回访。', 1, '2026-03-12 10:00:00', 1),
(2, '汪喵之家小动物保护中心', 'http://localhost:8080/imgs/shelter/s02.jpg', '91510100MA6C2X222B', '罗晓萌', '13800000004', '四川省', '成都', '高新区', '高新区益州大道中段 1999 号园区内', 104.065035, 30.545820,
 '成都本地注册的小动物保护组织，下设两个收容点，长期开展“抓捕-绝育-放归（TNR）”与领养开放日活动，所有待领养动物均完成疫苗与驱虫。', 1, '2026-04-02 14:30:00', 1),
(3, '阳光伴侣动物救助基地', 'http://localhost:8080/imgs/shelter/s03.jpg', '91110105MA01Y333C', '赵一鸣', '13800000005', '北京市', '北京', '朝阳区', '朝阳区金盏乡东窑路东侧', 116.588450, 39.956730,
 '北京朝阳区的伴侣动物救助基地，正在申请入驻平台，具备犬类收容场地 800 平方米，待平台审核通过后开展领养业务。', 0, NULL, NULL);

-- 密码统一为 123456（BCrypt 强度 10）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `avatar`, `role_code`, `shelter_id`, `status`) VALUES
(1, 'admin',    '$2a$10$BeUHtX2T.tYc39mFwjGdI.CzZwUSHjgZ.DoO/dqdJJ6j6j2dbRTCC', '平台管理员', '13800000000', 'http://localhost:8080/imgs/avatar/a01.jpg', 'ADMIN',   NULL, 1),
(2, 'shelter1', '$2a$10$BeUHtX2T.tYc39mFwjGdI.CzZwUSHjgZ.DoO/dqdJJ6j6j2dbRTCC', '陈远山',     '13800000003', 'http://localhost:8080/imgs/shelter/s01.jpg', 'SHELTER', 1,    1),
(3, 'shelter2', '$2a$10$BeUHtX2T.tYc39mFwjGdI.CzZwUSHjgZ.DoO/dqdJJ6j6j2dbRTCC', '罗晓萌',     '13800000004', 'http://localhost:8080/imgs/shelter/s02.jpg', 'SHELTER', 2,    1),
(4, 'shelter3', '$2a$10$BeUHtX2T.tYc39mFwjGdI.CzZwUSHjgZ.DoO/dqdJJ6j6j2dbRTCC', '赵一鸣',     '13800000005', 'http://localhost:8080/imgs/shelter/s03.jpg', 'SHELTER', 3,    1);

-- =====================================================================================
-- 三、C 端用户
-- =====================================================================================

-- user.phone / user.id_card_no 为 AES-128-CBC 加密后的密文（Base64），密钥见后端 application.yml
INSERT INTO `user` (`id`, `username`, `password`, `openid`, `nickname`, `avatar`, `phone`, `gender`, `city`, `id_card_no`, `real_name`, `real_name_status`, `status`, `last_login_time`) VALUES
(1, 'demo', '$2a$10$BeUHtX2T.tYc39mFwjGdI.CzZwUSHjgZ.DoO/dqdJJ6j6j2dbRTCC', 'dev_wechat_001', '江小领', 'http://localhost:8080/imgs/avatar/a02.jpg',
 'w93NXMAb/sBofr51FjbL5A==', 1, '杭州', '9v/vY7ntWWshSetDRLPOpYv1wFoMRyICn2UPjihIQgE=', '江小领', 1, 1, '2026-11-15 19:20:00'),
(2, NULL, NULL, 'dev_wechat_002', '汪汪队友', 'http://localhost:8080/imgs/avatar/a03.jpg',
 'B7ftt20UnDfuthvvV94TDg==', 2, '成都', NULL, '', 0, 1, '2026-11-14 21:05:00');

INSERT INTO `user_address` (`user_id`, `receiver`, `phone`, `province`, `city`, `district`, `detail`, `is_default`) VALUES
(1, '江小领', '13800000001', '浙江省', '杭州', '西湖区', '西湖区文二路街道文体中心旁', 1);

-- =====================================================================================
-- 四、物种 / 品种
-- =====================================================================================

INSERT INTO `pet_category` (`id`, `name`, `icon`, `sort`) VALUES
(1, '猫',   'http://localhost:8080/imgs/icon/cat.png',    1),
(2, '狗',   'http://localhost:8080/imgs/icon/dog.png',    2),
(3, '兔',   'http://localhost:8080/imgs/icon/rabbit.png', 3),
(4, '其他', 'http://localhost:8080/imgs/icon/other.png',  4);

INSERT INTO `pet_breed` (`category_id`, `name`, `sort`) VALUES
(1, '中华田园猫', 1), (1, '英国短毛猫', 2), (1, '美国短毛猫', 3), (1, '布偶猫', 4),
(1, '暹罗猫', 5), (1, '橘猫', 6), (1, '狸花猫', 7), (1, '三花猫', 8),
(2, '中华田园犬', 1), (2, '金毛寻回犬', 2), (2, '拉布拉多', 3), (2, '柯基', 4),
(2, '柴犬', 5), (2, '比熊', 6), (2, '泰迪贵宾', 7), (2, '边境牧羊犬', 8),
(2, '萨摩耶', 9), (2, '哈士奇', 10),
(3, '垂耳兔', 1), (3, '侏儒兔', 2);

-- =====================================================================================
-- 五、宠物档案（12 只，覆盖猫/狗/兔、不同状态、不同城市）
-- 图片为演示素材（来源见 docs/design/photo-credits.md），由后端 static 目录统一托管
-- =====================================================================================

INSERT INTO `pet` (`id`, `pet_no`, `name`, `category_id`, `breed_id`, `gender`, `age_month`, `weight`, `size`, `color`, `sterilized`, `vaccinated`, `dewormed`, `health_status`, `character_desc`, `adoption_requirements`, `story`, `cover_image`, `shelter_id`, `status`, `view_count`, `favorite_count`, `publish_time`) VALUES
(1, 'PET20261101001', '橘子', 1, 6, 'MALE', 14, 4.20, 'SMALL', '橘白', 1, 1, 1, 'HEALTHY',
 '亲人黏人，会主动蹭手求摸，对其他猫友好，会用猫砂盆，饭点准时蹲守。',
 '接受科学喂养，按时免疫驱虫；封窗封网；家人同意且全体不过敏；承诺不离不弃，配合 4 次定期回访。',
 '去年秋天在留下街道的快餐店后厨被发现，当时的橘子瘦得只剩一把骨头，眼睛还有点发炎。救助站的志愿者每天带它上药、喂罐头，两周后它开始主动蹭人。现在它是站里的“迎宾猫”，谁进门都要去闻一闻。',
 'http://localhost:8080/imgs/pet/p01.jpg', 1, 'AVAILABLE', 486, 32, '2026-11-01 10:00:00'),
(2, 'PET20261101002', '团子', 1, 7, 'FEMALE', 20, 3.60, 'SMALL', '狸花', 1, 1, 1, 'HEALTHY',
 '独立稳重，喜欢在高处观察人，熟悉之后会跳上膝盖午睡，不闹腾。',
 '家中无未封纱窗的窗户；能接受 7-30 天的适应期；不散养。',
 '团子是在文一西路高架桥下的绿化带里被救助的，当时它正守着一只翻倒的垃圾桶。经检查它已怀孕，救助站为它接生了三只小猫，幼猫均已找到领养，团子自己在完成绝育后也排队等一个新家。',
 'http://localhost:8080/imgs/pet/p04.jpg', 1, 'AVAILABLE', 312, 21, '2026-11-01 10:05:00'),
(3, 'PET20261102001', '雪球', 1, 4, 'FEMALE', 9, 2.80, 'SMALL', '纯白', 0, 1, 1, 'HEALTHY',
 '布偶幼猫，性格软糯，抱起来不打挣扎，喜欢追逗猫棒，声音很轻。',
 '有养猫经验者优先；已预约绝育（满 6 个月执行）；室内饲养；签署领养协议并接受回访。',
 '雪球原是城西某小区住户散养的猫，一窝生下后被遗弃在纸箱里。志愿者循着叫声找了两天才在灌木丛里把它救出来。它现在已完成两针疫苗，正在等待满六月龄做绝育。',
 'http://localhost:8080/imgs/pet/p07.jpg', 1, 'PENDING', 208, 18, '2026-11-02 09:30:00'),
(4, 'PET20261015001', '大壮', 2, 10, 'MALE', 30, 28.50, 'LARGE', '金黄色', 1, 1, 1, 'HEALTHY',
 '标准金毛脾气，对人和其他狗都热情，会坐下、握手、捡球，散步不爆冲。',
 '有大型犬饲养经验；住址需允许中型以上犬；每天保证 1 小时以上户外活动；全家同意。',
 '大壮原是工地看护犬，工地撤场后被锁在铁笼里遗弃。救助站接手时它有轻度皮肤病，经过 6 周药浴和营养补充已完全康复。它2026年11月通过审核完成领养，是站里领养流程的“样板生”。',
 'http://localhost:8080/imgs/pet/p10.jpg', 1, 'ADOPTED', 351, 15, '2026-10-15 15:00:00'),
(5, 'PET20261103001', '煤球', 1, 2, 'MALE', 24, 4.90, 'SMALL', '纯黑', 1, 1, 1, 'ILL',
 '安静内向，警惕期约一周，熟悉后信任度很高，爱吃冻干，喜欢纸箱。',
 '能接受长期用药观察；家中无其他未免疫宠物；耐心对待慢热型猫。',
 '煤球被送来时上呼吸道感染未愈，目前仍在治疗期，待复查两次指标正常后恢复可领养状态。它不闹不叫，治疗时很配合，医护都说它是“最省心的病人”。',
 'http://localhost:8080/imgs/pet/p13.jpg', 1, 'MEDICAL', 154, 9, '2026-11-03 11:00:00'),
(6, 'PET20261103002', '年糕', 1, 8, 'FEMALE', 11, 3.10, 'SMALL', '三花', 1, 1, 1, 'HEALTHY',
 '三花小母猫，好奇心强，抽屉、纸袋、洗衣机滚筒都要查一遍，爱蹭腿。',
 '封网；适龄绝育已完成；接受视频回访；不弃养。',
 '年糕是良渚农贸市场的“摊位猫”，摊主搬家时把它留了下来。市场管理员联系救助站，它现在每天巡视站里的走廊，像在查摊。',
 'http://localhost:8080/imgs/pet/p16.jpg', 1, 'AVAILABLE', 267, 25, '2026-11-03 11:20:00'),
(7, 'PET20261105001', '麦兜', 2, 12, 'MALE', 18, 11.80, 'MEDIUM', '黄白双色', 1, 1, 1, 'HEALTHY',
 '短腿小柯基，精力旺盛，爱追球，会坐会等，对小孩友好。',
 '有养犬经验；能保证每日遛狗；科学喂养控制体重（柯基易胖，需控制腰椎负担）。',
 '麦兜原主人因怀孕弃养。它刚来时超重 2 公斤，救助站的志愿者给它制定了减肥计划，现在体重已回到标准区间，跑起来依然像个小毛球。',
 'http://localhost:8080/imgs/pet/p19.jpg', 2, 'AVAILABLE', 429, 28, '2026-11-05 10:00:00'),
(8, 'PET20261105002', '豆豆', 2, 15, 'FEMALE', 22, 5.20, 'SMALL', '咖啡色', 1, 1, 1, 'HEALTHY',
 '泰迪妹妹，粘人，会握手与转圈，不喜欢被单独留在家太久。',
 '居家办公或有人陪伴的家庭优先；按时美容；不离不弃。',
 '豆豆是宠物店繁育淘汰的“库存犬”，来时毛打结严重、皮下有褥疮。剃毛、药浴两个月后，它的卷毛重新长了出来，见人会转圈打招呼。',
 'http://localhost:8080/imgs/pet/p22.jpg', 2, 'AVAILABLE', 388, 30, '2026-11-05 10:10:00'),
(9, 'PET20261106001', '阿柴', 2, 13, 'MALE', 16, 9.60, 'MEDIUM', '赤柴色', 1, 1, 1, 'HEALTHY',
 '典型柴犬性格：聪明但有自己的主意，牵绳散步服从性好，掉毛季需勤梳。',
 '了解柴犬性格，不因掉毛弃养；阳台封窗；接受领养协议与回访。',
 '阿柴在双流机场附近的草丛里流浪了一个月，靠投喂点的粮食撑了下来。被抓捕时它已经学会见人就跑，救助站用了整整两周建立信任。现在它见到牵引绳会自己伸爪子。',
 'http://localhost:8080/imgs/pet/p25.jpg', 2, 'AVAILABLE', 456, 26, '2026-11-06 09:00:00'),
(10, 'PET20261106002', '哈雷', 2, 18, 'MALE', 20, 24.30, 'LARGE', '黑白', 1, 1, 1, 'HEALTHY',
 '哈士奇，运动量需求大，嗓门洪亮但不怕生，会“说话”，对猫和小动物需引导。',
 '有大犬经验；家庭居住（非合租）；每日运动量不低于 1.5 小时；接受喂食规范指导。',
 '哈雷的主人是位经常出差的销售，实在照顾不过来，通过平台做移交。它精力旺盛，是站里遛狗志愿者的“考核项目”，但服从训练基础不错。',
 'http://localhost:8080/imgs/pet/p28.jpg', 2, 'AVAILABLE', 512, 33, '2026-11-06 09:15:00'),
(11, 'PET20261108001', '糯米', 3, 19, 'FEMALE', 7, 1.60, 'SMALL', '白色垂耳', 0, 0, 0, 'HEALTHY',
 '垂耳兔，爱吃提摩西草和油麦菜，安静不掉毛量大，适合上班族。',
 '室内饲养并做好电线防护；以牧草为主食；不散放；承诺不因搬家弃养。',
 '糯米是学校门口被当作“抽奖奖品”卖剩的小兔子，一位老师买下后联系救助站。它现在住在站里的兔舍，耳朵一激动就耷拉下来。',
 'http://localhost:8080/imgs/pet/p31.jpg', 2, 'AVAILABLE', 143, 12, '2026-11-08 14:00:00'),
(12, 'PET20261110001', '球球', 2, 9, 'MALE', 12, 8.40, 'MEDIUM', '黄黑田园色', 1, 1, 1, 'HEALTHY',
 '田园犬里的“优等生”：不叫唤、不拆家、会定点排便，对人零戒备。',
 '不限住房类型，但需全体家人同意；按城市规定办犬证；接受 4 次回访。',
 '球球是余杭塘河边跟着环卫工师傅长大的流浪狗，师傅退休后无法继续喂养。它做过完整体检，是站里体检指标最标准的一只狗。',
 'http://localhost:8080/imgs/pet/p34.jpg', 2, 'AVAILABLE', 231, 19, '2026-11-10 16:00:00');

-- 相册（每只 3 张，与封面不重复；另含 followup/report/shelter/article 等演示图，见 photo-credits.md）
INSERT INTO `pet_image` (`pet_id`, `object_name`, `url`, `sort`, `type`) VALUES
(1,  'pet/2026/11/p02.jpg', 'http://localhost:8080/imgs/pet/p02.jpg', 1, 'IMAGE'), (1,  'pet/2026/11/p03.jpg', 'http://localhost:8080/imgs/pet/p03.jpg', 2, 'IMAGE'), (1,  'pet/2026/11/p37.jpg', 'http://localhost:8080/imgs/pet/p37.jpg', 3, 'IMAGE'),
(2,  'pet/2026/11/p05.jpg', 'http://localhost:8080/imgs/pet/p05.jpg', 1, 'IMAGE'), (2,  'pet/2026/11/p06.jpg', 'http://localhost:8080/imgs/pet/p06.jpg', 2, 'IMAGE'), (2,  'pet/2026/11/p38.jpg', 'http://localhost:8080/imgs/pet/p38.jpg', 3, 'IMAGE'),
(3,  'pet/2026/11/p08.jpg', 'http://localhost:8080/imgs/pet/p08.jpg', 1, 'IMAGE'), (3,  'pet/2026/11/p09.jpg', 'http://localhost:8080/imgs/pet/p09.jpg', 2, 'IMAGE'), (3,  'pet/2026/11/p39.jpg', 'http://localhost:8080/imgs/pet/p39.jpg', 3, 'IMAGE'),
(4,  'pet/2026/10/p11.jpg', 'http://localhost:8080/imgs/pet/p11.jpg', 1, 'IMAGE'), (4,  'pet/2026/10/p12.jpg', 'http://localhost:8080/imgs/pet/p12.jpg', 2, 'IMAGE'), (4,  'pet/2026/10/p40.jpg', 'http://localhost:8080/imgs/pet/p40.jpg', 3, 'IMAGE'),
(5,  'pet/2026/11/p14.jpg', 'http://localhost:8080/imgs/pet/p14.jpg', 1, 'IMAGE'), (5,  'pet/2026/11/p15.jpg', 'http://localhost:8080/imgs/pet/p15.jpg', 2, 'IMAGE'), (5,  'pet/2026/11/p41.jpg', 'http://localhost:8080/imgs/pet/p41.jpg', 3, 'IMAGE'),
(6,  'pet/2026/11/p17.jpg', 'http://localhost:8080/imgs/pet/p17.jpg', 1, 'IMAGE'), (6,  'pet/2026/11/p18.jpg', 'http://localhost:8080/imgs/pet/p18.jpg', 2, 'IMAGE'), (6,  'pet/2026/11/p42.jpg', 'http://localhost:8080/imgs/pet/p42.jpg', 3, 'IMAGE'),
(7,  'pet/2026/11/p20.jpg', 'http://localhost:8080/imgs/pet/p20.jpg', 1, 'IMAGE'), (7,  'pet/2026/11/p21.jpg', 'http://localhost:8080/imgs/pet/p21.jpg', 2, 'IMAGE'), (7,  'pet/2026/11/p43.jpg', 'http://localhost:8080/imgs/pet/p43.jpg', 3, 'IMAGE'),
(8,  'pet/2026/11/p23.jpg', 'http://localhost:8080/imgs/pet/p23.jpg', 1, 'IMAGE'), (8,  'pet/2026/11/p24.jpg', 'http://localhost:8080/imgs/pet/p24.jpg', 2, 'IMAGE'), (8,  'pet/2026/11/p44.jpg', 'http://localhost:8080/imgs/pet/p44.jpg', 3, 'IMAGE'),
(9,  'pet/2026/11/p26.jpg', 'http://localhost:8080/imgs/pet/p26.jpg', 1, 'IMAGE'), (9,  'pet/2026/11/p27.jpg', 'http://localhost:8080/imgs/pet/p27.jpg', 2, 'IMAGE'), (9,  'pet/2026/11/p45.jpg', 'http://localhost:8080/imgs/pet/p45.jpg', 3, 'IMAGE'),
(10, 'pet/2026/11/p29.jpg', 'http://localhost:8080/imgs/pet/p29.jpg', 1, 'IMAGE'), (10, 'pet/2026/11/p30.jpg', 'http://localhost:8080/imgs/pet/p30.jpg', 2, 'IMAGE'), (10, 'pet/2026/11/p46.jpg', 'http://localhost:8080/imgs/pet/p46.jpg', 3, 'IMAGE'),
(11, 'pet/2026/11/p32.jpg', 'http://localhost:8080/imgs/pet/p32.jpg', 1, 'IMAGE'), (11, 'pet/2026/11/p33.jpg', 'http://localhost:8080/imgs/pet/p33.jpg', 2, 'IMAGE'), (11, 'pet/2026/11/p47.jpg', 'http://localhost:8080/imgs/pet/p47.jpg', 3, 'IMAGE'),
(12, 'pet/2026/11/p35.jpg', 'http://localhost:8080/imgs/pet/p35.jpg', 1, 'IMAGE'), (12, 'pet/2026/11/p36.jpg', 'http://localhost:8080/imgs/pet/p36.jpg', 2, 'IMAGE'), (12, 'pet/2026/11/p48.jpg', 'http://localhost:8080/imgs/pet/p48.jpg', 3, 'IMAGE');

-- 健康档案（时间轴演示数据）
INSERT INTO `pet_health_record` (`pet_id`, `record_type`, `record_date`, `title`, `content`, `vet_name`, `attachment_url`) VALUES
(1, 'VACCINE',   '2026-09-12', '猫三联第一针', '妙三多第一针，注射后无异常反应。', '杭州安安宠医·城西店', ''),
(1, 'DEWORM',    '2026-10-08', '体内外驱虫', '海乐妙内驱 + 大宠爱外驱，体重 4.2kg 按量给药。', '', ''),
(1, 'STERILIZE', '2026-10-20', '绝育手术', '公猫绝育，手术顺利，第七天拆线，伤口愈合良好。', '杭州安安宠医·城西店', ''),
(2, 'VACCINE',   '2026-08-30', '猫三联加强', '年度加强免疫，注射后食欲正常。', '杭州安安宠医·城西店', ''),
(2, 'CHECK',     '2026-10-25', '入站体检', '血常规、粪检均正常，体况评分 5/9。', '杭州联合动物医院', ''),
(3, 'VACCINE',   '2026-10-15', '猫三联第二针', '幼猫免疫程序第二针，精神食欲良好。', '杭州安安宠医·城西店', ''),
(3, 'CHECK',     '2026-11-01', '绝育前评估', '体重 2.8kg，月龄 9，预约 12 月绝育手术。', '杭州安安宠医·城西店', ''),
(4, 'TREATMENT', '2026-09-05', '皮肤病治疗', '蠕形螨继发浅表脓皮症，药浴每周两次，口服药 21 天。', '杭州联合动物医院', ''),
(4, 'CHECK',     '2026-10-20', '康复复查', '皮肤镜检转阴，被毛重新覆盖，可安排领养。', '杭州联合动物医院', ''),
(5, 'TREATMENT', '2026-11-04', '上呼吸道感染治疗', '多西环素按 5mg/kg 给药，配合雾化每日一次，预计疗程 14 天。', '杭州联合动物医院', ''),
(5, 'CHECK',     '2026-11-14', '疗程中期复查', '打喷嚏频次下降，眼鼻分泌物减少，继续当前方案。', '杭州联合动物医院', ''),
(7, 'VACCINE',   '2026-09-20', '犬四联年度免疫', '年度加强针，注射后无异常。', '成都瑞派华西宠物医院', ''),
(7, 'CHECK',     '2026-10-28', '减重复查', '体重由 13.9kg 降至 11.8kg，腰椎触诊无疼痛反应。', '成都瑞派华西宠物医院', ''),
(9, 'VACCINE',   '2026-10-02', '犬四联 + 狂犬', '免疫齐全，已办理芯片登记。', '成都瑞派华西宠物医院', ''),
(11, 'CHECK',    '2026-11-09', '入站体检', '粪检阴性，牙齿咬合正常，建议 6 月龄后评估绝育。', '成都瑞派华西宠物医院', '');

-- =====================================================================================
-- 六、领养申请全链路演示
-- =====================================================================================

INSERT INTO `adoption_application` (`id`, `apply_no`, `pet_id`, `user_id`, `shelter_id`, `applicant_name`, `applicant_phone`, `applicant_age`, `occupation`, `city`, `housing_type`, `has_pet_experience`, `family_agree`, `has_children`, `has_allergy`, `reason`, `status`, `interview_time`, `home_visit_time`, `audit_remark`, `auditor_id`, `audit_time`, `create_time`) VALUES
(1, 'APP20261110001', 1, 1, 1, '江小领', '13800000001', 26, '软件工程师', '杭州', 'OWN', 1, 1, 0, 0,
 '一直想领养一只亲人的猫，家里已封窗，有养猫经验，希望给橘子一个室内生活的家。', 'REVIEWING', NULL, NULL, '', NULL, NULL, '2026-11-10 20:12:00'),
(2, 'APP20261108002', 3, 2, 1, '汪汪队友', '13800000002', 29, '护士', '杭州', 'OWN', 1, 1, 0, 0,
 '家里有一只 3 岁的英短，希望给雪球找个伴，白天家里有人，可以照顾幼猫。', 'INTERVIEW', '2026-11-18 14:00:00', NULL, '初筛通过，安排到场面谈。', 2, '2026-11-12 10:00:00', '2026-11-08 15:40:00'),
(3, 'APP20261015003', 4, 1, 1, '江小领', '13800000001', 26, '软件工程师', '杭州', 'OWN', 1, 1, 0, 0,
 '家有一楼小院已围栏，每天可遛狗两次，希望领养一只性格稳定的成年犬陪伴家人。', 'ADOPTED', '2026-10-18 10:00:00', '2026-10-25 15:00:00', '面谈与家访均通过，同意领养。', 2, '2026-11-02 09:30:00', '2026-10-15 09:00:00'),
(4, 'APP20261109004', 7, 2, 2, '汪汪队友', '13800000002', 29, '护士', '成都', 'RENT', 0, 1, 0, 1,
 '很喜欢柯基，虽然租房但房东允许养狗。', 'REJECTED', NULL, NULL, '申请人有过敏史且无养犬经验，建议先积累饲养知识。', 3, '2026-11-11 11:00:00', '2026-11-09 18:20:00'),
(5, 'APP20261112005', 2, 1, 1, '江小领', '13800000001', 26, '软件工程师', '杭州', 'OWN', 1, 1, 0, 0,
 '想再领一只狸花猫作伴。', 'CANCELED', NULL, NULL, '', NULL, NULL, '2026-11-12 09:00:00');

INSERT INTO `adoption_flow_log` (`application_id`, `from_status`, `to_status`, `operator_id`, `operator_type`, `remark`, `create_time`) VALUES
(1, '',          'SUBMITTED',  1, 'USER',    '提交领养申请', '2026-11-10 20:12:00'),
(1, 'SUBMITTED', 'REVIEWING',  2, 'SHELTER', '救助站受理，开始资料审核', '2026-11-11 09:30:00'),
(2, '',          'SUBMITTED',  2, 'USER',    '提交领养申请', '2026-11-08 15:40:00'),
(2, 'SUBMITTED', 'REVIEWING',  2, 'SHELTER', '救助站受理', '2026-11-09 10:00:00'),
(2, 'REVIEWING', 'INTERVIEW',  2, 'SHELTER', '初筛通过，面谈时间 2026-11-18 14:00', '2026-11-12 10:00:00'),
(3, '',          'SUBMITTED',  1, 'USER',    '提交领养申请', '2026-10-15 09:00:00'),
(3, 'SUBMITTED', 'REVIEWING',  2, 'SHELTER', '救助站受理', '2026-10-15 16:00:00'),
(3, 'REVIEWING', 'INTERVIEW',  2, 'SHELTER', '初筛通过，面谈时间 2026-10-18 10:00', '2026-10-16 10:00:00'),
(3, 'INTERVIEW', 'HOME_VISIT', 2, 'SHELTER', '面谈通过，家访时间 2026-10-25 15:00', '2026-10-18 11:30:00'),
(3, 'HOME_VISIT','APPROVED',   2, 'SHELTER', '家访通过，居住环境符合领养要求', '2026-10-26 09:00:00'),
(3, 'APPROVED',  'SIGNED',     2, 'SHELTER', '2026-11-05 到站签署领养协议', '2026-11-05 14:00:00'),
(3, 'SIGNED',    'ADOPTED',    2, 'SHELTER', '领养完成，进入回访计划', '2026-11-05 14:30:00'),
(4, '',          'SUBMITTED',  2, 'USER',    '提交领养申请', '2026-11-09 18:20:00'),
(4, 'SUBMITTED', 'REVIEWING',  3, 'SHELTER', '救助站受理', '2026-11-10 09:00:00'),
(4, 'REVIEWING', 'REJECTED',   3, 'SHELTER', '申请人有过敏史且无养犬经验，建议先积累饲养知识', '2026-11-11 11:00:00'),
(5, '',          'SUBMITTED',  1, 'USER',    '提交领养申请', '2026-11-12 09:00:00'),
(5, 'SUBMITTED', 'CANCELED',   1, 'USER',    '申请人主动撤回', '2026-11-13 20:00:00');

INSERT INTO `adoption_record` (`id`, `adoption_no`, `application_id`, `pet_id`, `user_id`, `shelter_id`, `sign_date`, `agreement_object_name`, `agreement_url`, `status`) VALUES
(1, 'ADO20261105001', 3, 4, 1, 1, '2026-11-05', 'agreement/2026/11/ado20261105001.pdf', '', 'ADOPTED');

-- ADOPTED 后自动生成 4 次回访（第 7/30/90/180 天），第 1 次已完成
INSERT INTO `follow_up` (`record_id`, `application_id`, `plan_date`, `actual_date`, `round`, `weight`, `health_status`, `vaccine_status`, `life_desc`, `images`, `handler_id`, `status`) VALUES
(1, 3, '2026-11-12', '2026-11-12', 1, 28.60, '皮肤痊愈，被毛有光泽，精神食欲俱佳', '疫苗在有效期内，无需补种', '大壮已经认识家里的每个成员，每天早晚陪爷爷下楼遛弯，学会了在门口等拖鞋。',
 '["http://localhost:8080/imgs/followup/f01.jpg","http://localhost:8080/imgs/followup/f02.jpg","http://localhost:8080/imgs/followup/f03.jpg"]', 2, 'DONE'),
(1, 3, '2026-12-05', NULL, 2, NULL, '', '', '', NULL, NULL, 'PENDING'),
(1, 3, '2027-02-03', NULL, 3, NULL, '', '', '', NULL, NULL, 'PENDING'),
(1, 3, '2027-05-04', NULL, 4, NULL, '', '', '', NULL, NULL, 'PENDING');

-- =====================================================================================
-- 七、资讯 / 评论 / 收藏 / 消息
-- =====================================================================================

INSERT INTO `article` (`id`, `title`, `cover`, `category`, `summary`, `content`, `author`, `view_count`, `like_count`, `is_top`, `status`, `publish_time`) VALUES
(1, '新手养猫前必须确认的 8 件小事', 'http://localhost:8080/imgs/article/a01.jpg', 'RAISE',
 '封窗、疫苗、猫砂盆位置……这些细节决定猫和你前三个月的相处质量。',
 '<p>领养代替购买，是这件事最正确的开始方式。但在把猫接回家之前，有 8 件小事值得逐条确认：</p><p>一、封窗封网。高层坠猫是流浪猫最主要的“二次流浪”来源之一，纱窗要选能承重的款式。</p><p>二、疫苗与驱虫计划。接回 7-10 天适应期结束后安排首针，之后按时加强。</p><p>三、猫砂盆位置。远离食盆、通风但不穿堂风的位置最理想。</p><p>四、高危物品收纳。百合科植物、洋葱巧克力、线性玩具都要收好。</p><p>五、独立房间。前三天给猫一个 6-8 平方米的安静房间，比整个家更能降低应激。</p><p>六、稳定喂食时间。固定早晚两餐，比自由采食更容易观察食欲变化。</p><p>七、留出躲藏处。纸箱永远好过昂贵的猫窝。</p><p>八、和救助站保持联系。适应期有任何问题，先咨询救助站，不要轻易放弃。</p>',
 '毛窝窝救助站·编辑部', 1206, 87, 1, 1, '2026-10-28 09:00:00'),
(2, '猫粮配料表怎么读：前五位成分说明什么', 'http://localhost:8080/imgs/article/a02.jpg', 'KNOWLEDGE',
 '配料表按含量降序排列，前五位里有明确肉类来源，比“高蛋白”三个字更值得看。',
 '<p>选猫粮时，很多人第一眼只看蛋白质百分比，其实配料表的前五位更能说明问题。</p><p>第一，配料按添加量降序排列，前五位出现了明确的肉类（如“鸡肉”“三文鱼”），优于只出现谷物或“肉粉”的产品。</p><p>第二，警惕只有“肉类及其副产品”这类模糊表述的配方，来源不明的蛋白来源更容易引起肠胃不适。</p><p>第三，牛磺酸是猫的必需营养素，配方表里应当能找到。</p><p>第四，换粮遵循 7 日过渡法，新旧粮按比例逐步替换，减少软便概率。</p><p>最后提醒：没有“最好的猫粮”，只有适合你家猫年龄、体质和钱包的猫粮。</p>',
 '汪喵之家·科普组', 861, 54, 0, 1, '2026-10-21 10:00:00'),
(3, '从高架桥下到窗台：狸花猫团子的半年', 'http://localhost:8080/imgs/article/a03.jpg', 'STORY',
 '它守着一只翻倒的垃圾桶过冬，如今在领养人家里的飘台上晒太阳。',
 '<p>2026 年 4 月，志愿者在文一西路高架桥下第一次见到团子。那时它守着一只翻倒的垃圾桶，警惕地盯着每一个靠近的人。</p><p>投喂了两周后，大家发现它的肚子渐渐大了起来。救助站把它接回站里，5 月初顺利产下三只小猫。</p><p>幼猫两个月起陆续被领养，团子在完成绝育和免疫后，进入了待领养名单。</p><p>10 月底，一位住在城西的姑娘提交了申请。面谈那天她说：“我不需要一只黏人的猫，我只想给它一个安稳的地方。”</p><p>上周的第一次回访照片里，团子正趴在飘窗台上晒太阳，胖了一圈。</p>',
 '毛窝窝救助站·编辑部', 932, 76, 0, 1, '2026-11-08 15:00:00'),
(4, '犬猫疫苗时间表：什么时候打、打什么', 'http://localhost:8080/imgs/article/a04.jpg', 'KNOWLEDGE',
 '幼犬 45 日龄起首免，幼猫 8 周龄起首免，成年后每年加强一次。',
 '<p>疫苗是领养后第一年的头等大事，把时间表贴在冰箱上比记在心里更可靠。</p><p>幼猫：8 周龄首针猫三联，之后每 3-4 周一针，共 2-3 针；3 月龄以上可接种狂犬疫苗。</p><p>幼犬：45 日龄起接种犬二联/四联，每 2-3 周一针，共 3 针；3 月龄以上接种狂犬疫苗。</p><p>成年犬猫：每年加强一次联苗与狂犬疫苗。</p><p>注意事项：接种前完成体内外驱虫；接种后一周内不洗澡、减少外出；疫苗期间出现一过性精神变差属于常见现象，持续超过 48 小时需要就医。</p>',
 '汪喵之家·科普组', 715, 43, 0, 1, '2026-11-02 09:00:00'),
(5, '领养回家的第一周：笼位、作息与适应期', 'http://localhost:8080/imgs/article/a05.jpg', 'RAISE',
 '第一周的目标不是亲密，而是让它确认这里没有危险。',
 '<p>很多领养人担心“猫不亲人”，其实绝大多数问题出在第一周。</p><p>接猫回家前，准备一个独立房间：水、粮、猫砂盆、纸箱，四样东西齐了就可以。</p><p>前三天，除了添水换粮，尽量不打扰。它躲在床底不出来是正常现象，不要强行抱出。</p><p>第四天起，坐在房间里安静地看书或办公，让它习惯你的存在。零食的投放从“扔过去”过渡到“放在手心”。</p><p>一周后，如果它开始主动靠近，可以尝试轻抚下巴和头顶。切记：适应期用围栏或关门管理，不要让它接触整屋。</p><p>狗的适应期稍短，但原理相同：先建立“这里安全、吃饭有规律”，再谈训练。</p>',
 '汪喵之家·科普组', 654, 39, 0, 1, '2026-11-10 10:00:00'),
(6, '平台 10 月领养数据公示：37 只动物进入新家', 'http://localhost:8080/imgs/article/a06.jpg', 'NEWS',
 '10 月新增待领养档案 52 份，完成领养 37 只，其中猫 26 只、犬 10 只、兔 1 只。',
 '<p>以下为平台 2026 年 10 月运营数据公示，数据来源于各救助站提交的领养记录：</p><p>一、新增待领养宠物档案 52 份，其中猫 31 份、犬 19 份、其他 2 份。</p><p>二、完成领养 37 只，猫 26 只、犬 10 只、兔 1 只，领养完成率较 9 月提升 4.1 个百分点。</p><p>三、收到领养申请 214 份，通过初筛 96 份，通过率 44.9%，主要驳回原因为居住条件不符与过敏史。</p><p>四、收到流浪动物线索上报 63 条，其中 47 条已由救助站跟进处理。</p><p>五、累计登记志愿者 118 人，本月参与服务 273 人次。</p><p>感谢每一位救助站工作人员与志愿者的记录，数据透明是公益平台的底线。</p>',
 '平台运营组', 480, 22, 0, 1, '2026-11-01 09:00:00'),
(7, '一只后腿受伤的柯基，和三个志愿者的 42 天', 'http://localhost:8080/imgs/article/a07.jpg', 'STORY',
 '从无法站立到追着球跑，麦兜用了 42 天，志愿者们换了三班倒。',
 '<p>9 月中旬，麦兜被发现在双流一个小区的车库出口，后腿无法着地。首位发现人用纸箱把它送到汪喵之家。</p><p>X 光显示软组织挫伤，无骨折。医生的建议是：限制活动、理疗、观察。</p><p>“限制活动”对一只一岁半的柯基来说近乎酷刑。三位志愿者排了三班倒，白天有人陪它做康复训练，晚上有人带它回家看护。</p><p>第 10 天，它第一次四条腿站立吃完了整碗饭。第 28 天，它在走廊里小跑。第 42 天，复查通过，正式进入待领养名单。</p><p>现在麦兜的档案页面上，最常被收藏的照片是它追着网球跑的那一张。</p>',
 '汪喵之家·编辑部', 778, 61, 0, 1, '2026-11-12 14:00:00'),
(8, '绝育手术前后要注意什么：术前 3 天到术后 7 天', 'http://localhost:8080/imgs/article/a08.jpg', 'KNOWLEDGE',
 '术前禁食 8 小时、术后戴 7 天头套，绝育护理是一套完整流程。',
 '<p>本篇为待发布草稿，正文正在由兽医顾问审核中，预计下周发布。</p>',
 '汪喵之家·科普组', 0, 0, 0, 0, NULL);

INSERT INTO `comment` (`target_type`, `target_id`, `user_id`, `parent_id`, `content`, `like_count`, `status`, `create_time`) VALUES
('ARTICLE', 1, 2, 0, '第三条“猫砂盆位置”太真实了，放在洗衣机旁边它就是不肯用，挪到阳台立刻好了。', 12, 1, '2026-10-29 12:00:00'),
('ARTICLE', 1, 1, 0, '上周刚领养，独立房间这条亲测有效，第三天它就自己出来巡视了。', 8, 1, '2026-10-30 20:10:00'),
('ARTICLE', 3, 1, 0, '团子的故事看哭了，希望它在新家一切顺利。', 15, 1, '2026-11-09 08:30:00'),
('ARTICLE', 6, 1, 0, '数据公示很透明，支持！希望每月都能看到。', 5, 1, '2026-11-02 10:00:00'),
('ARTICLE', 4, 2, 0, '请问两针之间最多可以间隔多久？工作安排有冲突。', 3, 0, '2026-11-14 22:00:00'),
('PET', 1, 2, 0, '橘子看起来状态很好，申请表已经提交了，希望能通过初筛。', 6, 1, '2026-11-11 09:00:00');

INSERT INTO `favorite` (`user_id`, `target_type`, `target_id`, `create_time`) VALUES
(1, 'PET', 2, '2026-11-11 08:30:00'),
(1, 'PET', 7, '2026-11-12 21:00:00'),
(2, 'PET', 1, '2026-11-11 08:00:00'),
(2, 'PET', 3, '2026-11-09 19:00:00'),
(1, 'ARTICLE', 1, '2026-10-30 20:12:00');

INSERT INTO `notification` (`user_id`, `user_type`, `title`, `content`, `biz_type`, `biz_id`, `is_read`, `read_time`, `create_time`) VALUES
(1, 'USER', '领养申请已提交', '您的领养申请 APP20261110001（宠物：橘子）已提交，救助站将在 3 个工作日内完成初筛，可在“我的申请”中查看进度。', 'APPLICATION', 1, 0, NULL, '2026-11-10 20:12:00'),
(2, 'USER', '面谈时间已安排', '您的申请 APP20261108002（宠物：雪球）已通过初筛，面谈时间为 2026-11-18 14:00，地点：毛窝窝救助站（西湖区文一西路辅路），请提前 10 分钟到场。', 'APPLICATION', 2, 0, NULL, '2026-11-12 10:00:00'),
(1, 'USER', '第一次回访已完成', '大壮的第 1 次回访记录已通过救助站归档，第 2 次回访计划日期为 2026-12-05，届时可在“个人中心-我的回访”填写生活近况。', 'FOLLOW_UP', 1, 1, '2026-11-13 09:00:00', '2026-11-13 09:00:00'),
(1, 'USER', '欢迎加入平台', '欢迎注册流浪宠物领养平台。您可以先浏览“待领养”列表，了解领养流程后再提交申请。', 'SYSTEM', 0, 1, '2026-10-15 12:00:00', '2026-10-15 12:00:00'),
(2, 'SHELTER', '收到新的领养申请', '毛窝窝救助站：您有一份新的领养申请（APP20261110001，宠物：橘子）待受理，请及时处理。', 'APPLICATION', 1, 0, NULL, '2026-11-10 20:13:00'),
(1, 'ADMIN', '救助站入驻申请待审核', '阳光伴侣动物救助基地已提交入驻申请，请前往“机构审核”处理。', 'SHELTER', 3, 0, NULL, '2026-11-14 10:00:00');

-- =====================================================================================
-- 八、募捐 / 捐赠 / 志愿者 / 线索上报
-- =====================================================================================

INSERT INTO `donation_project` (`id`, `shelter_id`, `title`, `cover`, `target_amount`, `raised_amount`, `donor_count`, `start_date`, `end_date`, `status`, `content`) VALUES
(1, 1, '流浪猫冬季救助计划', 'http://localhost:8080/imgs/donation/d01.jpg', 20000.00, 8600.00, 23, '2026-11-01', '2026-12-31', 'ONGOING',
 '杭州入冬后，站内在册流浪猫的数量从 61 只增加到 88 只，取暖设备、猫粮与呼吸道疾病用药是本季度的三大开销。本次募集将全部用于：猫粮采购（约 60%）、取暖与保温设备（约 25%）、冬季高发疾病用药（约 15%）。募捐明细将按月在本项目页公示。'),
(2, 2, '受伤狗狗手术与康复费募集', 'http://localhost:8080/imgs/donation/d02.jpg', 15000.00, 5200.00, 12, '2026-10-20', '2026-11-30', 'ONGOING',
 '10 月以来，汪喵之家接收到 3 只需要手术的受伤犬只（车祸骨裂 1 只、软组织重伤 2 只）。手术与术后康复预计总费用 15000 元，包含手术费、住院费与复查费。每一笔费用都会附上医院单据编号，欢迎监督。');

INSERT INTO `donation_record` (`project_id`, `user_id`, `donor_name`, `amount`, `pay_no`, `pay_status`, `is_anonymous`, `message`, `pay_time`) VALUES
(1, 1,   '江小领',     200.00, 'PAY202611050001', 'SUCCESS', 0, '给站里的猫添点过冬的粮。', '2026-11-05 20:15:00'),
(1, 2,   '汪汪队友',   100.00, 'PAY202611060002', 'SUCCESS', 0, '', '2026-11-06 12:30:00'),
(1, NULL,'匿名爱心人士', 500.00, 'PAY202611080003', 'SUCCESS', 1, '心意为重，不具名。', '2026-11-08 09:00:00'),
(1, NULL,'爱心人士',   50.00,  'PAY202611100004', 'SUCCESS', 0, '', '2026-11-10 18:40:00'),
(1, 1,   '江小领',     88.00,  'PAY202611120005', 'SUCCESS', 0, '支持冬季救助计划。', '2026-11-12 08:20:00'),
(2, 2,   '汪汪队友',   300.00, 'PAY202610250006', 'SUCCESS', 0, '希望麦兜们都能跑起来。', '2026-10-25 21:00:00'),
(2, NULL,'匿名爱心人士', 1000.00,'PAY202610280007', 'SUCCESS', 1, '', '2026-10-28 11:00:00'),
(2, 1,   '江小领',     120.00, 'PAY202611010008', 'SUCCESS', 0, '手术顺利！', '2026-11-01 19:30:00');

INSERT INTO `volunteer_application` (`id`, `shelter_id`, `user_id`, `real_name`, `phone`, `skills`, `available_time`, `status`, `audit_remark`, `audit_time`) VALUES
(1, 1, 1, '江小领', '13800000001', '摄影、剪辑', '周末全天', 'APPROVED', '欢迎加入，拍摄任务请留意每周任务群通知。', '2026-10-20 10:00:00'),
(2, 1, 2, '汪汪队友', '13800000002', '宠物医疗辅助（持有宠物护理证）', '工作日晚间与节假日', 'PENDING', '', NULL);

INSERT INTO `rescue_report` (`id`, `report_no`, `user_id`, `category_id`, `description`, `images`, `province`, `city`, `district`, `address`, `longitude`, `latitude`, `urgency`, `status`, `handler_id`, `handle_remark`, `handle_time`, `create_time`) VALUES
(1, 'RSR20261112001', 1, 1, '文三路某小区东门绿化带有一只约 3 个月的小猫，非常瘦，不怕人，会跟着人走，附近无投喂点。',
 '["http://localhost:8080/imgs/report/r01.jpg"]', '浙江省', '杭州', '西湖区', '西湖区文三路东侧小区绿化带', 120.125430, 30.278910, 2, 'PENDING', NULL, '', NULL, '2026-11-12 18:30:00'),
(2, 'RSR20261108002', 2, 2, '武侯区某工地围挡内有一只后腿疑似受伤的中型犬，无法正常行走，请尽快安排处理。',
 '["http://localhost:8080/imgs/report/r02.jpg"]', '四川省', '成都', '武侯区', '武侯区永丰路附近工地围挡内', 104.043210, 30.612340, 3, 'PROCESSING', 3, '已由汪喵之家志愿者前往现场确认，正协助送医。', '2026-11-09 09:00:00', '2026-11-08 21:10:00'),
(3, 'RSR20261030003', 1, 1, '余杭塘河边有一只黑白花色的成年猫，疑似被遗弃，已在此徘徊一周。',
 '["http://localhost:8080/imgs/report/r03.jpg"]', '浙江省', '杭州', '拱墅区', '拱墅区余杭塘路河边步道', 120.110020, 30.298760, 1, 'RESOLVED', 2, '已由毛窝窝救助站接回站内，完成体检后进入待领养名单。', '2026-11-02 15:00:00', '2026-10-30 12:00:00');

-- =====================================================================================
-- 九、聊天会话（消息正文在 MongoDB chat_message，由后端启动初始化器写入演示消息）
-- =====================================================================================

INSERT INTO `chat_session` (`id`, `user_id`, `shelter_id`, `last_message`, `last_time`, `user_unread`, `shelter_unread`) VALUES
(1, 1, 1, '好的，周六上午我们等你们，记得带上封窗的照片。', '2026-11-15 20:31:00', 1, 0);

SET FOREIGN_KEY_CHECKS = 1;
