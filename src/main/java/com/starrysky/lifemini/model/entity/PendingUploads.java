package com.starrysky.lifemini.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 临时上传文件记录表
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-18
 */
@Data
@TableName("tb_pending_uploads")
@Schema(description="临时上传文件记录表")
public class PendingUploads implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键，自增ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "文件唯一标识（UUID，带连字符，如 a1b2c3d4-e5f6-...）")
    private String fileId;

    @Schema(description = "文件在对象存储中的完整路径（用于删除或移动）")
    private String filePath;

    @Schema(description = "文件状态：0=待确认，1=已确认")
    private Integer status;

    @Schema(description = "文件上传时间")
    private LocalDateTime uploadTime;

    @Schema(description = "过期时间（由应用层根据业务场景设置，如 upload_time + 24h）")
    private LocalDateTime expireAt;


}
