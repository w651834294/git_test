package com.fu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fu.yygh.cmn.client.DictFeignClient;
import com.fu.yygh.enums.DictEnum;
import com.fu.yygh.hosp.repository.HospitalRepository;
import com.fu.yygh.hosp.service.HospitalService;
import com.fu.yygh.model.hosp.Department;
import com.fu.yygh.model.hosp.Hospital;
import com.fu.yygh.vo.hosp.HospitalQueryVo;
import com.fu.yygh.vo.hosp.HospitalSetQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;



    @Override
    public void save(Map<String, Object> stringObjectMap) {
        String string = JSONObject.toJSONString(stringObjectMap);
        Hospital hospital = JSONObject.parseObject(string, Hospital.class);
        //根据医院编号查询医院信息
        Hospital mongoHospital = hospitalRepository.findByHoscode(hospital.getHoscode());
        //如果有医院信息，更新；如果没有，就添加
        if(mongoHospital==null){
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }else {
            hospital.setStatus(mongoHospital.getStatus());
            hospital.setCreateTime(mongoHospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(mongoHospital.getIsDeleted());
            hospital.setId(mongoHospital.getId());
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        return hospitalRepository.findByHoscode(hoscode);
    }

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalSetQueryVo hospitalSetQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");

        //0为第一页
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalSetQueryVo,hospital);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);

        //封装医院等级数据
        pages.getContent().stream().forEach(item->{
            this.packHospital(item);
        });

        return pages;
    }

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {

        Pageable pageable=PageRequest.of(page-1,limit,Sort.by(Sort.Direction.ASC,"createTime"));
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写


        Example<Hospital> example = Example.of(hospital,matcher);

        Page<Hospital> all = hospitalRepository.findAll(example, pageable);

        all.getContent().stream().forEach(item->{
            this.packHospital(item);
        });

        return all;
    }

    @Override
    public void update(String id, Integer status) {
        if(status.intValue()==0||status.intValue()==1){
            Hospital hospital = hospitalRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Map<String, Object> getHospitalDeatilById(String id) {
        Optional<Hospital> byId = hospitalRepository.findById(id);
        Hospital hospital = byId.get();
        hospital = this.packHospital(hospital);
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("hospital",hospital);
        map.put("bookingRule",hospital.getBookingRule());
        hospital.setBookingRule(null);
        return map;
    }

    @Override
    public List<Hospital> findByNameLike(String hosname) {
        List<Hospital> list=hospitalRepository.findByHosnameLike(hosname);
        return list;
    }




    /*@Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");

        //0为第一页
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);

        //封装医院等级数据
        pages.getContent().stream().forEach(item->{
            this.packHospital(item);
        });

        return pages;
    }*/





    //封装数据
    private Hospital packHospital(Hospital hospital) {
        String hostypeString = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(), hospital.getHostype());
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());

        hospital.getParam().put("hostypeString",hostypeString);
        hospital.getParam().put("fullAddress",provinceString+cityString+districtString+hospital.getAddress());
        return hospital;
    }


}
