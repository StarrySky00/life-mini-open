package com.starrysky.lifemini.controller.user;

import com.starrysky.lifemini.model.vo.ShopCategoryVO;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.IShopCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 商家分类表 前端控制器
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@RestController
@Tag(name = "（用户端）商店分类",description = "商店分类相关接口")
@RequestMapping("/shop-category")
public class ShopCategoryController {
    @Autowired
    private IShopCategoryService shopCategoryService;
    @GetMapping("/all")
    @Operation(summary = "查询商店的所有分类")
    public Result<List<ShopCategoryVO>> queryShopCategories(){
        List<ShopCategoryVO> vos = shopCategoryService.queryShopCategories();
        return Result.success(vos);
    }
    @GetMapping("/count")
    @Operation(summary = "查询分类总数")
    public Result<Long> categoryCount(){
        return shopCategoryService.categoryCount();
    }

}
