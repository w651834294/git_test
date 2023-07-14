package com.fu.yygh.cmn.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fu.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务类
 * </p>
 *
 * @author fu
 * @since 2023-06-16
 */
public interface DictService extends IService<Dict> {

    List<Dict> getDictListByPid(Long pid);

    void exportExcel(HttpServletResponse response) throws IOException;

    void importData(MultipartFile file) throws IOException;

    //根据上级编码和值获取数据字典名称
    String getNameByParentDictCodeAndValue(String parentDictCode, String value);


    List<Dict> findByDictCode(String dictCode);



}
