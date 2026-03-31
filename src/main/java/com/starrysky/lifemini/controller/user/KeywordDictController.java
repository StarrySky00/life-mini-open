package com.starrysky.lifemini.controller.user;


import com.starrysky.lifemini.model.entity.KeywordDict;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.IKeywordDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 关键词字典表【预设所有可选关键词，统一管理】 前端控制器
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@RestController
@RequestMapping("/keyword-dict")
@Tag(name = "（用户端）keyword",description = "keyword相关接口")
public class KeywordDictController {
    @Autowired
    private IKeywordDictService keywordDictService;
    @GetMapping("/list")
    @Operation(summary = "查询keyword列表")
    public Result<List<KeywordDict>> queryKeywords(){
        List<KeywordDict> keywordDicts = keywordDictService.queryKeywords();
        return Result.success(keywordDicts);
    }
    @GetMapping("/count")
    @Operation(summary = "查询keyword总数量")
    public Result<Long> keywordCount(){
        return keywordDictService.keywordCount();
    }

}
