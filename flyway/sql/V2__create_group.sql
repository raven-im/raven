CREATE TABLE IF NOT EXISTS `t_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `uid` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '群组名称',
  `portrait_url` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '群组头像URL',
  `creator_uid` varchar(36) DEFAULT NULL COMMENT '创建者uid',
  `create_dt` datetime(3) DEFAULT NULL,
  `update_dt` datetime(3) DEFAULT NULL,
  `status` tinyint(1) DEFAULT '0' COMMENT '用户状态: 0: 正常 1: 禁用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid_unique` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=217 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组表';
