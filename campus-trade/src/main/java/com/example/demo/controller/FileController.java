
package com.example.demo.controller;

import com.example.demo.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/file")
public class FileController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                return Result.error("上传文件为空");
            }
            
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFileName = UUID.randomUUID().toString() + ext;

            File dest = new File(UPLOAD_DIR + newFileName);
            file.transferTo(dest);

            String url = "http://localhost:8080/file/" + newFileName;
            return Result.success(url);

        } catch (IOException e) {
            return Result.error("图片上传失败啦：" + e.getMessage());
        }
    }

    @GetMapping("/{fileName}")
    public void getFile(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        File file = new File(UPLOAD_DIR + fileName);
        if (file.exists()) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            response.getOutputStream().write(bytes);
        }
    }
}
