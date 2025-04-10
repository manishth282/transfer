package com.modus.projectmanagement.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {
    public void saveFile(MultipartFile file) throws IOException;
}
