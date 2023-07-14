package com.fu.yygh.cmn.excel;

import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;
import java.util.List;

public class EasyExcelWrite {
    public static void main(String[] args) {
        List<Student> list = new ArrayList<>();
        for (int i=0;i<10;i++){
            Student student = new Student();
            student.setName("张三"+i);
            student.setAge(18+i);
            student.setGender(true);
            list.add(student);
        }
        EasyExcel.write("E:\\easyExcel\\student.xlsx",Student.class).sheet().doWrite(list);
    }
}
