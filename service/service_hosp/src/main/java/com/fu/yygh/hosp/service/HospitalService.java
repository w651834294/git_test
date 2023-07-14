package com.fu.yygh.hosp.service;

import com.fu.yygh.model.hosp.Hospital;
import com.fu.yygh.vo.hosp.HospitalQueryVo;
import com.fu.yygh.vo.hosp.HospitalSetQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    void save(Map<String, Object> stringObjectMap);

    Hospital getByHoscode(String hoscode);


    Page<Hospital> selectPage(Integer page, Integer limit, HospitalSetQueryVo hospitalSetQueryVo);

    Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    void update(String id, Integer status);

    Map<String, Object> getHospitalDeatilById(String id);

    List<Hospital> findByNameLike(String hosname);
}
