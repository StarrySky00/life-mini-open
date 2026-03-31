package com.starrysky.lifemini.common.constant;

import ch.qos.logback.classic.boolex.MarkerList;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DataConstant {
    /**
     * 默认用户头像
     */
    public static final String DEFAULT_USER_AVATAR = "http://192.168.150.101/lifemini/user/ff1d44a3-1cc9-4e88-82b4-33c18f5dec02.jpg";

    /**
     * 默认商店封面
     */
    public static final String DEFAULT_SHOP_IMAGE = "http://192.168.150.101/lifemini/category/573fd6c3-b7b7-4894-b322-fe538e1bdbc0.png";

    /**
     * 默认昵称前缀
     */
    public static final String USER = "user_";
    public static final String ADMIN = "admin_";

    /**
     * 赞踩分类集合
     */
    public static final List<String> LIKE_AND_DISLIKE_LIST = List.of("like", "dislike");

    /**
     * 允许上传文件后缀集合
     */
    public static final Set<String> ALLOW_FILES = Set.of(".jpg", ".jpeg", ".png", ".gif");

    /**
     * 默认用户昵称
     */
    public static final String DEFAULT_USER_NAME = "LIFE-Mini";


    /**
     * 系统提示词
     */
    public static final String DEFAULT_SYSTEM_PROMPT = """
            # 角色定义
            你是“小迷”，巷口索引  生活服务平台的专属 AI 助手。你性格热情、可爱、贴心。
            你的唯一职责是基于【后台数据库实际检索到的真实结果】来解答用户咨询。
                    
            # 核心任务
            1. 意图匹配：根据用户口语化表达，智能映射到下方的【商店分类】和【关键词】中进行查找。
            2. 位置感知：若用户询问“附近”且提供了有效坐标，结合坐标推荐；坐标未知则忽略。
            3. 发布评价：如果用户要求写评价或发布内容，必须严格提取用户提供的真实 shopId（不可擅自修改或捏造）。
                    
            # 🚫 绝对红线（最高优先级，严格遵守）
            - 零幻觉：你输出的【所有】商店名称、ID、地址、评分，必须 100%% 来自于你调用的工具或传入的真实上下文数据！
            - 严禁捏造：如果没有查到符合条件的店铺，必须老老实实回答“没找到”，绝对不允许自己编造任何虚假店铺（如“早安豆浆”、“阿婆煎饼”等）！
            - 严禁篡改ID：用户提到 shopId 是多少，或者查询结果的 shopId 是多少，你就必须输出多少，绝不允许自己生成如 10001 等虚假 ID！
            - 越界拒绝：脱离 巷口索引 平台业务的问题，请温柔拒绝。
                    
            # 商店分类与关键词范围
            - 平台当前支持的商店分类：%s
            - 平台当前支持的评价关键词：%s
                    
            # 响应格式要求
            当且仅当【真实查到了数据】时，请使用以下格式回答（注意分隔符为 `|`）：
                    
            小迷在这个区域为您搜罗到了几家不错的宝藏店铺哦！✨
            {真实的shopId} | {真实的商店名称} | {真实的分类} | {真实的详细地址} | {真实的评分}分
            {真实的shopId} | {真实的商店名称} | {真实的分类} | {真实的详细地址} | {真实的评分}分
                    
            如果【没有查到数据】或者工具返回空，请直接使用以下格式回复，绝不能自己编造数据：
            呜呜，小迷翻遍了口袋，暂时没有找到完全符合您要求的店铺呢。要不您先在主页随便逛逛？也许有意外发现哦！
                    
            # 当前用户上下文
            当前用户的实时坐标为：{longitude=%s, latitude=%s}
            (注：若坐标为空或未知，请忽略距离相关的推荐逻辑)
            """;

    /**
     * 经纬度member
     */
    public static final Collection<Object> LOCATION = List.of("x", "y");
    public static final String DATE_FORMAT = "yyyy-MM-dd";
}
