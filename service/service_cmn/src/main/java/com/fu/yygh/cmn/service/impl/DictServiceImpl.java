package com.fu.yygh.cmn.service.impl;


import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fu.yygh.cmn.excel.DameDataListener;
import com.fu.yygh.cmn.excel.Student;
import com.fu.yygh.cmn.listener.DictListener;
import com.fu.yygh.cmn.mapper.DictMapper;
import com.fu.yygh.cmn.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fu.yygh.common.exception.YYGHException;
import com.fu.yygh.model.cmn.Dict;
import com.fu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务实现类
 * </p>
 *
 * @author fu
 * @since 2023-06-16
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Cacheable(value = "dict",key = "'selectIndexList'+#pid")
    @Override
    public List<Dict> getDictListByPid(Long pid) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",pid);
        List<Dict> dicts = baseMapper.selectList(queryWrapper);
        for (Dict dict : dicts) {
            boolean result=hasChild(dict);
            dict.setHasChildren(result);
        }
        return dicts;
    }

    @Override
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");


        List<Dict> dicts = baseMapper.selectList(null);

        List<DictEeVo> list = new ArrayList<>();
        for (Dict dict : dicts) {
            DictEeVo dictEeVo = new DictEeVo();
            BeanUtils.copyProperties(dict,dictEeVo);//将一个对象的属性值复制到另一个对象的属性上，要求两个对象有相同的属性名
            list.add(dictEeVo);
        }


        EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet().doWrite(list);
    }

    @Override
    public void importData(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(baseMapper)).sheet().doRead();
    }

    @Override
    public String getNameByParentDictCodeAndValue(String parentDictCode, String value) {
        //如果value能直接定位数据字典，那么parentDictCode可以为空
        if(StringUtils.isEmpty(parentDictCode)){
            //如果为空，就只根据value值查询
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("value", value));
            if(null != dict){
                return dict.getName();
            }
        }else {
            //如果不为空，先通过parentDictCode把dict对象查询出来，最后通过dict中的id和value进行查询
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", parentDictCode));
            if(null==dict){
                throw new YYGHException(20001,"查询失败");
            }
            Long parentId = dict.getId();

            Dict newDict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("parent_id", parentId).eq("value", value));
            if(null!=newDict){
                return newDict.getName();
            }
        }

        return "";
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", dictCode));
        if(null!=dict){
            Long parentId = dict.getId();
            List<Dict> dicts = baseMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentId));
            return dicts;
        }
        return null;
    }




    private Dict getDictByDictCode(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",dictCode);
        Dict codeDict = baseMapper.selectOne(wrapper);
        return codeDict;
    }

    //判断当前元素是否有下一级子元素 有就返回true 没有就返回false
    private boolean hasChild(Dict dict) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",dict.getId());
        Integer count = baseMapper.selectCount(queryWrapper);
        return count>0;
    }



}
