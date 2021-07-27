package com.example.controller;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@RestController
public class UploadController {


    private final static String utf8 = "utf-8";

    /**
     * 分片：一个请求一个分片
     * id,name,type(文件类型),lastModifiedDate(最后修改时间)，size(大小),chunks(分片数),chunk(当前分片数),upload(binary)
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/upload")
    public void upload(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding(utf8);
        Integer chunk = null;
        Integer chunks = null;
        String name = null;
        String uploadPath = "F:\\fileItem";
        BufferedOutputStream os = null;
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(1024);
            factory.setRepository(new File(uploadPath));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(5L * 1024L * 1024L * 1024L);
            upload.setSizeMax(10L * 1024L * 1024L * 1024L);
            List<FileItem> items = upload.parseRequest(request);

            for (FileItem item : items) {
                if (item.isFormField()) {
                    if (item.getFieldName().equals("chunk")) {
                        chunk = Integer.parseInt(item.getString(utf8));
                    }
                    if (item.getFieldName().equals("chunks")) {
                        chunks = Integer.parseInt(item.getString(utf8));
                    }
                    if (item.getFieldName().equals("name")) {
                        name = item.getString(utf8);
                    }
                }
            }
            //分片文件
            for (FileItem item : items) {
                if (!item.isFormField()) {
                    String temFileName = name;
                    if (name != null) {
                        if (chunk != null) {
                            temFileName = chunk + "_" + name;
                        }
                        //断点续传
                        //temFile 临时目录
                        File temFile = new File(uploadPath, temFileName);
                        if (!temFile.exists()) {
                            item.write(temFile);
                        }
                    }
                }
            }
            //判断是否有分片,文件合并
            if (chunk != null && chunk.intValue() == chunks.intValue() - 1) {
                File tempFile = new File(uploadPath, name);
                os = new BufferedOutputStream(new FileOutputStream(tempFile));

                for (int i = 0; i < chunks; i++) {
                    File file = new File(uploadPath, i + "_" + name);
                    while (!file.exists()) {
                        Thread.sleep(100);
                    }
                    byte[] bytes = FileUtils.readFileToByteArray(file);
                    os.write(bytes);
                    os.flush();
                    file.delete();
                }
                os.flush();
            }
            response.getWriter().write("文件上传成功" + name);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @RequestMapping("/uploadFile")
    public String uploadFile(HttpServletRequest request, HttpServletResponse response) throws IOException {

        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) (request);
        MultipartFile file = multiRequest.getFile("file");
        if (file.isEmpty()) {
            System.out.println("不存在");
        }
        String originalFilename = file.getOriginalFilename();
        byte[] bytes = file.getBytes();
        System.out.println(bytes);
        System.out.println(originalFilename + "," + file.getContentType() + "，" + file.getName());


        System.out.println("-------------------------------------");
        File newFile = new File("C:\\Users\\13347\\Desktop\\fff\\" + originalFilename);
        if (newFile.exists()) {
            return "文件已存在";
        }
        System.out.println(file.getSize());    //文件大小，限制大小
        System.out.println("=======================================");
        System.out.println("---------------------------------");
        InputStream inputStream = file.getInputStream();
        OutputStream outputStream = new FileOutputStream(newFile);
        byte[] tempbytes = new byte[1024];
        int read = 0;
        int off = 0;
        try {
            while ((read = inputStream.read(tempbytes)) != -1) {
                outputStream.write(tempbytes);
            }
        } finally {
            inputStream.close();
            outputStream.close();
        }
//        while ((byteread =  != -1)) {
//            out.write(tempbytes, 0, byteread);
//        }


//        file.transferTo(newFile);
//        FileOutputStream outputStream = new FileOutputStream(newFile);
//        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
//        outputStream.close();
//        outputStreamWriter.close();
        return "null";
    }
}
