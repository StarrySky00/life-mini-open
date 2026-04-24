package com.starrysky.lifemini.mapper;

import com.starrysky.lifemini.model.dto.CommentKeywordRelation;
import com.starrysky.lifemini.model.dto.KeywordSimpleDTO;
import com.starrysky.lifemini.model.entity.KeywordDict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 关键词字典表【预设所有可选关键词，统一管理】 Mapper 接口
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
public interface KeywordDictMapper extends BaseMapper<KeywordDict> {

    @Delete("delete from tb_keyword_dict where id = #{keywordId} ")
    void deleteByKeywordId(@Param("keywordId") Long keywordId);

    @Select("select id,keyword from tb_keyword_dict order by id")
    List<KeywordSimpleDTO> queryKeywordSimpleList();

    List<CommentKeywordRelation> quertAllrelationInfo();
}
