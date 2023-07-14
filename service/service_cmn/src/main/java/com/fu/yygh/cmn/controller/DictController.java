package com.fu.yygh.cmn.controller;


import com.alibaba.excel.EasyExcel;
import com.fu.yygh.cmn.service.DictService;
import com.fu.yygh.common.result.R;
import com.fu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * <p>
 * 组织架构表 前端控制器
 * </p>
 *
 * @author fu
 * @since 2023-06-16
 */
@Api(description = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin
public class DictController {

    @Autowired
    private DictService dictService;

    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getName(@ApiParam(name = "parentDictCode",value = "上级编码",required = true)
                              @PathVariable("parentDictCode") String parentDictCode,
                          @ApiParam(name = "value",value = "值",required = true)
                          @PathVariable("value") String value){

        return dictService.getNameByParentDictCodeAndValue(parentDictCode,value);
    }


    @ApiOperation(value = "获取数据字典名称")
    @GetMapping("/getName/{value}")
    public String getName(@ApiParam(name = "value",value = "值",required = true)@PathVariable("value") String value){
        return dictService.getNameByParentDictCodeAndValue("",value);
    }

    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping("/findByDictCode/{dictCode}")
    public R findByDictCode(@ApiParam(name = "dictCode",value = "节点编码",required = true)@PathVariable("dictCode")String dictCode){
        return R.ok().data("list",dictService.findByDictCode(dictCode));
    }



    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("/childList/{pid}")
    public R getDictListByPid(@ApiParam(name = "pid",value = "父id",required = true) @PathVariable(value = "pid") Long pid){
        return R.ok().data("list",dictService.getDictListByPid(pid));
    }


    //导出excel
    @GetMapping("/exportData")
    public void exportExcel(HttpServletResponse response) throws IOException {
        dictService.exportExcel(response);
    }

    //导出
    @PostMapping("/importData")
    public R importData(MultipartFile file) throws IOException {
        dictService.importData(file);
        return R.ok();
    }
}

