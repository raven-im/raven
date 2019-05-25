CREATE TABLE IF NOT EXISTS `t_app_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `uid` varchar(36) NOT NULL COMMENT 'App Key',
  `secret`  varchar(36) NOT NULL COMMENT 'App Secret',
  `create_dt` DATETIME NOT NULL,
  `update_dt` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='App Config表';
