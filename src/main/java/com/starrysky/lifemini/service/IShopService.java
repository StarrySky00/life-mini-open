package com.starrysky.lifemini.service;

import com.starrysky.lifemini.model.dto.ShopDTO;
import com.starrysky.lifemini.model.dto.ShopPageQueryDTO;
import com.starrysky.lifemini.model.dto.ShopUpdateDTO;
import com.starrysky.lifemini.model.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starrysky.lifemini.model.vo.ShopVO;
import com.starrysky.lifemini.model.result.PageResult;
import com.starrysky.lifemini.model.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商家信息表 服务类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
public interface IShopService extends IService<Shop> {

    //根据 商店分类 分页查询 商店信息
    PageResult<ShopVO> shopPageQuery(Integer sortType,ShopPageQueryDTO dto);
    //根据商店id查询商店信息（缓存+DB）
    Map<Long, ShopVO> getShopVoBatchMap(List<Long> ids);
    List<ShopVO> getShopVoBatchList(List<Long> ids);

    //查询商店数量
    Result<Long> getShopCountByCate(Long categoryId);

    //新增商铺
    Result<Void> addShop(ShopDTO dto);

    //修改商店信息
    Result updateShopInfo(ShopUpdateDTO dto);

    //修改商店封面
    Result updateShopImage(MultipartFile file, Long id);

    //根据id查询商店信息
    Result<ShopVO> getShopById(Long id);

    //禁用商店
    Result disableShop(Long shopId);

    //启用商店
    Result enableShop(Long shopId);

    //根据条件查询所有商店
    Result<List<ShopVO>> queryShopList(Integer status,Long categoryId);

    //查询所有商店的数量
    Result<Long> getAllShopCount(Integer status);

    //根据分类分页查询商店
    List<ShopVO> queryShopByCategory(ShopPageQueryDTO dto);
}
