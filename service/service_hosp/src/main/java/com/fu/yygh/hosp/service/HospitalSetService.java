package com.fu.yygh.hosp.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fu.yygh.model.hosp.HospitalSet;

/**
 * <p>
 * 医院设置表 服务类
 * </p>
 *
 * @author fu
 * @since 2023-06-11
 */
public interface HospitalSetService extends IService<HospitalSet> {

    String getSignKey(String hoscode);
}
