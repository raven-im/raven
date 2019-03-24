CREATE TABLE IF NOT EXISTS `t_group_member` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `group_uid`  varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '群组Id',
  `user_uid`  varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成员Id',
  `create_dt` datetime(3) DEFAULT NULL,
  `update_dt` datetime(3) DEFAULT NULL,
  `status` tinyint(1) DEFAULT NULL COMMENT '群成员状态: 1: 正式成员  2: 请求待确认',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_member_unique` (`group_uid`,`user_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组成员表';
