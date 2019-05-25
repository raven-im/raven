CREATE TABLE IF NOT EXISTS `t_group_member` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `group_uid` varchar(36) NOT NULL COMMENT '群组Id',
  `user_uid` varchar(36) NOT NULL COMMENT '成员Id',
  `create_dt` DATETIME NOT NULL,
  `update_dt` DATETIME NOT NULL,
  `status` tinyint(1) NOT NULL DEFAULT "0" COMMENT '群成员状态: 0: 正常  2: 标记删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_member` (`group_uid`,`user_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组成员表';
