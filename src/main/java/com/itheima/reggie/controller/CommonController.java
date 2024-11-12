package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /*
    * 文件上传
    * */
    @PostMapping("/upload")
    // 参数名称需要与前端的name保持一致
    public R<String> upload(MultipartFile file){
        // file是个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        log.info(file.toString());

        // 原始文件名
        String originalFileName = file.getOriginalFilename();
        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String  fileName = UUID.randomUUID().toString() + suffix;

        // 创建一个目录对象
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        try{
            // 将临时文件转存到指定位置
            file.transferTo(new File(basePath + fileName));
        }catch (IOException e){
            e.printStackTrace();
        }

        // 返回文件名 后续需要保存到数据库中
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try{
            // 通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            // 通过输出流将文件写回浏览器 在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] read = new byte[1024];
            while((len = fileInputStream.read(read)) != -1){
                outputStream.write(read,0,len);
                outputStream.flush();
            }

            // 关闭资源
            outputStream.close();
            fileInputStream.close();

        }catch (IOException e){
            e.printStackTrace();
        }

    }


}
