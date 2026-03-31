package com.starrysky.lifemini.service.impl;

import com.starrysky.lifemini.common.constant.DataConstant;
import com.starrysky.lifemini.common.constant.MessageConstant;
import com.starrysky.lifemini.service.FileService;
import com.starrysky.lifemini.service.IPendingUploadsService;
import com.starrysky.lifemini.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${minio.custom-domain}")
    private String URL_PRX;
    @Value("${minio.bucket}")
    private String bucketName;
    private final MinioService minioService;
    private final IPendingUploadsService pendingUploadsService;

    /**
     * 上传文件（待确认）
     *
     * @param file      文件
     * @param folder    目录
     * @param isPending 是否待确认
     * @param ttl       待确认时间区间  单位：小时
     * @return url
     */
    @Override
    public String uploadFile(MultipartFile file, String folder, boolean isPending, Integer ttl) {
        log.info("【上传文件】上传至 " + folder + " 目录。是否为待确认文件：" + isPending + "，待确认时间：" + ttl + "h");
        //1. 生成唯一文件名
        try {
            if (file.getSize() > 2 * 1024 * 1024) {
                log.error("文件大小超出限制：{}", file.getOriginalFilename());
                throw new RuntimeException(MessageConstant.FILE_SIZE_EXCEED_LIMIT);
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                log.error("文件名不合法:{}", file.getOriginalFilename());
                throw new RuntimeException(MessageConstant.FILE_NAME_NOT_LEGAL);
            }
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!DataConstant.ALLOW_FILES.contains(suffix)) {
                log.error("文件类型错误");
                throw new RuntimeException(MessageConstant.FILE_TYPE_WRONG);
            }
            String uuid = UUID.randomUUID().toString();
            String fileName = folder + "/" + uuid + suffix;

            //2. 上传文件
            String url = minioService.upload(file, fileName);

            //3. 是否是待确认文件
            if (isPending) {
                try {
                    pendingUploadsService.saveUploadRecords(uuid, fileName, ttl);
                } catch (RuntimeException e) {
                    //4. 保存待确认信息失败
                    minioService.deleteFile(url);
                    throw new RuntimeException(MessageConstant.PENDING_SAVE_FAILED);
                }
            }
            return url;
        } catch (Exception ex) {
            log.error("文件上传失败: ", ex);
            return null;
        }
    }

    /**
     * 上传文件（无需确认）
     *
     * @param file   文件
     * @param folder 目录
     * @return 成功  url <br/> 失败：null
     */
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, false, null);
    }

    @Override
    public String uploadFile(byte[] imageBytes, String folder, String originalName, boolean isPending, Integer ttl) {
        log.info("【上传文件】上传至 " + folder + " 目录。是否为待确认文件：" + isPending + "，待确认时间：" + ttl + "h");
        //1. 生成唯一文件名
        try {
            if (originalName == null || originalName.isBlank()) {
                throw new RuntimeException(MessageConstant.FILE_NAME_NOT_LEGAL);
            }
            int dotIndex = originalName.lastIndexOf(".");
            //2. 非法文件！
            if (dotIndex == -1 || dotIndex == originalName.length() - 1) {
                throw new RuntimeException("文件格式非法，缺少有效的后缀名:"+originalName);
            }
            String suffix = originalName.substring(dotIndex);
            if (!DataConstant.ALLOW_FILES.contains(suffix)) {
                log.error("文件类型错误");
                throw new RuntimeException(MessageConstant.FILE_TYPE_WRONG);
            }
            String uuid = UUID.randomUUID().toString();
            String fileName = folder + "/" + uuid + suffix;

            //2. 上传文件
            String url = minioService.upload(imageBytes, fileName);

            //3. 是否是待确认文件
            if (isPending) {
                try {
                    pendingUploadsService.saveUploadRecords(uuid, fileName, ttl);
                } catch (RuntimeException e) {
                    //4. 保存待确认信息失败
                    minioService.deleteFile(url);
                    throw new RuntimeException(MessageConstant.PENDING_SAVE_FAILED);
                }
            }
            return url;
        } catch (Exception ex) {
            log.error("文件上传失败: ", ex);
            return null;
        }
    }

    @Override
    public String uploadFile(byte[] imageBytes, String folder, String originalName) {
        return uploadFile(imageBytes, folder,originalName, false, null);
    }

    /**
     * 判断文件状态是否过期
     *
     * @param imageUrl
     * @return
     */
    @Override
    public boolean fileStatus(String imageUrl, String folder) {
        if (imageUrl != null) {
            Long id = CheckFileStatus(imageUrl, folder);
            //3. 文件未确认，未过期
            if (id > 0) {
                //3.1 确认文件
                pendingUploadsService.confirmFile(id);
                return true;
            }
        }
        return false;
    }

    private Long CheckFileStatus(String imageUrl, String folder) {
        log.info("检查文件确认状态");
        //1. 截取文件名
        String fileName = imageUrl.replace(URL_PRX + "/" + bucketName + "/", "");
        String name = fileName.replace(folder + "/", "");
        String uuid = name.substring(0, name.lastIndexOf("."));
        Long id = pendingUploadsService.getIdByFileId(uuid);//方法里判断过了，查不到时返回0
        //2. 文件未确认，且已过期
        if (id == 0) {
            log.error("图片文件已经过期" + imageUrl);
            return 0L;
        }
        return id;
    }

    /**
     * 删除文件
     *
     * @param url
     */
    @Override
    public void deleteFile(String url) {
        minioService.deleteFile(url);
        return;
    }
}

