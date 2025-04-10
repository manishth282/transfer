package com.modus.projectmanagement.controller;

import com.modus.projectmanagement.payload.UploadErrorResponse;
import com.modus.projectmanagement.service.FileUploadService;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000/","http://localhost:3001/"})
public class FileUploadController {
    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private MessageSource messageSource;
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestPart("file")MultipartFile file) throws CsvValidationException {
        if(file.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UploadErrorResponse(HttpStatus.BAD_REQUEST.value(), messageSource.getMessage("EmptyUploadedFile", null, LocaleContextHolder.getLocale())));
        }
        try {
            fileUploadService.saveFile(file);
        } catch (IOException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
        }
        return ResponseEntity.ok("File uploaded and data saved successfully");
    }
}
