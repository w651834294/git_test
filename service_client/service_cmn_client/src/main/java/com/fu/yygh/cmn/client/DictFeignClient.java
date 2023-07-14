package com.fu.yygh.cmn.client;

import com.fu.yygh.common.result.R;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-cmn")
public interface DictFeignClient {
   /* //根据父id和值获取字典数据名称
    @GetMapping("/admin/cmn/dict/getName/{parentDictCode}/{value}")
    String getName(@PathVariable("parentDictCode")String parentDictCode,@PathVariable("value")String value);

    //根据值获取字典数据名称
    @GetMapping("/admin/cmn/dict/getName/{value}")
    String getName(@PathVariable("value")String value);*/


    @GetMapping(value = "/admin/cmn/dict/getName/{parentDictCode}/{value}")
    public String getName(@PathVariable("parentDictCode") String parentDictCode, @PathVariable("value") String value);


    @GetMapping("/admin/cmn/dict/getName/{value}")
    public String getName(@PathVariable("value") String value);



}
