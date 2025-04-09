package com.company.uploadCsvAndXlsx.exception;

import com.company.uploadCsvAndXlsx.response.UploadErrorResponse;
import org.apache.poi.EmptyFileException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFileExtensionException.class)
    public ResponseEntity<UploadErrorResponse> invalidFileExtensionException(InvalidFileExtensionException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UploadErrorResponse(400, exception.getMessage()));
    }

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<UploadErrorResponse> emptyFileException(EmptyFileException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UploadErrorResponse(400, exception.getMessage()));
    }
}
