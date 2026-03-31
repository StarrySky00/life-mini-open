package com.starrysky.lifemini.service;

import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface MinioService {
    //上传文件
    String upload(MultipartFile file,String fileName);
    String upload(byte[] imageBytes,String fileName);


    //删除文件
    void deleteFile(String fileUrl);

    //根据文件路径删除文件
    List<String> deleteFileByPath(List<DeleteObject> filePaths);
}
