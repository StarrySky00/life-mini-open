package com.starrysky.lifemini.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starrysky.lifemini.common.constant.MessageConstant;
import com.starrysky.lifemini.common.enums.FileUploadStatus;
import com.starrysky.lifemini.model.entity.PendingUploads;
import com.starrysky.lifemini.mapper.PendingUploadsMapper;
import com.starrysky.lifemini.service.IPendingUploadsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 临时上传文件记录表 服务实现类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-18
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PendingUploadsServiceImpl extends ServiceImpl<PendingUploadsMapper, PendingUploads> implements IPendingUploadsService {
    private final PendingUploadsMapper pendingUploadsMapper;
    /**
     * 保存待确认文件信息
     * @param uuid 文件唯一标识
     * @param fileName 文件名
     * @param ttl 待确认时长，默认单位 小时。  超时清空。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUploadRecords(String uuid, String fileName, Integer ttl) {
        try {
            PendingUploads pendingUploads = new PendingUploads();
            pendingUploads.setFileId(uuid);
            pendingUploads.setFilePath(fileName);
            LocalDateTime now = LocalDateTime.now();
            pendingUploads.setUploadTime(now);
            pendingUploads.setStatus(FileUploadStatus.PENDING.getStatus());//默认就是0
            pendingUploads.setExpireAt(now.plusHours(ttl));
            save(pendingUploads);
        }catch (Exception e){
            throw new RuntimeException(MessageConstant.PENDING_SAVE_FAILED);
        }
    }

    /**
     * 批量查询 超时未确认的文件
     * @return
     */
    @Override
    public List<PendingUploads> queryPendingInfo() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<PendingUploads> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(PendingUploads::getStatus,FileUploadStatus.PENDING)
                .le(PendingUploads::getExpireAt,now)
                .last("limit 500");
        List<PendingUploads> pendingUploads = pendingUploadsMapper.selectList(wrapper);
        return pendingUploads;
    }

    /**
     * 根据文件唯一标识获取文件id
     * @param uuid 唯一标识
     * @return 主键值  未查到时返回 0
     */
    @Override
    public Long getIdByFileId(String uuid) {
        Long id=pendingUploadsMapper.getIdByDileId(uuid);
        if(id==null || id ==0){
            return 0L;
        }
        return id;
    }

    /**
     * 确认待确认的文件
     * @param id 主键
     */
    @Override
    public void confirmFile(Long id) {
        log.info("修改文件状态为已确认");
        pendingUploadsMapper.confirmFile(id);
    }
}
