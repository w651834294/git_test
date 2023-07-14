package com.fu.yygh.oss.controller;

import com.fu.yygh.common.result.R;
import com.fu.yygh.oss.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "阿里云文件管理")
@RestController
@RequestMapping("/admin/oss/file")
public class FileUploadController {

    @Autowired
    private FileService fileService;

    @ApiOperation(value = "文件上传")
    @PostMapping("upload")
    public R upload(MultipartFile file){
        String uploadUrl=fileService.uploadFile(file);
        return R.ok().message("文件上传成功").data("url",uploadUrl);
    }
}
