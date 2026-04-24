package com.starrysky.lifemini.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.mapper.ShopMapper;
import com.starrysky.lifemini.model.dto.KeywordDictDTO;
import com.starrysky.lifemini.model.dto.KeywordSimpleDTO;
import com.starrysky.lifemini.model.entity.KeywordDict;
import com.starrysky.lifemini.mapper.KeywordDictMapper;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.IKeywordDictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 关键词字典表【预设所有可选关键词，统一管理】 服务实现类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KeywordDictServiceImpl extends ServiceImpl<KeywordDictMapper, KeywordDict> implements IKeywordDictService {

    private final ShopMapper shopMapper;
    private final KeywordDictMapper keywordDictMapper;
    @Autowired
    @Lazy
    private IKeywordDictService keywordDictService;

    /**
     * 查询keyword列表
     *
     * @return
     */
    @Cacheable(cacheManager = CacheConstant.CAFFEINE_CACHE_MANAGER,
            cacheNames = CacheConstant.KEYWORD_DICT_CACHE
    )
    @Override
    public List<KeywordDict> queryKeywords() {
        log.info("查询keyword列表");
        List<KeywordDict> keywords = lambdaQuery().list();
        return keywords;
    }

    /**
     * 新增keyword
     *
     * @param dto
     * @return
     */
    @Caching(
            evict = {
                    @CacheEvict(cacheManager = CacheConstant.CAFFEINE_CACHE_MANAGER,
                            cacheNames = CacheConstant.KEYWORD_DICT_CACHE,
                            allEntries = true),
                    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
                            cacheNames = CacheConstant.SIMPLE_KEYWORD,
                            allEntries = true)
            }
    )
    @Override
    public Result addKeyword(KeywordDictDTO dto) {
        log.info("新增keyword：{}", dto);
        save(BeanUtil.copyProperties(dto, KeywordDict.class));
        return Result.success();
    }

    /**
     * 删除keyword
     *
     * @param keywordId
     * @return
     */
    @Override
    @Caching(
            evict = {
                    @CacheEvict(cacheManager = CacheConstant.CAFFEINE_CACHE_MANAGER,
                            cacheNames = CacheConstant.KEYWORD_DICT_CACHE,
                            allEntries = true),
                    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
                            cacheNames = CacheConstant.SIMPLE_KEYWORD,
                            allEntries = true)
            }
    )
    @Transactional
    public Result deleteKeyword(Long keywordId) {
        log.info("删除keyword：{}", keywordId);
        shopMapper.deleteShopKeyWord(keywordId);
        keywordDictMapper.deleteByKeywordId(keywordId);
        return Result.success();
    }

    /**
     * 查询keyword总数量
     *
     * @return
     */
    @Override
    public Result<Long> keywordCount() {
        long count = count();
        return Result.success(count);
    }

    /**
     * 查询keyword简单信息
     *
     * @return
     */
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SIMPLE_KEYWORD
    )
    @Override
    public List<KeywordSimpleDTO> querySimpleKeywordList() {
        List<KeywordSimpleDTO> list = keywordDictMapper.queryKeywordSimpleList();
        return list;
    }

    /**
     * 查询keyword列表
     *
     * @return
     */
    @Override
    public String queryKeywordsStr() {
        List<KeywordSimpleDTO> kws = keywordDictService.querySimpleKeywordList();
        // 用{id,keyword}的格式返回给大模型看
        StringBuilder sb = new StringBuilder("关键词列表格式{keywordId：keyword},列表为：");
        for (KeywordSimpleDTO kw : kws) {
            sb.append("{").append(+kw.getId()).append("：").append(kw.getKeyword()).append("},");
        }
        sb.replace(sb.length() - 1, sb.length(), "。");//去掉最后一个逗号
        return sb.toString();
    }
}
