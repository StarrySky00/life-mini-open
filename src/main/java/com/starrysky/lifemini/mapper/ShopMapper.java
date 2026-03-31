package com.starrysky.lifemini.mapper;

import com.starrysky.lifemini.model.dto.ShopMatchDTO;
import com.starrysky.lifemini.model.dto.ShopPageQueryDTO;
import com.starrysky.lifemini.model.entity.Shop;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starrysky.lifemini.model.query.ShopQuery;
import com.starrysky.lifemini.model.vo.ConsoleExcelVO;
import com.starrysky.lifemini.model.vo.ShopSearchVO;
import com.starrysky.lifemini.model.vo.ShopVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 商家信息表 Mapper 接口
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
public interface ShopMapper extends BaseMapper<Shop> {

    List<ShopVO> queryShopByCategory(@Param("shopName") String shopName,
                                     @Param("categoryId") Long categoryId,
                                     @Param("pageStart") Integer pageStart,
                                     @Param("pageSize") Integer pageSize);

    @Update("""
            update tb_shop
            set score =(
                select coalesce(avg(score),0.0)
                from tb_comment
                where shop_id = #{shopId}
                    and hidden = 1
            )
            where id = #{shopId}
            """)
    boolean updateShopAvgScore(@Param("shopId") Long shopId);


    boolean saveShopKeyword(@Param("keywords") List<Integer> keywords,
                            @Param("commentId") Long commentId,
                            @Param("shopId") Long shopId);

    @Delete("""
            delete from tb_shop_keyword_relation
            where shop_id = #{shopId}
                and comment_id = #{commentId}
            """)
    boolean deleteShopKeyword(@Param("shopId") Long shopId,
                              @Param("commentId") Long commentId);

    @Delete("delete from tb_shop_keyword_relation where keyword_id = #{keywordId}")
    boolean deleteShopKeyWord(@Param("keywordId") Long keywordIds);

    @Update("update tb_shop set status = #{status} where id = #{id}")
    boolean updateShopStatusById(@Param("id") Long shopId,
                                 @Param("status") Integer status);

    List<ShopVO> queryShopByStatus(@Param("status") Integer status,
                                   @Param("categoryId") Long categoryId);


    List<ShopMatchDTO> filterShopsByGeoAndKeywords(@Param("categoryId") Long categoryId,
                                                   @Param("keywordIds") List<Long> keywordIds,
                                                   @Param("candidateIds") List<Long> candidateIds,
                                                   @Param("status") Integer status);

    List<ShopMatchDTO> searchByCategoryAndKeywords(@Param("categoryId") Long categoryId,
                                                   @Param("keywordIds") List<Long> keywordIds,
                                                   @Param("status") Integer status);

    List<Long> searchByCategoryOnly(@Param("categoryId") Long categoryId,
                                    @Param("status") Integer status,
                                    @Param("limit") Integer limit);

    //批量查询商店信息和分类名称
    List<ShopVO> queryShopAndCategoryNameByIds(@Param("missIds") List<Long> missIds);

    @Delete("delete from tb_shop_keyword_relation where comment_id = #{commentId}")
    void deleteKeywordRelation(Long commentId);

    @Select(("""
            select 
            sc.category_name as `name`
            ,count(s.id) as `count`
            from tb_shop s
            left join tb_shop_category sc on s.category_id=sc.id
            group by sc.id
            """))
    List<ConsoleExcelVO.CategoryStats> fetchCategoryStatsList();
}
