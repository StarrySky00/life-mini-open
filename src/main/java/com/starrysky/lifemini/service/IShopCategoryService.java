package com.starrysky.lifemini.service;

import com.starrysky.lifemini.model.dto.ShopCategoryDTO;
import com.starrysky.lifemini.model.dto.ShopCategorySimpleDTO;
import com.starrysky.lifemini.model.entity.ShopCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starrysky.lifemini.model.vo.ShopCategoryVO;
import com.starrysky.lifemini.model.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 商家分类表 服务类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
public interface IShopCategoryService extends IService<ShopCategory> {

    /**
     * 查询商店的所有分类
     * @return 商店所有分类
     */
    List<ShopCategoryVO> queryShopCategories();
    //上传或修改分类图标
    Result<String> updateShopImage(MultipartFile image, Long id);

    /**
     * 新增店铺分类
     * @param shopCategoryDTO 参数
     */
    Result<Void> addShopCategory(ShopCategoryDTO shopCategoryDTO);

    /**
     * 删除指定商品分类
     * @param categoryId 要删除的商品分类
     */
    Result<Void> deleteShopCategory(Long categoryId);

    //查询分类总数
    Result<Long> categoryCount();

    //查询分类的简单信息
    List<ShopCategorySimpleDTO> queryShopCategorySimpleList();
}
