package com.starrysky.lifemini.common.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtils {

    // 微信图片安全检查限制大小（1MB = 1048576 Bytes），我们设为 1000KB 留点余量
    private static final long IMAGE_SIZE = 1000 * 1024;

    /**
     * 将用户上传的图片压缩到1M范围内
     *
     * @param file 用户上传的原始文件
     * @return 压缩后的字节流
     */
    public static byte[] compressImage(MultipartFile file) throws IOException, RuntimeException {
        // 1. 如果原始文件大小已经符合要求，直接返回原始字节流
        if (file.getSize() <= IMAGE_SIZE) {
            return file.getBytes();
        }

        // 2. 如果文件过大，进行压缩
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                // 限制最大宽高：微信官方建议不超过 750 x 1334
                .size(750, 1334)
                // 设置图片质量：0.8 是一个很好的平衡点，肉眼看不出明显糊，但体积锐减
                .outputQuality(0.8f)
                // 建议统一下调输出格式为 jpg，不仅兼容性最好，而且文件体积比 png 小得多
                .outputFormat("jpg")
                .toOutputStream(outputStream);
        if (outputStream.size() == 0) {
            throw new RuntimeException("图片压缩后为空");
        }
        return outputStream.toByteArray();
    }
}