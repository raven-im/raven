CREATE TABLE IF NOT EXISTS `t_friend` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `uid`  varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'uid',
  `friend_uid`  varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '好友uid',
  `alias` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注名',
  `create_dt` datetime(3) DEFAULT NULL,
  `update_dt` datetime(3) DEFAULT NULL,
  `state` tinyint(1) DEFAULT 0 COMMENT '好友状态: 0: 正式好友 1: 黑名单',
  PRIMARY KEY (`id`),
  UNIQUE KEY `relation_unique` (`uid`,`friend_uid`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';