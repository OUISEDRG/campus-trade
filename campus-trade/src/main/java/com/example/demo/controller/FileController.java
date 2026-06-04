
package com.example.demo.controller;

import com.example.demo.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/file")
public class FileController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024; // 3MB

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                return Result.error("上传文件为空");
            }

            // 文件大小校验
            if (file.getSize() > MAX_FILE_SIZE) {
                return Result.error("文件大小不能超过 3MB");
            }

            // 文件扩展名校验
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            if (!ext.isEmpty() && !ALLOWED_EXTENSIONS.contains(ext)) {
                return Result.error("不支持的文件类型，仅支持 JPG、PNG、GIF、WebP 格式");
            }

            // 文件魔数校验 - 防止伪造扩展名
            byte[] magicBytes = new byte[4];
            try (java.io.InputStream is = file.getInputStream()) {
                is.read(magicBytes);
            }
            String magicHex = bytesToHex(magicBytes).toUpperCase();
            boolean validMagic = magicHex.startsWith("FFD8") || // JPEG
                                 magicHex.startsWith("89504E47") || // PNG
                                 magicHex.startsWith("47494638") || // GIF
                                 magicHex.startsWith("52494646"); // WEBP
            if (!validMagic) {
                return Result.error("文件类型不合法，仅支持 JPG、PNG、GIF、WebP 格式");
            }

            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
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

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
