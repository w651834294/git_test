package com.fu.yygh.cmn.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    @ExcelProperty(value = "名字",index = 0)
    private String name;
    @ExcelProperty(value = "年龄",index = 1)
    private Integer age;
    @ExcelProperty(value = "性别")
    private boolean gender;
}
