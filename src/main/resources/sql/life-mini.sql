/* 活动表*/
CREATE TABLE `tb_activity` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动主键ID',
                               `shop_id` bigint NOT NULL COMMENT '【逻辑外键】关联商家ID',
                               `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动标题',
                               `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动详情',
                               `start_time` datetime NOT NULL COMMENT '活动开始时间',
                               `end_time` datetime NOT NULL COMMENT '活动结束时间',
                               `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1=有效 0=过期',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`) USING BTREE,
                               KEY `idx_shop_id` (`shop_id`) USING BTREE,
                               KEY `idx_status_time` (`status`,`start_time`,`end_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家活动表';

/*管理员信息表*/
CREATE TABLE `tb_admin` (
                            `id` bigint NOT NULL COMMENT '用户主键ID',
                            `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
                            `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(BCrypt加密存储)',
                            `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号(登录/注册唯一标识)',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            PRIMARY KEY (`id`) USING BTREE,
                            UNIQUE KEY `uk_phone` (`phone`) USING BTREE,
                            UNIQUE KEY `uk_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员信息表';


/*对话记录表*/
CREATE TABLE `tb_chat_message` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `user_id` bigint NOT NULL COMMENT '用户ID',
                                   `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话ID',
                                   `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色: user/assistant',
                                   `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   KEY `idx_user_conversation_time` (`user_id`,`conversation_id`,`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户对话记录表';



/*评价表*/

CREATE TABLE `tb_comment` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论主键ID',
                              `shop_id` bigint NOT NULL COMMENT '【逻辑外键】关联商家ID',
                              `user_id` bigint NOT NULL COMMENT '【逻辑外键】发布者用户ID',
                              `hidden` tinyint NOT NULL DEFAULT '1' COMMENT '是否隐藏评论。默认 1 ：展示。 0：隐藏',
                              `score` decimal(2,1) NOT NULL COMMENT '评分 1.0-5.0【只有评价需要打分，回复无评分】',
                              `content` varchar(800) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评价/回复的纯文本内容',
                              `like_num` int NOT NULL DEFAULT '0' COMMENT '点赞数',
                              `dislike_num` int NOT NULL DEFAULT '0' COMMENT '踩数',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                              `photo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商家封面图URL',
                              PRIMARY KEY (`id`) USING BTREE,
                              KEY `idx_user_id` (`user_id`) USING BTREE,
                              KEY `idx_shop_id_hidden` (`shop_id`,`hidden`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价回复表：0=评价(一级)，非0=回复(二级)，仅评价可绑定关键词';


/*关键词表*/
CREATE TABLE `tb_keyword_dict` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关键词主键ID【核心字段，AI查询用】',
                                   `keyword` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关键词名称(如：性价比高、口味偏辣、上菜快)',
                                   `type` tinyint NOT NULL DEFAULT '1' COMMENT '关键词分类 1=好评 2=差评 3=口味 4=通用',
                                   `sort` int NOT NULL DEFAULT '0' COMMENT '展示排序优先级，数值越小越靠前',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE KEY `uk_keyword` (`keyword`) USING BTREE COMMENT '关键词名称唯一，避免重复'
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关键词字典表【预设所有可选关键词，统一管理】';

/*待确认文件记录表*/
CREATE TABLE `tb_pending_uploads` (
                                      `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键，自增ID',
                                      `file_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件唯一标识（UUID，带连字符，如 a1b2c3d4-e5f6-...）',
                                      `file_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件在对象存储中的完整路径（用于删除或移动）',
                                      `status` tinyint NOT NULL DEFAULT '0' COMMENT '文件状态：0=待确认，1=已确认',
                                      `upload_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '文件上传时间',
                                      `expire_at` datetime NOT NULL COMMENT '过期时间（由应用层根据业务场景设置，如 upload_time + 24h）',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE KEY `uk_file_id` (`file_id`) USING BTREE,
                                      KEY `idx_cleanup` (`status`,`expire_at`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='临时上传文件记录表';


/*店铺表*/
CREATE TABLE `tb_shop` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商家主键ID',
                           `shop_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商家名称',
                           `category_id` bigint NOT NULL COMMENT '【逻辑外键】关联分类ID',
                           `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商家详细地址',
                           `longitude` double(10,7) DEFAULT NULL COMMENT '经度(位置搜索用)',
                           `latitude` double(10,7) DEFAULT NULL COMMENT '纬度(位置搜索用)',
                           `score` decimal(2,1) NOT NULL DEFAULT '0.0' COMMENT '商家综合评分 0.0-5.0',
                           `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商家联系电话',
                           `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商家封面图URL',
                           `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1=上架 0=下架',
                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`) USING BTREE,
                           KEY `idx_status` (`status`) USING BTREE,
                           KEY `idx_category_id_status_score` (`category_id`,`status`,`score`) USING BTREE,
                           KEY `tb_shop_create_time_index` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家信息表';


/*店铺分类*/
CREATE TABLE `tb_shop_category` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类主键ID',
                                    `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称(美食/药店/超市/快递站等)',
                                    `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分类图标URL',
                                    `sort` int NOT NULL DEFAULT '0' COMMENT '排序优先级，数值越小越靠前',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    UNIQUE KEY `uk_category_name` (`category_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家分类表';


/*关机才关联表*/
CREATE TABLE `tb_shop_keyword_relation` (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联表主键ID，自增',
                                            `keyword_id` bigint NOT NULL COMMENT '【逻辑外键】关联关键词字典表的主键ID',
                                            `shop_id` bigint NOT NULL COMMENT '【逻辑外键】关联商家表的主键ID',
                                            `comment_id` bigint NOT NULL COMMENT '【逻辑外键】关联评价的ID，溯源这条关键词是谁的评价勾选的',
                                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            PRIMARY KEY (`id`) USING BTREE,
                                            KEY `idx_keyword_id` (`keyword_id`) USING BTREE COMMENT '核心索引！AI根据关键词ID查店铺，极致性能',
                                            KEY `idx_shop_id` (`shop_id`) USING BTREE COMMENT '查某店铺的所有关键词标签',
                                            KEY `idx_comment_id` (`comment_id`) USING BTREE COMMENT '查某条评价勾选的所有关键词'
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='店铺关键词关联表：存储关键词与店铺的绑定关系，AI推荐核心依赖';


/*用户信息表*/
CREATE TABLE `tb_user` (
                           `id` bigint NOT NULL COMMENT '用户主键ID',
                           `openid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                           `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
                           `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '密码(BCrypt加密存储)',
                           `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号(登录/注册唯一标识)',
                           `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户头像URL',
                           `preferences` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户消费偏好(如：爱吃辣,预算50)',
                           `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1=正常 0=禁用',
                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`) USING BTREE,
                           UNIQUE KEY `uk_phone` (`phone`) USING BTREE,
                           UNIQUE KEY `openid` (`openid`) USING BTREE,
                           KEY `tb_user_create_time_index` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';


/*用户收藏*/
CREATE TABLE `tb_user_collect` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏主键ID',
                                   `user_id` bigint NOT NULL COMMENT '【逻辑外键】收藏的用户ID',
                                   `shop_id` bigint NOT NULL COMMENT '【逻辑外键】被收藏的商家ID',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE KEY `uk_user_shop` (`user_id`,`shop_id`) USING BTREE COMMENT '联合唯一，避免用户重复收藏同一商家'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏商家表';