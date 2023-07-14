package com.fu.yygh.cmn.excel;

import com.alibaba.excel.EasyExcel;

public class EasyExcelRead {
    public static void main(String[] args) {
        EasyExcel.read("E:\\easyExcel\\student.xlsx",Student.class,new DameDataListener()).sheet().doRead();
    }
}
