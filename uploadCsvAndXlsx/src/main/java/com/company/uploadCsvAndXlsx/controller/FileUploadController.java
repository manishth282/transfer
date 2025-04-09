package com.company.uploadCsvAndXlsx.controller;

import com.company.uploadCsvAndXlsx.exception.InvalidFileExtensionException;
import com.company.uploadCsvAndXlsx.response.UploadErrorResponse;
import com.company.uploadCsvAndXlsx.service.FileUploadService;
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
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private MessageSource messageSource;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestPart("file") MultipartFile file) {
        if(file.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UploadErrorResponse(HttpStatus.BAD_REQUEST.value(), messageSource.getMessage("EmptyUploadedFile", null, LocaleContextHolder.getLocale())));
        }
        try {
            fileUploadService.saveFile(file);
            return ResponseEntity.ok("File uploaded and data saved successfully");
        } catch (IOException | CsvValidationException e) {
            return ResponseEntity.badRequest().body("Failed to process file");
        }
    }
}

