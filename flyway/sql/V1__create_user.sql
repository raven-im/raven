CREATE TABLE IF NOT EXISTS `t_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `uid` varchar(36)  NOT NULL,
  `username` varchar(64) NOT NULL COMMENT '登录名',
  `password` varchar(64) NOT NULL COMMENT '登录密码',
  `create_dt` DATETIME NOT NULL,
  `update_dt` DATETIME NOT NULL,
  `mobile` varchar(16) NOT NULL DEFAULT "" COMMENT '电话',
  `email` varchar(64) NOT NULL DEFAULT "" COMMENT '邮箱',
  `name` varchar(16) NOT NULL DEFAULT "" COMMENT '名字',
  `portrait_url` varchar(1024) NOT NULL DEFAULT "" COMMENT '头像url',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '用户状态: 0: 正常 1: 禁用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';