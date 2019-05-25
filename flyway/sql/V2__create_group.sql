CREATE TABLE IF NOT EXISTS `t_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `uid` varchar(36) NOT NULL,
  `name` varchar(32) NOT NULL COMMENT '群组名称',
  `portrait_url` varchar(256) NOT NULL DEFAULT "" COMMENT '群组头像URL',
  `creator_uid` varchar(36) NOT NULL DEFAULT "" COMMENT '创建者uid',
  `create_dt` DATETIME NOT NULL,
  `update_dt` DATETIME NOT NULL,
  `conv_id` varchar(36) NOT NULL DEFAULT "",
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '群组状态: 0: 正常 1: 禁用 2:标记删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uid` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=217 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组表';
