package com.starrysky.lifemini.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ConsoleExcelVO {

    @Data
    public static class UserStats {
        @ExcelProperty("日期")
        private String date;
        @ExcelProperty("总用户数")
        private Integer total;
        @ExcelProperty("新增用户数")
        private Integer add;
    }

    @Data
    public static class ShopStats {
        @ExcelProperty("日期")
        private String date;
        @ExcelProperty("总店铺数")
        private Integer total;
        @ExcelProperty("新增店铺数")
        private Integer add;
    }

    @Data
    public static class CategoryStats {
        @ExcelProperty("分类名称")
        private String name;
        @ExcelProperty("分类店铺数量")
        private String count;
    }
}