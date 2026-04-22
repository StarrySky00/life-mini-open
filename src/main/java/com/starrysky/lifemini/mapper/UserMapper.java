package com.starrysky.lifemini.mapper;

import com.starrysky.lifemini.model.dto.UserInfoDTO;
import com.starrysky.lifemini.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starrysky.lifemini.model.vo.UserVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 用户信息表 Mapper 接口
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-15
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("""
            select id,
                username,
                password,
                phone,
                avatar,
                preferences,
                status,
                create_time,
                update_time
            from tb_user
            where email = #{email}
             """)
    User getUserByEmail(@Param("email") String email);

    @Update("update tb_user set avatar = #{avatarUrl} where id = #{id} ")
    void updateAvatar(@Param("id") Long id,
                      @Param("avatarUrl") String avatarUrl);

    @Update("update tb_user set password = #{newEncodedPwd} where id = #{userId}")
    void updatePasswordById(@Param("newEncodedPwd") String newEncodedPwd,
                            @Param("userId") Long userId);

    @Update("update tb_user set password = #{password} where email = #{email}")
    void updatePasswordByEmail(@Param("email") String email,
                               @Param("password") String password);

    @Update("update tb_user set status = #{status} where id = #{userId}")
    boolean updateUserStatusById(@Param("userId") Long userId,
                                 @Param("status") Integer status);

    List<UserInfoDTO> queryUserInfoList(@Param("userIds") List<Long> userIds,
                                        @Param("status") Integer status);

    @Select("""
            select u.id,
            u.username,
            u.phone,
            u.avatar,
            u.preferences,
            u.create_time,
            u.update_time
            from tb_user u
            left join tb_comment c on u.id = c.user_id
            where c.id = #{commentId}
            """)
    UserVO getUserByCommentId(@Param("commentId") Long commentId);

    @Select("select * from tb_user where openid = #{openid}")
    User getUserByOpenid(@Param("openid") String openid);


    @Select("select email from tb_user where email = #{email}")
    String getEmailByEmail(String email);
}
