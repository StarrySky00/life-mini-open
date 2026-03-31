package com.starrysky.lifemini.mapper;

import com.starrysky.lifemini.model.entity.PendingUploads;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 临时上传文件记录表 Mapper 接口
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-18
 */
public interface PendingUploadsMapper extends BaseMapper<PendingUploads> {

    @Select("select id from tb_pending_uploads where file_id = #{uuid} and status = 0 ")
    Long getIdByDileId(@Param("uuid") String uuid);

    @Update("update tb_pending_uploads set status = 1 where id = #{id}")
    void confirmFile(@Param("id") Long id);
}
