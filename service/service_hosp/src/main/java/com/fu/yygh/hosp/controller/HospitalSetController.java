package com.fu.yygh.hosp.controller;


import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fu.yygh.common.result.R;
import com.fu.yygh.hosp.service.HospitalSetService;
import com.fu.yygh.hosp.util.MD5;
import com.fu.yygh.model.hosp.HospitalSet;
import com.fu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author fu
 * @since 2023-06-11
 */
@Api(tags = "医院设置接口")
@RestController
@RequestMapping("/admin/hosp/hospital-set")
@Slf4j
//@CrossOrigin
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;


    @ApiOperation(value = "设置锁定和解锁")
    @PutMapping("lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(@ApiParam(name = "id",value = "医院编号",required = true)@PathVariable Long id,
                             @ApiParam(name = "status",value = "状态",required = true)@PathVariable Integer status){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }



    @ApiOperation(value = "批量删除医院")
    @DeleteMapping("batchRemove")
    public R batchRemoveHospitalSet(@ApiParam(name = "idList",value = "批量删除id列表",required = true)@RequestBody List<Long> idList){
        hospitalSetService.removeByIds(idList);
        return R.ok();
    }


    //根据医院id查询医院详情
    @ApiOperation(value = "根据医院id查询医院详情")
    @GetMapping("/detail")
    public R getHospitalSetDetail(@ApiParam(name = "id",value = "医院编号",required = true)@RequestParam("id") Integer id){
        return R.ok().data("item",hospitalSetService.getById(id));
    }




    //修改医院信息
    @ApiOperation(value = "根据id修改医院信息")
    @PostMapping("/updateHospSet")
    public R updateHospSet(@ApiParam(name = "HospitalSet",value = "医院设置对象",required = true)@RequestBody HospitalSet hospitalSet){
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }



    //医院添加
    @ApiOperation(value = "添加医院信息")
    @PostMapping("/saveHospSet")
    public R save(@RequestBody HospitalSet hospitalSet){
        hospitalSet.setStatus(0);
        //签名秘钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis()+""+random.nextInt(1000)));

        hospitalSetService.save(hospitalSet);
        return R.ok();
    }




    @ApiOperation(value = "带条件查询医院分页信息")
    @PostMapping("/{current}/{limit}")
    public R pageList(@ApiParam(name = "current",value = "当前页页码",required = true)@PathVariable(value = "current")Integer current,
                      @ApiParam(name = "limit",value = "每页显示条数",required = true)@PathVariable(value = "limit")Integer limit,
                      @ApiParam(name = "hospitalSetQueryVo",value = "查询对象",required = false)@RequestBody HospitalQueryVo hospitalQueryVo){
        Page<HospitalSet> pageParam = new Page<>(current, limit);
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        if(hospitalQueryVo==null){
            hospitalSetService.page(pageParam,queryWrapper);
        }else {
            String hosname = hospitalQueryVo.getHosname();
            String hoscode = hospitalQueryVo.getHoscode();

            if(!StringUtils.isEmpty(hosname)){
                queryWrapper.like("hosname",hosname);
            }
            if(!StringUtils.isEmpty(hoscode)){
                queryWrapper.eq("hoscode",hoscode);
            }

            hospitalSetService.page(pageParam,queryWrapper);
        }

        List<HospitalSet> records = pageParam.getRecords();
        long total = pageParam.getTotal();

        return R.ok().data("total", total).data("rows",records);
    }






    //查询所有医院设置
    @ApiOperation(value = "医院设置列表")
    @GetMapping("/findAll")
    public R findALL(){
        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("items",list);
    }

    @ApiOperation(value = "医院设置删除")
    @DeleteMapping("/remove/{id}")
    public R removeById(@ApiParam(name = "id",value = "医院ID设置",required = true)@PathVariable(value = "id") String id){
        boolean b = hospitalSetService.removeById(id);
        if(b){
            return R.ok();
        }else {
            return R.error();
        }
    }
}

