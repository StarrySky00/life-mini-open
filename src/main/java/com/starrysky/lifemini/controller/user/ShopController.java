package com.starrysky.lifemini.controller.user;


import com.starrysky.lifemini.model.dto.ShopPageQueryDTO;
import com.starrysky.lifemini.model.vo.ShopVO;
import com.starrysky.lifemini.model.result.PageResult;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.IShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 商家信息表 前端控制器
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@RestController
@RequestMapping("/shop")
@Tag(name = "（用户端）商店", description = "商店相关接口")
public class ShopController {
    @Autowired
    private IShopService shopService;

    @GetMapping("/queryShops/{sortType}")
    @Operation(summary = "根据条件查询商店信息",description = "(sortType  1：评分，2：距离)(分为两种排序方式，按距离升序排序和按评分降序排序。两种排序方式都可附带商店名称模糊查)")
    public PageResult<ShopVO> queryShopByCategory(@PathVariable("sortType")Integer sortType,
                                                  ShopPageQueryDTO dto) {
        return shopService.shopPageQuery(sortType,dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询商店信息")
    public Result<ShopVO> getShopById(@PathVariable("id") Long id) {
        return shopService.getShopById(id);
    }

    @GetMapping("/count")
    @Operation(summary = "根据商店类型查询商店数量，categoryId==null时查询商店总数")
    public Result<Long> getShopCountByCate(@RequestParam(value = "categoryId", required = false)
                                           Long categoryId) {
        return shopService.getShopCountByCate(categoryId);
    }

}
