package com.fu.yygh.hosp.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fu.yygh.hosp.mapper.HospitalSetMapper;
import com.fu.yygh.hosp.service.HospitalSetService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fu.yygh.model.hosp.HospitalSet;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 医院设置表 服务实现类
 * </p>
 *
 * @author fu
 * @since 2023-06-11
 */
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {


    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(queryWrapper);
        return hospitalSet.getSignKey();
    }
}
