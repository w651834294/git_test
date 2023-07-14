package com.fu.yygh.hosp.service;

import com.fu.yygh.model.hosp.Department;
import com.fu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface DepartmentService {
    void saveDepartment(Map<String, Object> stringObjectMap);

    Page findDepartmentPage(Map<String, Object> stringObjectMap);

    void remove(String hoscode, String depcode);

    List<DepartmentVo> getDeptList(String hoscode);

    Department getDepartment(String hoscode, String depcode);
}
