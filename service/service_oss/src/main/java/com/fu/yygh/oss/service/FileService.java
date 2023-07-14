package com.fu.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;


public interface FileService {

    public String uploadFile(MultipartFile file);

}
