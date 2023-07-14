package com.fu.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.fu.yygh.oss.service.FileService;
import com.fu.yygh.oss.utils.ConstantPropertiesUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private ConstantPropertiesUtil constantPropertiesUtil;

    @Override
    public String uploadFile(MultipartFile file) {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = constantPropertiesUtil.getEndpoint();
        // 强烈建议不要把访问凭证保存到工程代码里，否则可能导致访问凭证泄露，威胁您账号下所有资源的安全。本代码示例以从环境变量中获取访问凭证为例。运行本代码示例之前，请先配置环境变量。
        //EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写Bucket名称，例如examplebucket。
        String accessKeyId=constantPropertiesUtil.getKeyid();
        String accessKeySecret=constantPropertiesUtil.getKeysecret();
        String bucketName = constantPropertiesUtil.getBucketname();
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = UUID.randomUUID().toString().replaceAll("-","")+file.getOriginalFilename();
        String timeUrl = new DateTime().toString("yyyy/MM/dd");
        objectName=timeUrl+"/"+objectName;


        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            return "https://"+bucketName+"."+endpoint+"/"+objectName;
        }  catch (Exception e) {
            System.out.println("Error Message:" + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return null;
    }
}
