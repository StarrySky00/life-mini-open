package com.starrysky.lifemini.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.constant.DataConstant;
import com.starrysky.lifemini.common.constant.FileConstant;
import com.starrysky.lifemini.common.constant.MessageConstant;
import com.starrysky.lifemini.common.enums.StatusEnum;
import com.starrysky.lifemini.common.util.ImageUtils;
import com.starrysky.lifemini.mapper.KeywordDictMapper;
import com.starrysky.lifemini.mapper.ShopMapper;
import com.starrysky.lifemini.mapper.UserMapper;
import com.starrysky.lifemini.model.dto.AiCommentDTO;
import com.starrysky.lifemini.model.dto.CommentDTO;
import com.starrysky.lifemini.model.dto.PageQueryDTO;
import com.starrysky.lifemini.model.dto.UserInfoDTO;
import com.starrysky.lifemini.model.entity.Comment;
import com.starrysky.lifemini.mapper.CommentMapper;
import com.starrysky.lifemini.model.entity.Shop;
import com.starrysky.lifemini.model.result.PageResult;
import com.starrysky.lifemini.model.vo.CommentAdminVO;
import com.starrysky.lifemini.model.vo.CommentVO;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starrysky.lifemini.common.util.SensitiveWordUtil;
import com.starrysky.lifemini.common.util.ThreadLocalUtil;
import com.starrysky.lifemini.common.util.TypeConversionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.checkerframework.checker.units.qual.A;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>
 * 评价回复表：0=评价(一级)，非0=回复(二级)，仅评价可绑定关键词 服务实现类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final ShopMapper shopMapper;
    private final CommentMapper commentMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final FileService fileService;
    private final UserMapper userMapper;
    private final Executor cacheExecutor;
    private final IUserService userService;
    private final IWeChatService weChatService;
    private final VectorService vectorService;
    private final Executor vectorExecutor;

    @Autowired
    @Lazy
    private ICommentService commentService;
    @Autowired
    private KeywordDictMapper keywordDictMapper;


    /**
     * 上传评价照片
     *
     * @param photo
     * @return
     */
    @Override
    public Result uploadPhoto(MultipartFile photo) {
        try {
            log.info("上传评价图片");
            byte[] imageBytes = ImageUtils.compressImage(photo);
            if (!weChatService.checkImage(imageBytes)) {
                Long userId = ThreadLocalUtil.getUserId();
                log.info("用户{}企图上传违规评价图片",userId);
                userService.banUserAndForcedOffline(userId);//上传违规图片，直接封禁
                return Result.error(MessageConstant.IMAGE_VIOLATION);
            }
            String url = fileService.uploadFile(imageBytes, FileConstant.COMMENT, photo.getOriginalFilename(), true, 1);
            if (url == null) {
                return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
            }
            return Result.success(MessageConstant.FILE_UPLOAD + MessageConstant.SUCCESS, url);

        } catch (Exception e) {
            log.error("文件上传失败,{}", e.getMessage());
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
        }
    }

    /**
     * 新增评价
     *
     * @param dto
     * @return 评论id
     */
    @Override
    @Transactional
    public Result addComment(CommentDTO dto) {
        log.info("新增评论/评价 {}。。。", dto);
        //1. 获取用户
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null || userId == 0) {
            log.error("未获取到用户id");
            return Result.error(MessageConstant.TAKE_USER_INFO_FAILED);
        }

        //2. 验证评论内容是否正常
        Long limit = stringRedisTemplate.opsForValue().increment("check:comment:" + userId);
        if (limit != null && limit == 1) {
            stringRedisTemplate.expire("check:comment:" + userId, 24, TimeUnit.HOURS);
        }
        if (limit != null && limit > 15) {
            return Result.error("每日评价数达到上限");
        }
        if (!weChatService.checkContent(dto.getContent())) {
            Long illegalTimes = stringRedisTemplate.opsForValue().increment("check:illegal:comment" + userId);
            if (illegalTimes != null && illegalTimes == 1) {
                stringRedisTemplate.expire("check:illegal:comment" + userId, 7, TimeUnit.DAYS);
            }
            if (illegalTimes != null && illegalTimes > 3) {
                log.debug("用户{}一周内多次发布违规评价，已封禁",userId);
                userService.banUserAndForcedOffline(userId);
            }
            return Result.error("评论内容违规！评论失败,一周内多次违规将封号");
        }

        //3. 判断图片是否过期
        String photoUrl = dto.getPhotoUrl();
        if (StrUtil.isNotBlank(photoUrl)) {
            //2.1 图片状态（未过期则确认图片）
            boolean notExpire = fileService.fileStatus(photoUrl, FileConstant.COMMENT);
            if (!notExpire) {
                return Result.error(MessageConstant.PHOTO_EXPIRED_RE_UPLOAD);
            }
        } else {
            dto.setPhotoUrl(null);
        }

        //4. 保存评价
        Comment comment = BeanUtil.copyProperties(dto, Comment.class);
        comment.setUserId(userId);
        comment.setHidden(StatusEnum.ENABLE.getId());
        save(comment);
        log.info("新增评价，更新商店评分，并存储keyword");
        //4.1 计算平均评分
        shopMapper.updateShopAvgScore(dto.getShopId());
        //4.2 存储keyword与商店关系表
        saveShopKeyword(dto.getKeywords(), comment.getId(), comment.getShopId());
        //4.3 异步清除缓存
        Long shopId = comment.getShopId();
        syncCleanCommentCache(shopId);

        // 5. 异步保存向量
        CompletableFuture.runAsync(() -> {
            log.debug("【保存评价向量】");
            Shop shop = shopMapper.selectById(shopId);
            List<String> kws = null;
            if (dto.getKeywords() != null) {
                kws = keywordDictMapper.queryKeywordListByIds(dto.getKeywords());
            }
            vectorService.saveCommentVector(comment, shop.getShopName(), shop.getCategoryId(), kws);
        }, vectorExecutor);
        return Result.success(comment.getId());
    }

    private void saveShopKeyword(List<Integer> keywords, Long commentId, Long shopId) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }
        if (keywords.size() > 5) {
            keywords = CollUtil.sub(keywords, 0, 5);
        }
        shopMapper.saveShopKeyword(keywords, commentId, shopId);
    }

    /**
     * 隐藏评价or取消隐藏
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    public Result hiddenComment(Long id) {
        log.info("隐藏评价or取消隐藏{}。。。", id);
        //1. 判断评价是否存在
        Comment comment = getById(id);
        if (comment == null) {
            log.info("评价不存在");
            return Result.error(MessageConstant.COMMENT_NOT_EXISTS);
        }

        //2. 隐藏评价或者取消隐藏
        Integer hidden = comment.getHidden();
        commentMapper.update(
                new LambdaUpdateWrapper<Comment>()
                        .set(StatusEnum.ENABLE.getId().equals(hidden), Comment::getHidden, StatusEnum.DISABLE.getId())
                        .set(StatusEnum.DISABLE.getId().equals(hidden), Comment::getHidden, StatusEnum.ENABLE.getId())
                        .eq(Comment::getId, id)
        );
        //3. 计算平均评分
        shopMapper.updateShopAvgScore(comment.getShopId());
        //3. 异步删除商铺评价缓存
        // key   commentCache:shopId:*（*：查询条件）     value
        Long shopId = comment.getShopId();
        syncCleanCommentCache(shopId);

        // 4. 异步保存向量
        CompletableFuture.runAsync(() -> {
            log.debug("【更新评价向量】");
            Integer status = hidden.equals(StatusEnum.ENABLE.getId()) ? StatusEnum.DISABLE.getId() : StatusEnum.ENABLE.getId();
            vectorService.updateCommentVectorStatus(comment.getId(), status);
        }, vectorExecutor);
        return Result.success();
    }

    private void syncCleanCommentCache(Long shopId) {
        log.info("正在异步清除商店评价缓存：shopId={}", shopId);
        CompletableFuture.runAsync(() -> {
            deleteKeysByPattern(CacheConstant.COMMENT_CACHE + shopId + ":*");
        }, cacheExecutor);
    }

    private void deleteKeysByPattern(String pattern) {
        log.info("【{}】开始清除缓存。。。", Thread.currentThread().getName());
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(1000)
                .build();
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            List<String> keys = new ArrayList<>();
            while (cursor.hasNext()) {
                keys.add(cursor.next());
                //500条删一次
                if (keys.size() == 500) {
                    stringRedisTemplate.delete(keys);
                    keys.clear();
                }
            }
            //若总 未到500，删一次
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("清除缓存失败, pattern={}", pattern, e);
        }
    }


    /**
     * 分页查询商店评价。sortType=1:按点赞降序，按踩升序。sortType=2：按时间降序排序。（默认1）
     *
     * @param sortType
     * @param shopId
     * @param pageQueryDTO
     * @return
     */
    @Override
    public Result<List<CommentVO>> queryComment(Integer sortType, Long shopId, PageQueryDTO pageQueryDTO) {
        log.info("分页查询商店评价:shopId={}。。。", shopId);
        Integer pageNo = pageQueryDTO.getPageNo();
        Integer pageSize = pageQueryDTO.getPageSize();
        //从缓存或数据库获取分页结果
        String key = CacheConstant.COMMENT_CACHE + shopId + ":" + sortType + "-" + pageNo + "-" + pageSize;
        List<CommentVO> vos = queryComments(key, () -> {
            //从数据库分页查询商店评价
            Page<Comment> page = lambdaQuery()
                    .eq(Comment::getShopId, shopId)
                    .eq(Comment::getHidden, StatusEnum.ENABLE.getId())
                    .orderByDesc(sortType == 1, Comment::getLikeNum)
                    .orderByAsc(sortType == 1, Comment::getDislikeNum)
                    .orderByDesc(sortType == 2, Comment::getCreateTime)
                    .page(new Page<>(pageNo, pageSize));
            return page.getRecords();
        });
        return Result.success(vos);
    }

    private List<CommentVO> queryComments(String key, Supplier<List<Comment>> dbQuery) {
        //1. 查缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        if ("".equals(json)) {
            log.info("查到空串，当前无数据，返回空集合。。。");
            return Collections.emptyList();
        }
        List<Comment> comments = JSONUtil.toList(json, Comment.class);

        //2. 缓存未命中，查数据库
        if (comments == null || comments.isEmpty()) {
            log.info("缓存未命中,查询数据库。。。");
            comments = dbQuery.get();
            if (comments == null || comments.isEmpty()) {
                //缓存穿透（数据库没数据）
                log.info("缓存穿透。将缓存空串（避免缓存击穿），返回空集合。。。");
                stringRedisTemplate.opsForValue().set(key, "", 5, TimeUnit.MINUTES);
                return Collections.emptyList();
            }
            //3. 写缓存
            String jsonStr = JSONUtil.toJsonStr(comments);
            stringRedisTemplate.opsForValue().set(key, jsonStr, 24, TimeUnit.HOURS);
        }

        //4. 从缓存查赞踩总数，用户行为，评价发布者信息。
        List<CommentVO> vos = BeanUtil.copyToList(comments, CommentVO.class);
        List<String> numKeys = new ArrayList<>(vos.size());//查赞踩数量
        List<Object> commentIds = new ArrayList<>(vos.size());//当前用户对目标评价的赞踩状态
        List<Long> userIds = new ArrayList<>(vos.size());//评价发布者id
        for (CommentVO vo : vos) {
            numKeys.add(CacheConstant.COMMENT + CacheConstant.COMMENT_NUM + vo.getId());
            commentIds.add(vo.getId().toString());
            userIds.add(vo.getUserId());
        }
        List<String> fields = DataConstant.LIKE_AND_DISLIKE_LIST;

        //4.1 异步 获取每个评价的赞/踩数量
        CompletableFuture<List<Object>> future1 = CompletableFuture.supplyAsync(() -> {
            log.info("【{}】异步获取每个评价的赞/踩数量。。。", Thread.currentThread().getName());
            return stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                RedisHashCommands hashOps = connection.hashCommands();
                for (String numKey : numKeys) {
                    for (String field : fields) {
                        hashOps.hGet(numKey.getBytes(), field.getBytes());
                    }
                }
                return null;
            });
        }, cacheExecutor);

        //4.2 异步 获取当前用户赞或踩过的评价
        Long userId = ThreadLocalUtil.getUserId();
        CompletableFuture<List<Object>> future2;
        if (userId == null) {
            future2 = CompletableFuture.completedFuture(Collections.nCopies(commentIds.size(), null));
        } else {
            String actionKey = CacheConstant.COMMENT + CacheConstant.COMMENT_ACTION + userId;
            future2 = CompletableFuture.supplyAsync(() -> {
                log.info("【{}】异步获取当前用户赞或踩过的评价。。。", Thread.currentThread().getName());
                return stringRedisTemplate.opsForHash().multiGet(actionKey, commentIds);
            }, cacheExecutor);
        }

        //4.3 异步 获取评价发布者信息
        CompletableFuture<Map<Long, UserInfoDTO>> future3 = CompletableFuture.supplyAsync(() -> {
            if (userIds.isEmpty()) {
                return Collections.emptyMap();
            }
            List<UserInfoDTO> list = userMapper.queryUserInfoList(userIds, StatusEnum.ENABLE.getId());
            if (list == null || list.isEmpty()) {
                return Collections.emptyMap();
            }
            return list.stream().collect(Collectors.toMap(UserInfoDTO::getId, v -> v));
        }, cacheExecutor);


        List<Object> results;
        List<Object> objects;
        Map<Long, UserInfoDTO> userInfos;
        try {
            //获取查询结果
            results = future1.get();//赞踩数量
            objects = future2.get();//用户赞踩行为
            userInfos = future3.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("异步 Redis 操作失败", e);
            return vos;
        }

        //5. 封装赞，踩数量和用户赞踩状态。
        for (int i = 0; i < vos.size(); i++) {
            CommentVO vo = vos.get(i);

            //赞踩
            Object like = results.get(2 * i);
            Object dislike = results.get(2 * i + 1);
            vo.setLikeNum(TypeConversionUtil.toInt(like, 0));
            vo.setDislikeNum(TypeConversionUtil.toInt(dislike, 0));

            //行为
            Object action = objects.get(i);
            vo.setAction((String) action);

            //用户信息
            if (!userInfos.isEmpty()) {
                UserInfoDTO u = userInfos.get(vo.getUserId());
                if (u != null) {
                    vo.setUsername(u.getUsername());
                    vo.setAvatar(u.getAvatar());
                    continue;
                }
            }
            vo.setUsername(DataConstant.DEFAULT_USER_NAME);
            vo.setAvatar(DataConstant.DEFAULT_USER_AVATAR);
        }
        return vos;
    }


    /**
     * 点赞或取消点赞
     *
     * @param commentId
     * @return
     */
    private static final DefaultRedisScript<List> LIKE_ACTION_SCRIPT = new DefaultRedisScript<>();

    static {
        LIKE_ACTION_SCRIPT.setLocation(new ClassPathResource("/lua/like.lua"));
        LIKE_ACTION_SCRIPT.setResultType(List.class);
    }

    @Override
    public Result likeOrDislike(Long commentId, String action) {
        //1. 验证参数
        if (!"like".equals(action) && !"dislike".equals(action)) {
            log.error("参数错误:{}", action);
            return Result.error(MessageConstant.PARAM_ERROR + MessageConstant.LIKE_OR_DISLIKE);
        }

        //2. 验证用户
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            log.error("未获取到用户id");
            return Result.error(MessageConstant.NOT_LOGIN);
        }

        //3. 验证评价
        Comment comment = commentMapper.selectOne(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getId, commentId)
                        .eq(Comment::getHidden, StatusEnum.ENABLE.getId())
        );
        if (comment == null) {
            return Result.error(MessageConstant.COMMENT_NOT_EXISTS);
        }

        //4. 执行操作
        String actionKey = CacheConstant.COMMENT + CacheConstant.COMMENT_ACTION + userId;
        String numKey = CacheConstant.COMMENT + CacheConstant.COMMENT_NUM + commentId;
        String dirtyKey = CacheConstant.COMMENT + CacheConstant.COMMENT_DIRTY;
        log.info("执行点赞or踩脚本，actionKey={}, numKey={}, dirtyKey={}, commentId={}, action={}", actionKey, numKey, dirtyKey, commentId, action);
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) stringRedisTemplate.execute(
                LIKE_ACTION_SCRIPT,
                List.of(actionKey, numKey, dirtyKey),
                commentId.toString(), action
        );
        if (result == null || result.isEmpty()) {
            log.error("执行失败:{}", action);
            return Result.error(MessageConstant.ACTION_FAILED);
        }
        String operation = result.get(0);
        log.info("执行成功operation={}", operation);
        switch (operation) {
            case "cancel":
                return Result.success(MessageConstant.CANCEL + result.get(1));
            case "switch":
                return Result.success(String.format(MessageConstant.FROM_SWITCH_TO, result.get(1), result.get(2)));
            case "add":
                return Result.success(MessageConstant.ADD + result.get(1));
            default:
                return Result.success();
        }
    }


    /**
     * 删除评价
     *
     * @param commentId
     * @return
     */
    @Override
    @Transactional
    public Result deleteComment(Long commentId) {
        log.info("删除评价commentId={}", commentId);
        Comment comment = getById(commentId);
        Long userId = comment.getUserId();
        if (!userId.equals(ThreadLocalUtil.getUserId())) {
            log.info("不能删除别人的评价");
            return Result.error(MessageConstant.CAN_NOT_DELETE_OTHER_COMMENT);
        }
        //2. 隐藏评价
        log.info("正在删除评价。。。");
        commentMapper.update(
                new LambdaUpdateWrapper<Comment>()
                        .set(Comment::getHidden, StatusEnum.DISABLE.getId())
                        .eq(Comment::getId, commentId)
        );
        //3. 删除关键词关系
        log.info("正在删除关键词。。。");
        shopMapper.deleteKeywordRelation(commentId);
        Long shopId = comment.getShopId();
        //4.  删除图片
        if (StrUtil.isNotBlank(comment.getPhotoUrl())) {
            log.info("正在删除评价图片。。。");
            fileService.deleteFile(comment.getPhotoUrl());
        }
        //5. 计算平均分
        log.info("正在计算商店评分。。。");
        shopMapper.updateShopAvgScore(shopId);
        syncCleanCommentCache(shopId);

        // 6. 异步保存向量
        CompletableFuture.runAsync(() -> {
            log.info("【更新评价向量】");
            vectorService.updateCommentVectorStatus(commentId, StatusEnum.DISABLE.getId());
        }, vectorExecutor);
        return Result.success();
    }


    /**
     * 根据商店id查询商店的所有评价。按时间降序
     *
     * @return
     */
    @Override
    public Result<List<CommentAdminVO>> queryAllCommentByShopId(Long shopId) {
        List<CommentAdminVO> vos = commentMapper.queryAllCommentByShopId(shopId);
        if (vos == null) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(vos);
    }

    /**
     * 查询我发布的评价
     *
     * @param isAsc
     * @param dto
     * @return
     */
    @Override
    public PageResult<Comment> queryMyComments(Boolean isAsc, PageQueryDTO dto) {
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            return PageResult.success();
        }
        Page<Comment> page = lambdaQuery().eq(Comment::getUserId, userId)
                .eq(Comment::getHidden, 1)
                .orderByAsc(BooleanUtil.isTrue(isAsc), Comment::getCreateTime)
                .orderByDesc(isAsc == null || BooleanUtil.isFalse(isAsc), Comment::getCreateTime)
                .page(new Page<>(dto.getPageNo(), dto.getPageSize()));
        List<Comment> comments = page.getRecords();
        if (comments.isEmpty()) {
            return PageResult.success();
        }
        return PageResult.success(comments.size() >= dto.getPageSize(), comments);
    }

    /**
     * 帮助用户写评价
     *
     * @param dto
     * @return
     */
    @Override
    public String helpWriteComment(AiCommentDTO dto) {
        CommentDTO commentDTO = BeanUtil.copyProperties(dto, CommentDTO.class);
        try {
            Result<Long> result = commentService.addComment(commentDTO);
            if (!result.getCode().equals(200)) {
                return "写评价失败了，可能是商店id不存在，或者评价内容不合法等原因导致的哦，请告知用户检查一下输入的内容，或者稍后再试试吧！";
            }
            return "评价发布成功";
        } catch (Exception e) {
            return "写评价失败了,请告知用户检查一下输入的内容，或者稍后再试试吧！";
        }
    }
}
