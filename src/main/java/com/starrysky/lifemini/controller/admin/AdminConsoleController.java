package com.starrysky.lifemini.controller.admin;


import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.model.vo.DataVO;
import com.starrysky.lifemini.model.vo.UserClicksVO;
import com.starrysky.lifemini.model.vo.UserVisitsVO;
import com.starrysky.lifemini.service.ConsoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@Slf4j
@RequestMapping("/admin/console")
@Tag(name = "（管理端）控制台", description = "管理端控制台，展示用户数据，访问数据等")
public class AdminConsoleController {
    @Resource
    private ConsoleService consoleService;

    @PutMapping("/control")
    @Operation(summary = "控制小程序的熔断",description = "禁止内容修改 1 ,禁止访问两种 2  ,取消 0  ")
    public Result<String> controlUser(@RequestParam("status") Integer status){
        return consoleService.controlUser(status);
    }
    @GetMapping("/control/status")
    @Operation(summary = "获取当前的控制状态")
    public Result<Integer> getControlStatus(){
        return consoleService.getControlStatus();
    }

    @GetMapping("/statsUserData")
    @Operation(summary = "统计新用户数据", description = "## 1. 时间范围最大为最近1年。 ## 2. 参数为起止日期范围，格式：yyyy-MM-dd")
    public Result<DataVO> getNewUserChange(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return consoleService.getNewUserChange(begin, end);
    }
    @GetMapping("/statsShopData")
    @Operation(summary = "统计新商店数据", description = "## 1. 时间范围最大为最近1年。 ## 2. 参数为起止日期范围，格式：yyyy-MM-dd")
    public Result<DataVO> getNewShopChange(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return consoleService.getNewShopChange(begin, end);
    }

    @GetMapping("/visits")
    @Operation(summary = "获取用户访问量", description = "## 1. 时间范围最大为最近三个月内。## 2. 参数为起止日期范围(begin,end)，格式：yyyy-MM-dd")
    public Result<UserVisitsVO> getUserVisits(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return consoleService.getUserVisits(begin, end);
    }
    @GetMapping("/clicks")
    @Operation(summary = "获取用户点击量", description = "## 1. 时间范围最大为最近三个月内。## 2. 参数为起止日期范围（begin,end），格式：yyyy-MM-dd")
    public Result<UserClicksVO> getUserClicks(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return consoleService.getUserClicks(begin, end);
    }
    @GetMapping("/export")
    @Operation(summary = "导出数据Excel表格",description = "导出数据Excel表格，返回值是void，通过response和输出流下载到用户浏览器")
    public void export(HttpServletResponse response){
        consoleService.exportDataExcelTable(response);
    }
}
