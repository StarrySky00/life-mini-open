package com.starrysky.lifemini.service;

import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.model.vo.DataVO;
import com.starrysky.lifemini.model.vo.UserClicksVO;
import com.starrysky.lifemini.model.vo.UserVisitsVO;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;

public interface ConsoleService {
    //获取新用户数据
    Result<DataVO> getNewUserChange(LocalDate begin, LocalDate end);
    //获取新店铺数据
    Result<DataVO> getNewShopChange(LocalDate begin, LocalDate end);

    //获取用户访问量和总访问量
    Result<UserVisitsVO> getUserVisits(LocalDate begin, LocalDate end);

    //用户点击量和总点击量
    Result<UserClicksVO> getUserClicks(LocalDate begin, LocalDate end);

    void exportDataExcelTable(HttpServletResponse response);


    Result<String> controlUser(Integer status);

    Result<Integer> getControlStatus();
}
