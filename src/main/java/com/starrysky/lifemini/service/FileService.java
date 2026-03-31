package com.starrysky.lifemini.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传文件
     * @param file
     * @param folder 上传到目录
     * @param isPending true 待确认
     * @param ttl 待确认时间
     * @return url
     */
    String uploadFile(MultipartFile file,String folder,boolean isPending,Integer ttl);

    /**
     * 上传文件
     * @param imageBytes
     * @param folder 上传到目录
     * @param originalName 文件名称
     * @param isPending true 待确认
     * @param ttl 待确认时间
     * @return
     */
    String uploadFile(byte[] imageBytes,String folder,String originalName,boolean isPending,Integer ttl);

    /**
     * 上传文件，直接上传
     * @param file
     * @param folder
     * @return
     */
    String uploadFile(MultipartFile file,String folder);

    /**
     * 上传文件，直接上传
     * @param imageBytes
     * @param folder
     * @return
     */
    String uploadFile(byte[] imageBytes,String originalName,String folder);

    boolean fileStatus(String imageUrl,String folder);

    void deleteFile(String url);
}
