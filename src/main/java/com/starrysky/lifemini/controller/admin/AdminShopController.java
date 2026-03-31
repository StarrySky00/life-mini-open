package com.starrysky.lifemini.controller.admin;


import com.starrysky.lifemini.common.annotation.CheckRole;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.model.dto.ShopDTO;
import com.starrysky.lifemini.model.dto.ShopUpdateDTO;
import com.starrysky.lifemini.model.vo.ShopVO;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.IShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/admin/shop")
@Tag(name = "（管理端）商店",description = "商店相关接口")
public class AdminShopController {
    @Autowired
    private IShopService shopService;
    @PostMapping("/add")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "新增商店")
    public Result<Void> addShop(@RequestBody @Valid ShopDTO dto) {
        return shopService.addShop(dto);
    }

    @PutMapping("/update")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "修改商店信息")
    public Result updateShopInfo(@RequestBody @Valid ShopUpdateDTO dto) {
        return shopService.updateShopInfo(dto);
    }

    @PostMapping("/update/image/{id}")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "修改商店封面图")
    public Result updateShopImage(@RequestParam("image") MultipartFile image,
                                  @PathVariable("id") Long id) {
        return shopService.updateShopImage(image, id);
    }

    @PostMapping("/disable/{id}")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "禁用商店")
    public Result disableShop(@PathVariable("id") Long shopId) {
        return shopService.disableShop(shopId);
    }

    @PostMapping("/enable/{id}")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "启用商店")
    public Result enableShop(@PathVariable("id") Long shopId) {
        return shopService.enableShop(shopId);
    }

    @GetMapping("/list")
    @Operation(summary = "根据商店条件查询所有商店（status为null查所有）")
    public Result<List<ShopVO>> queryShopList(@RequestParam(value = "status", required = false)
                                              Integer status,
                                              @RequestParam(value = "categoryId",required = false)
                                              Long categoryId) {
        return shopService.queryShopList(status,categoryId);
    }

    @GetMapping("/count")
    @Operation(summary = "根据商店状态查询商店数量（status为null查所有）")
    public Result<Long> getAllShopCount(@RequestParam(value = "status", required = false)
                                        Integer status) {
        return shopService.getAllShopCount(status);
    }

}
