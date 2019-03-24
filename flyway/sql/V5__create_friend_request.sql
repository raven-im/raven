CREATE TABLE IF NOT EXISTS `t_friend_request` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `from_uid`  varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发起申请uid',
  `to_uid`  varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '被申请uid',
  `create_dt` datetime(3) DEFAULT NULL,
  `update_dt` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `from_uid_key` (`from_uid`),
  KEY `to_uid_key` (`to_uid`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表';