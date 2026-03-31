package com.starrysky.lifemini.controller.admin;


import com.starrysky.lifemini.common.annotation.CheckRole;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.model.dto.ShopCategoryDTO;
import com.starrysky.lifemini.model.vo.ShopCategoryVO;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.IShopCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@Tag(name = "（管理端）商店分类", description = "商店分类相关接口")
@RequestMapping("/admin/shop-category")
public class AdminShopCategoryController {
    @Autowired
    private IShopCategoryService shopCategoryService;

    @GetMapping("/all")
    @Operation(summary = "查询商店的所有分类")
    public Result<List<ShopCategoryVO>> queryShopCategories() {
        List<ShopCategoryVO> vos = shopCategoryService.queryShopCategories();
        return Result.success(vos);
    }

    @PostMapping("/upload/image")
    @Operation(summary = "上传分类图标")
    @CheckRole(RoleEnum.ADMIN)
    public Result<String> uploadImage(@RequestParam("image") MultipartFile image,
                                      @RequestParam(name = "id",required = false) Long id) {
        return shopCategoryService.updateShopImage(image, id);
    }

    @PostMapping("/add")
    @Operation(summary = "新增商店分类")
    @CheckRole(RoleEnum.ADMIN)
    public Result<Void> addShopCategory(@RequestBody @Valid ShopCategoryDTO shopCategoryDTO) {
        return shopCategoryService.addShopCategory(shopCategoryDTO);
    }

    @DeleteMapping("/delete/{categoryId}")
    @Operation(summary = "删除指定商店分类")
    @CheckRole(RoleEnum.ADMIN)
    public Result<Void> deleteShopCategory(@PathVariable Long categoryId) {
        return shopCategoryService.deleteShopCategory(categoryId);
    }

    @GetMapping("/count")
    @Operation(summary = "查询分类总数")
    public Result<Long> categoryCount() {
        return shopCategoryService.categoryCount();
    }

}
