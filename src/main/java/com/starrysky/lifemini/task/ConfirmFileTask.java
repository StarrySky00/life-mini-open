package com.starrysky.lifemini.task;

import cn.hutool.core.collection.CollUtil;
import com.starrysky.lifemini.model.entity.PendingUploads;
import com.starrysky.lifemini.service.IPendingUploadsService;
import com.starrysky.lifemini.service.MinioService;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmFileTask {
    private final IPendingUploadsService pendingUploadsService;
    private final MinioService minioService;

    /**
     * 每小时清除超时未确认的文件。
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Async(value = "dbExecutor")
    public void cleanPendingFileTask() {
        log.info("定时删除过期未确认文件————开始");
        //1. 获取过期待确认文件(单次最多500条)
        List<PendingUploads> pendingUploads = pendingUploadsService.queryPendingInfo();
        if (CollUtil.isEmpty(pendingUploads)) {
            log.info("无过期未确认文件。");
            return;
        }
        //2. 收集待删除文件的  id和文件路径
        Set<Long> ids = new HashSet<>(pendingUploads.size());
        List<DeleteObject> filePaths = new ArrayList<>(pendingUploads.size());
        for (PendingUploads pendingUpload : pendingUploads) {
            ids.add(pendingUpload.getId());
            filePaths.add(new DeleteObject(pendingUpload.getFilePath()));
        }
        log.info("清理过期待确认文件：{}条", pendingUploads.size());
        //3. 删除孤儿文件
        List<String> failedFilePaths = minioService.deleteFileByPath(filePaths);
        //4. 批量清理数据库
        pendingUploadsService.removeBatchByIds(ids);
        if (CollUtil.isEmpty(failedFilePaths)) {
            log.error("【删除失败的的文件，请手动删除】：{}", failedFilePaths);
            return;
        }
    }
}
