CREATE TABLE IF NOT EXISTS `t_user` (
  `uid` BIGINT(64) UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '登录名',
  `password` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '登录密码',
  `create_dt` datetime(3) DEFAULT NULL,
  `update_dt` datetime(3) DEFAULT NULL,
  `mobile` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
  `email` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `name` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '名字',
  `portrait_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像url',
  `status` tinyint(1) DEFAULT '0' COMMENT '用户状态: 0: 正常 1: 禁用',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `username_unique` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';