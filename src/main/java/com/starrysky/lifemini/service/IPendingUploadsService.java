package com.starrysky.lifemini.service;

import com.starrysky.lifemini.model.entity.PendingUploads;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 临时上传文件记录表 服务类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-18
 */
public interface IPendingUploadsService extends IService<PendingUploads> {
    //保存待确认文件信息
    void saveUploadRecords(String uuid,String fileName,Integer ttl);

    // 批量查询 超时未确认的文件
    List<PendingUploads> queryPendingInfo();

    //根据文件唯一标识获取文件id
    Long getIdByFileId(String uuid);

    //确认 待确认的文件
    void confirmFile(Long id);
}
