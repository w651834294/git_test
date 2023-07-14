package com.fu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fu.yygh.hosp.repository.DepartmentRepository;
import com.fu.yygh.hosp.service.DepartmentService;
import com.fu.yygh.model.hosp.Department;
import com.fu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;


    @Override
    public void saveDepartment(Map<String, Object> stringObjectMap) {
        String string = JSONObject.toJSONString(stringObjectMap);
        Department department = JSONObject.parseObject(string, Department.class);
        //查询mongodb中是否有科室信息
        Department targetDepartment = departmentRepository.findByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());

        if (targetDepartment == null) {
            //没有做添加操作
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        } else {
            //有做更新操作
            department.setCreateTime(targetDepartment.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(targetDepartment.getIsDeleted());
            department.setId(targetDepartment.getId());
            departmentRepository.save(department);
        }

    }

    @Override
    public Page findDepartmentPage(Map<String, Object> stringObjectMap) {
        String hoscode=(String)stringObjectMap.get("hoscode");
        Integer page=Integer.parseInt ((String) stringObjectMap.get("page"));
        Integer limit= Integer.parseInt((String) stringObjectMap.get("limit"));

        Department department = new Department();
        department.setHoscode(hoscode);
        Sort sort=Sort.by(Sort.Direction.ASC,"createTime");
        /*//创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写*/

        Example example=Example.of(department);

        PageRequest pageRequest= PageRequest.of(page-1,limit,sort);
        return  departmentRepository.findAll(example, pageRequest);
    }

    @Override
    public void remove(String hoscode, String depcode) {
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        if(department !=null){
            departmentRepository.deleteById(department.getId());
        }
    }


    @Override
    public List<DepartmentVo> getDeptList(String hoscode) {

        //1.根据医院编号查询所有科室信息
        Department department = new Department();
        department.setHoscode(hoscode);
        Example<Department> example=Example.of(department);
        List<Department> all = departmentRepository.findAll(example);

        //2.封装前端需要的数据
        //key:大科室编号，value:当前大科室底下的所有子科室列表
        Map<String, List<Department>> map = all.stream().collect(Collectors.groupingBy(Department::getBigcode));

        List<DepartmentVo> bigDeptList=new ArrayList<DepartmentVo>();

        for (Map.Entry<String, List<Department>> entry : map.entrySet()) {
            //大科室
            DepartmentVo departmentVo = new DepartmentVo();
            String bigCode = entry.getKey();
            departmentVo.setDepcode(bigCode);

            List<Department> smallDeptList = entry.getValue();

            departmentVo.setDepname(smallDeptList.get(0).getBigname());


            //封装的是当前大科室对应的所有子科室信息
            List<DepartmentVo> children = new ArrayList<DepartmentVo>();


            //子科室
            for (Department department1 : smallDeptList) {
                DepartmentVo small = new DepartmentVo();
                String depcode = department1.getDepcode();
                String depname = department1.getDepname();
                small.setDepcode(depcode);
                small.setDepname(depname);


                children.add(small);
            }
            departmentVo.setChildren(children);
            bigDeptList.add(departmentVo);
        }


        return bigDeptList;
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.findByHoscodeAndDepcode(hoscode,depcode);
    }


}
