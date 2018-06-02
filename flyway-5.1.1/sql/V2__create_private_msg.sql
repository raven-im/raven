CREATE TABLE IF NOT EXISTS `t_private_msg` (
  `msg_uid` BIGINT(64) UNSIGNED NOT NULL COMMENT '消息uid',
  `content` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息内容',
  `send_uid` BIGINT(64) UNSIGNED NOT NULL COMMENT '发送者uid',
  `receive_uid` BIGINT(64) UNSIGNED NOT NULL COMMENT '发送者uid',
  `send_time` datetime(3) DEFAULT NULL COMMENT '发送时间',
  `type` int(11) DEFAULT '0' COMMENT '消息类型',
  PRIMARY KEY (`msg_uid`),
  KEY `key_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COLLATE=utf8mb4_unicode_ci COMMENT='单聊消息表';