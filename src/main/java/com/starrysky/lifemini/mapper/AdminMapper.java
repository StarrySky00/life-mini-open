package com.starrysky.lifemini.mapper;

import com.starrysky.lifemini.model.entity.Admin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starrysky.lifemini.model.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 管理员信息表 Mapper 接口
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-26
 */
public interface AdminMapper extends BaseMapper<Admin> {

    @Select("""
            select id,
                username,
                password,
                phone,
                create_time
            from tb_admin
            where phone = #{phone}
             """)
    Admin getUserByPhone(@Param("phone") String phone);
}
