package com.fu.yygh.cmn.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.Map;

public class DameDataListener extends AnalysisEventListener<Student> {
    //解析excel文件执行，逐行解析 输出内容信息
    @Override
    public void invoke(Student student, AnalysisContext analysisContext) {
        System.out.println(student);
    }

    //输出表头信息
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("excel文件的表头为："+headMap);
    }

    //excel文件中的数据读取完成之后
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
