package com.starrysky.lifemini.listener;

import com.starrysky.lifemini.model.event.DictChangeEvent;
import com.starrysky.lifemini.service.IKeywordDictService;
import com.starrysky.lifemini.service.IShopCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author StarrySky
 * @date 2026/4/24 21:24 星期五
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GlobalDictManager {

    // 直接以 String 形式常驻 JVM 内存
    private volatile String categoryDictContext = "";
    private volatile String keywordDictContext = "";

    private final IShopCategoryService shopCategoryService;
    private final IKeywordDictService keywordDictService;

    /**
     * 项目启动时，自动加载一次
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.debug("【AI 上下文】正在加载全局字典...");
        refreshContext();
    }
    /**
     * 刷新内存数据
     */
    public void refreshContext() {
        this.categoryDictContext = shopCategoryService.queryShopCategoryStr();
        this.keywordDictContext = keywordDictService.queryKeywordsStr();
        log.info("【AI 上下文】全局字典已加载至 JVM 内存");
    }

    // 当管理员在后台修改了分类或关键词时，发个事件，这里监听到就重新加载一次
    @EventListener       //TODO 添加发布事件
    public void onDictChange(DictChangeEvent event) {
        if ("category".equals(event.dictType())) {
            this.categoryDictContext = shopCategoryService.queryShopCategoryStr();
        }
        if ("keyword".equals(event.dictType())) {
            this.keywordDictContext = keywordDictService.queryKeywordsStr();
        }
    }

    public String getCategoryDict() {
        return categoryDictContext;
    }

    public String getKeywordDict() {
        return keywordDictContext;
    }
}
