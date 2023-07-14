package com.fu.yygh.user.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fu.yygh.cmn.client.DictFeignClient;
import com.fu.yygh.enums.DictEnum;
import com.fu.yygh.model.user.Patient;
import com.fu.yygh.user.mapper.PatientMapper;
import com.fu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务实现类
 * </p>
 *
 * @author fu
 * @since 2023-07-05
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {


    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public Patient getPatientInfo(Integer id) {
        //根据就诊人id查询就诊人信息
        Patient patient = baseMapper.selectById(id);
        //把对应的编号转换成字符串回显
        patient=this.packPatient(patient);
        return patient;
    }

    @Override
    public List<Patient> getPatientListByUid(Long userId) {
        QueryWrapper<Patient> queryWrapper=new QueryWrapper<Patient>();
        queryWrapper.eq("user_id",userId);
        List<Patient> list = baseMapper.selectList(queryWrapper);
        return list;
    }


    private Patient packPatient(Patient patient) {
        //根据证件类型编码，获取具体的证件类型
        String certificatesTypeString = dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());
        //省
        String provinceString = dictFeignClient.getName(patient.getProvinceCode());
        //市
        String cityString = dictFeignClient.getName(patient.getCityCode());
        //区
        String districtString = dictFeignClient.getName(patient.getDistrictCode());
        patient.getParam().put("certificatesTypeString",certificatesTypeString);
        patient.getParam().put("provinceString",provinceString);
        patient.getParam().put("cityString",cityString);
        patient.getParam().put("districtString",districtString);
        patient.getParam().put("fullAddress",provinceString+cityString+districtString+patient.getAddress());
        return patient;

    }
}
