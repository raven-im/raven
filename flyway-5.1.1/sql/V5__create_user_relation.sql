CREATE TABLE IF NOT EXISTS `t_user_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `uid` BIGINT(64) UNSIGNED NOT NULL COMMENT 'uid',
  `friend_uid` BIGINT(64) UNSIGNED NOT NULL COMMENT '好友uid',
  `alias` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注名',
  `create_dt` datetime(3) DEFAULT NULL,
  `update_dt` datetime(3) DEFAULT NULL,
  `status` tinyint(1) DEFAULT NULL COMMENT '好友状态: 1: 正式好友 2: 请求待确认',
  PRIMARY KEY (`id`),
  UNIQUE KEY `relation_unique` (`uid`,`friend_uid`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';