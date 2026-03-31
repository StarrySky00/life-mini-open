package com.starrysky.lifemini.controller.admin;


import com.starrysky.lifemini.common.annotation.CheckRole;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.model.dto.KeywordDictDTO;
import com.starrysky.lifemini.model.entity.KeywordDict;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.IKeywordDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/keyword")
@Tag(name = "（管理端）keyword",description = "keyword相关接口")
public class AdminKeywordDictController {

    @Autowired
    private IKeywordDictService keywordDictService;
    @GetMapping("/list")
    @Operation(summary = "查询keyword列表")
    public Result<List<KeywordDict>> queryKeywords(){
        List<KeywordDict> keywordDicts = keywordDictService.queryKeywords();
        return Result.success(keywordDicts);
    }

    @PostMapping("/add")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "新增keyword")
    public Result<Void> addKeyword(@RequestBody KeywordDictDTO dto){
        return keywordDictService.addKeyword(dto);
    }

    @DeleteMapping("/delete/{keywordId}")
    @Operation(summary = "删除keyword")
    @CheckRole(RoleEnum.ADMIN)
    public Result<Void> deleteKeyword(@PathVariable("keywordId") Long keywordId){
        return keywordDictService.deleteKeyword(keywordId);
    }
    @GetMapping("/count")
    @Operation(summary = "查询keyword总数量")
    public Result<Long> keywordCount(){
        return keywordDictService.keywordCount();
    }

}
