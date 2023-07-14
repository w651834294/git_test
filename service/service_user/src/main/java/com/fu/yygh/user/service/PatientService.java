package com.fu.yygh.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fu.yygh.model.user.Patient;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务类
 * </p>
 *
 * @author fu
 * @since 2023-07-05
 */
public interface PatientService extends IService<Patient> {


    Patient getPatientInfo(Integer id);

    List<Patient> getPatientListByUid(Long userId);

}
