package com.starrysky.lifemini.common.constant;

public class CacheConstant {
    /*
    缓存管理器
     */
    public static final String CAFFEINE_CACHE_MANAGER = "caffeineCacheManager";
    public static final String REDIS_CACHE_MANAGER = "redisCacheManager";

    /*
    缓存相关key
     */
    public static final String SHOP_CATEGORY_NAME_CACHE = "shopCateCache";
    public static final String SHOP_CACHE = "shopCache";
    public static final String SHOP_INFO_PRX="shop:info:";
    public static final String COMMENT_CACHE = "commentCache:";
    public static final String KEYWORD_DICT_CACHE = "keywordDictCache";

    public static final String GEO_KEY_PRX = "geo:location:";

    /*
     * 锁相关
     */
    public static final String SHOP_LOCK_PRX = "lock:shop:";
    public static final String CHAT_LOCK_PRX = "lock:chat:";
    /*
     *点赞，踩相关key
     */
    public static final String COMMENT = "comment:";
    public static final String COMMENT_ACTION = "action:";
    public static final String COMMENT_NUM = "num:";
    public static final String COMMENT_DIRTY = "dirty";

    /*
    * 验证码相关key
    * */
    public static final Object VERIFICATION_CODE_ADMIN = "verification:admin:";
    public static final Object VERIFICATION_CODE_USER = "verification:user:";
    public static final Object SMS_COUNT_SINGLE = "sms:count:single:";
    public static final Object SMS_COUNT_GLOBAL = "sms:count:global:";

    public static final String TOKEN = "token:";

    /*
     * ai提示词商店语关键词缓存
     */
    public static final String SIMPLE_KEYWORD = "simple:keyword";
    public static final String SIMPLE_CATEGORY = "simple:category";
    public static final String USER_LOCATION = "location:user:";
    public static final String LONGITUDE = "x";
    public static final String LATITUDE = "y";
    /*
    *对话警告次数限制
    */
    public static final String CHAT_WARN_PRX = "chat:warn:";
    public static final String CHAT_Limit_PRX = "chat:limit:";

    /*
     *统计数据使用（点击量，访问量）
     */
    /**
     * 点击量
     */
    public static final String STATS_PV ="stats:pv:";
    /**
     * 访问量
     */
    public static final String STATS_UV ="stats:uv:";
    /**
     * 总点击量
     */
    public static final String STATS_ALL_PV = "stats:all:pv";
    /**
     * Email发送验证码每日限制数量
     */
    public static final String EMAIL_CODE_LIMIT_ADMIN_PRX = "Email:code:admin:";
    public static final String EMAIL_CODE_LIMIT_USER_PRX = "Email:code:user:";
    public static final String MUTE_KEY = "lifemini:sys:mute";
    public static final String UPDATE_PASSWORD_ERROR = "update:password:error:";
}
