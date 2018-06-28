CREATE TABLE IF NOT EXISTS `t_friend_request_msg` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `request_id` int(11) NOT NULL  COMMENT '请求id',
  `from_uid`  varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'from uid',
  `to_uid`  varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'to uid',
  `message` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请信息或回复',
  `create_dt` datetime(3) DEFAULT NULL,
  `update_dt` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `request_id_key` (`request_id`),
  FOREIGN KEY(request_id) REFERENCES t_friend_request(id) on delete cascade on update cascade
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请信息表';