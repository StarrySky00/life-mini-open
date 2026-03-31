package com.starrysky.lifemini.service.impl;

import cn.hutool.core.io.FileUtil;
import com.starrysky.lifemini.common.constant.MessageConstant;
import com.starrysky.lifemini.service.MinioService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.bucket}")
    private String bucketName;
    @Value("${minio.custom-domain}")
    private String URL_PRX;

    private final MinioClient minioClient;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 可访问的url
     */
    @Override
    public String upload(MultipartFile file, String fileName) {
        try {
            log.debug("上传文件:{}", fileName);
            //1. 获取文件流
            InputStream inputStream = file.getInputStream();
            //2. 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            //3. 返回可访问的url
            return URL_PRX + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED + ":" + e.getMessage());
        }
    }


    /**
     * 直接上传字节数组 (用于上传压缩后的图片)
     */
    @Override
    public String upload(byte[] imageBytes, String fileName) {
        try {
            // 将 byte[] 转为 InputStream
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            String contentType = FileUtil.getMimeType(fileName);

            // 兜底机制：如果没推断出来，或者连后缀名都没有，默认给个 jpeg 或者通用的二进制流
            if (contentType == null) {
                contentType = "image/jpeg";
                // 也可以用纯原生 Java： contentType = URLConnection.guessContentTypeFromName(fileName);
            }
            // 调用 MinIO 的 putObject，注意告诉它这是字节流
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(bais, imageBytes.length, -1)
                    .contentType(contentType)
                    .build();
            minioClient.putObject(putObjectArgs);

            // 返回不带 9000 端口的最终访问路径 (配置在 yml 里的 customDomain)
            return URL_PRX + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            log.error("MinIO 上传压缩图片失败", e);
            throw new RuntimeException(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED + ":" + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 可访问的url
     * @return
     */
    @Override
    public void deleteFile(String fileUrl) {
        try {
            log.debug("删除文件:{}", fileUrl);
            //1. 获取文件路径  目录/文件名
            String filePath = fileUrl.replace(URL_PRX + "/" + bucketName + "/", "");
            //2. 删除文件
            minioClient.removeObject(
                    RemoveObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
        } catch (Exception e) {
            log.error("文件删除失败:" + fileUrl);
            throw new RuntimeException(MessageConstant.FILE_DELETE + MessageConstant.FAILED, e);
        }
    }

    /**
     * 根据文件路径集合  批量删除文件
     *
     * @param filePaths 文件路径集合
     * @return 删除失败的文件路径集合
     */
    @Override
    public List<String> deleteFileByPath(List<DeleteObject> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            log.info("文件路径集合为空");
            return Collections.emptyList();
        }
        try {
            log.info("开始批量删除");
            long start = System.currentTimeMillis();
            // 1. 批量删除
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(filePaths)
                            .build()
            );
            //2. 检查是否存在删除失败的
            List<String> failedFilePaths = new ArrayList<>();//记录失败文件路径作为返回值
            for (Result<DeleteError> result : results) {
                DeleteError deleteError = result.get();
                if ("NoSuchKey".equals(deleteError.code())) {
                    // 文件本来就没有，视为成功，不计入失败
                    continue;
                }
                failedFilePaths.add(deleteError.objectName());
                log.error("批量删除失败：对象={}, 错误={}", deleteError.objectName(), deleteError.message());
            }
            log.info("本次批量删除结束，耗时:{} ms", System.currentTimeMillis() - start);
            return failedFilePaths;
        } catch (Exception e) {
            log.error("文件批量删除异常");
            throw new RuntimeException(MessageConstant.FILE_DELETE + MessageConstant.FAILED, e);
        }
    }
}
