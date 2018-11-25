package com.pyg.manage.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import utils.FastDFSClient;
//dao service 异常都可以抛，web层不可以，web层是最后一层，抛的话就抛给客户了，是不可以的

//图片上传


//@RestController
//public class UploadController {
//
//        @Value("${FILE_SERVER_URL}")
//        private String FILE_SERVER_URL;
//    @RequestMapping("upload")
//    public Result upload(MultipartFile file){
//
//
//
//        String exname = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
//
//        try {
//            FastDFSClient client=new FastDFSClient("config/fdfs_client.conf");
//            String file_id = client.uploadFile(file.getBytes(), exname, null);
//
//            return new Result(true,FILE_SERVER_URL+file_id);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new Result(true,"上传失败");
//        }
//    }
//}
@RestController
public class UploadController {

//    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL="http://192.168.25.133/";

    /*
     *@Desc 封装页面提交的图片流
     *@param file 多媒体解析器封装的对象
     *@return entity.Result
     **/
    @RequestMapping("upload")
    public Result upload(MultipartFile file){
        //d:/3.4.jpg

        String extName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);

        try {
            FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
            String file_id = client.uploadFile(file.getBytes(), extName, null);
            return new Result(true,FILE_SERVER_URL+file_id);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }

    }

}

