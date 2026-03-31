package com.starrysky.lifemini.service;

import com.starrysky.lifemini.model.dto.KeywordDictDTO;
import com.starrysky.lifemini.model.dto.KeywordSimpleDTO;
import com.starrysky.lifemini.model.entity.KeywordDict;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starrysky.lifemini.model.result.Result;

import java.util.List;

/**
 * <p>
 * 关键词字典表【预设所有可选关键词，统一管理】 服务类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
public interface IKeywordDictService extends IService<KeywordDict> {

    //查询keyword列表
    List<KeywordDict> queryKeywords();

    //新增keyword
    Result<Void> addKeyword(KeywordDictDTO dto);

    //删除keyword
    Result<Void> deleteKeyword(Long keywordIds);

    //查询keyword总数量
    Result<Long> keywordCount();
    //查询keyword简单信息
    List<KeywordSimpleDTO> querySimpleKeywordList();
}
