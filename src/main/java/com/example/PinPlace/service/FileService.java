package com.example.PinPlace.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {
    private static final String UPLOAD_DIR = "upload/profile/";

    public String saveProfileImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String newFilename = UUID.randomUUID().toString() + "." + originalFilename;

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());
            return "/" + UPLOAD_DIR + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
    }

    public void deleteFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제에 실패했습니다.", e);
        }
    }
}
