package com.starrysky.lifemini.service;

import com.starrysky.lifemini.model.dto.UserDTO;
import com.starrysky.lifemini.model.dto.UserDeleteDTO;
import com.starrysky.lifemini.model.dto.WechatBindPhoneDTO;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.model.vo.UserAdminVO;
import com.starrysky.lifemini.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IWeChatService {
   /* //微信登录
    Result wxLogin(String code);

    //微信登录绑定手机号
    Result bindWechatPhone(WechatBindPhoneDTO bindDTO);
*/
    /**
     * 检查图片是否违规
     * @param imageBytes
     * @return true 通过
     */
     boolean checkImage(byte[] imageBytes);

    /**
     * 检查文本是否违规
     * @param content
     * @return true 通过
     */
    boolean checkContent(String content);
}
