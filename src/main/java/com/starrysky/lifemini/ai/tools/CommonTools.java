package com.starrysky.lifemini.ai.tools;

import com.starrysky.lifemini.service.IKeywordDictService;
import com.starrysky.lifemini.service.IShopCategoryService;
import com.starrysky.lifemini.service.IShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author StarrySky
 * @date 2026/4/24 10:28 星期五
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class CommonTools {
    private final IKeywordDictService keywordDictService;
    private final IShopCategoryService shopCategoryService;
    private final IShopService shopService;


    /**
     * 从商店信息或者评价信息中提取语义相近的上下文
     */
    @Tool(description = "【模糊语义搜索】当用户提出的需求比较主观、模糊（例如：安静适合学习的地方、情侣约会、菜品偏辣的店），无法用具体的分类和关键词精确定位时，调用此工具从向量库检索上下文。")
    public String queryBackInfo(@ToolParam(description = "用户输入的内容") String content) {
        log.debug("【AI.CommonTools】从商店信息或者评价信息中提取语义相近的上下文工具被调用，用户输入内容：{}", content);
       String docsStr= shopService.queryBackInfo(content);
        log.debug("【AI.CommonTools】从商店信息或者评价信息中提取语义相近的上下文结果：{}", docsStr);
        return docsStr;
    }

    /**
     * 查询评价可选的关键词列表
     * @return
     */

    @Tool(description = "【前置工具】当需要帮用户写评价（writeComment）或按关键词搜索商店前，必须先调用此工具获取合法可用的关键词ID列表。绝对不能凭空捏造ID。")
    public String queryKeyword() {
        log.debug("【AI.CommonTools】查询评价可选关键词列表工具被调用");
        String kwStr=keywordDictService.queryKeywordsStr();
        log.debug("【AI.CommonTools】查询评价可选关键词列表结果：{}", kwStr);
        return kwStr;
    }


    /**
     * 查询店铺的分类列表
     * @return
     */
    @Tool(description = "【前置工具】当需要搜索商店（searchShops）但不知道确切的分类ID时，必须先调用此工具获取分类字典。")
    public String queryShopCategory() {
        log.debug("【AI.CommonTools】查询店铺的分类列表工具被调用");
        String scStr=shopCategoryService.queryShopCategoryStr();
        log.debug("【AI.CommonTools】查询店铺的分类列表结果：{}", scStr);
        return scStr;
    }
}

