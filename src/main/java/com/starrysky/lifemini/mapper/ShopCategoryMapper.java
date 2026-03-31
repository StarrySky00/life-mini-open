package com.starrysky.lifemini.mapper;

import com.starrysky.lifemini.model.dto.ShopCategorySimpleDTO;
import com.starrysky.lifemini.model.entity.ShopCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 商家分类表 Mapper 接口
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
public interface ShopCategoryMapper extends BaseMapper<ShopCategory> {

    @Select("select id ,category_name from tb_shop_category order by id")
    List<ShopCategorySimpleDTO> queryShopCategorySimpleList();
}
