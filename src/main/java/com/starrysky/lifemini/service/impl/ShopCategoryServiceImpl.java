package com.starrysky.lifemini.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.constant.DataConstant;
import com.starrysky.lifemini.common.constant.FileConstant;
import com.starrysky.lifemini.common.constant.MessageConstant;
import com.starrysky.lifemini.common.util.ImageUtils;
import com.starrysky.lifemini.mapper.ShopMapper;
import com.starrysky.lifemini.model.dto.ShopCategoryDTO;
import com.starrysky.lifemini.model.dto.ShopCategorySimpleDTO;
import com.starrysky.lifemini.model.entity.Shop;
import com.starrysky.lifemini.model.entity.ShopCategory;
import com.starrysky.lifemini.mapper.ShopCategoryMapper;
import com.starrysky.lifemini.model.vo.ShopCategoryVO;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.FileService;
import com.starrysky.lifemini.service.IShopCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


/**
 * <p>
 * 商家分类表 服务实现类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopCategoryServiceImpl extends ServiceImpl<ShopCategoryMapper, ShopCategory> implements IShopCategoryService {
    private final ShopCategoryMapper shopCategoryMapper;
    private final ShopMapper shopMapper;
    private final FileService fileService;
    private final Executor dbExecutor;

    //查询商店所有分类
    @Override
    @Cacheable(//写入缓存
            cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SHOP_CATEGORY_NAME_CACHE
    )
    public List<ShopCategoryVO> queryShopCategories() {
        log.debug("查询商店所有分类");
        LambdaQueryWrapper<ShopCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(ShopCategory::getSort);
        List<ShopCategory> shopCategories = shopCategoryMapper.selectList(queryWrapper);
        List<ShopCategoryVO> vos = BeanUtil.copyToList(shopCategories, ShopCategoryVO.class);
        return vos;
    }


    @CacheEvict(//清空缓存
            cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SHOP_CATEGORY_NAME_CACHE,
            allEntries = true
    )
    @Override
    public Result updateShopImage(MultipartFile image, Long id) {
        log.info("{}商店分类图片，id={}", id == null ? "上传" : "修改", id);
        try {
            //1. 压缩
            byte[] imageBytes = ImageUtils.compressImage(image);//压缩图片
            //2. 直接上传，然后结束
            if (id == null) {
                //待确认
                String url = fileService.uploadFile(imageBytes, FileConstant.CATEGORY, image.getOriginalFilename(), true, 2);
                log.info("上传商店分类图片成功");
                return Result.success(url);
            }
            //3. 需要确认的上传
            ShopCategory category = shopCategoryMapper.selectById(id);
            String oldUrl = category.getIcon();
            String newUrl = fileService.uploadFile(imageBytes, FileConstant.CATEGORY, image.getOriginalFilename());
            try {
                //4. 修改原url为新的
                lambdaUpdate().set(newUrl != null, ShopCategory::getIcon, newUrl)
                        .eq(ShopCategory::getId, category.getId())
                        .update();
            } catch (Exception e) {
                log.error("上传商店分类图片失败");
                fileService.deleteFile(newUrl);
                return Result.error(MessageConstant.SHOP_CATE_IMAGE_UPDATE_FAILED);
            }
            //6. 删除旧封面
            if (StringUtils.isNotBlank(oldUrl)) {
                log.info("异步删除旧封面");
                CompletableFuture.runAsync(() -> {
                    try {
                        if (!DataConstant.DEFAULT_SHOP_IMAGE.equals(oldUrl)) {
                            fileService.deleteFile(oldUrl);
                        }
                    } catch (Exception ex) {
                        log.error("旧封面删除失败{}", oldUrl, ex);
                    }
                }, dbExecutor);
            }
            return Result.success(newUrl);
        } catch (IOException e) {
            log.error("修改商店封面失败");
            throw new RuntimeException(e);
        }
    }

    //新增商店分类
    @Override
    @CacheEvict(//清空缓存
            cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SHOP_CATEGORY_NAME_CACHE,
            allEntries = true
    )
    public Result addShopCategory(ShopCategoryDTO shopCategoryDTO) {
        log.debug("新增商店分类");
        String icon = shopCategoryDTO.getIcon();
        if (StrUtil.isNotBlank(icon)) {
            boolean notExpire = fileService.fileStatus(icon, FileConstant.CATEGORY);
            if (!notExpire) {
                log.info("商店分类图片超时未确认，使用默认图片");
                shopCategoryDTO.setIcon(DataConstant.DEFAULT_SHOP_IMAGE);
            }
        }
        ShopCategory shopCategory = BeanUtil.copyProperties(shopCategoryDTO, ShopCategory.class);
        save(shopCategory);
        return Result.success();
    }

    //删除指定商店分类
    @Override
    @CacheEvict(//清空缓存
            cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SHOP_CATEGORY_NAME_CACHE,
            allEntries = true
    )
    public Result deleteShopCategory(Long categoryId) {
        log.debug("删除指定商品分类");
        // TODO  删除指定商品分类
        Long count = shopMapper.selectCount(new LambdaQueryWrapper<Shop>().eq(Shop::getCategoryId, categoryId));
        if (count != null && !count.equals(0L)) {
            return Result.error(MessageConstant.CAN_NOT_DELETE_USED_CATEGORY);
        }
        ShopCategory category = getById(categoryId);
        if (category == null) {
            return Result.success(MessageConstant.CATEGORY_NOT_EXISTS);
        }

        removeById(categoryId);
        String url = category.getIcon();
        if (StrUtil.isNotBlank(url)) {
            log.info("删除分类图片");
            fileService.deleteFile(url);
        }
        return Result.success();
    }

    /**
     * 查询分类总数
     *
     * @return
     */
    @Override
    public Result<Long> categoryCount() {
        long count = count();
        return Result.success(count);
    }

    /**
     * 查询商店分类简单信息
     *
     * @return
     */
    @Override
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SIMPLE_CATEGORY)
    public List<ShopCategorySimpleDTO> queryShopCategorySimpleList() {
        List<ShopCategorySimpleDTO> list = shopCategoryMapper.queryShopCategorySimpleList();
        return list;
    }
}
