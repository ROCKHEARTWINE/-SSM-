package com.company.proj.controller;

import com.company.proj.dto.Result;
import com.company.proj.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Properties;

@RestController
public class UploadController {
    @Value("${fastdfs.file_server_url}")
    private String file_server_url;
    @Value("${fastdfs.tracker_server}")
    private String tracker_server;

    @RequestMapping("/upload_file")
    public Result upload(MultipartFile file) {
        //1提取扩展名
        String originalFilename = file.getOriginalFilename();
        String extName = originalFilename.substring(originalFilename.lastIndexOf("." )+ 1);

        Properties properties = new Properties();
        properties.setProperty("fastdfs.tracker_servers",tracker_server);

        //TODO 调用fastdfs客户端上传文件
        try {
            FastDFSClient fastDFSClient = new FastDFSClient(properties);
            /*FastDFSClient fastDFSClient = new FastDFSClient("classpath:properties/fastdfs.properties");*/
            String dfs_file_name = fastDFSClient.uploadFile(file.getBytes(), extName);
            return new Result(true,file_server_url+dfs_file_name);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败");
        }

    }
}
