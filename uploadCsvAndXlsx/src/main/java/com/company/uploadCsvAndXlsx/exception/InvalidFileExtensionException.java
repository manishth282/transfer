package com.company.uploadCsvAndXlsx.exception;

public class InvalidFileExtensionException extends RuntimeException{

    public InvalidFileExtensionException(String message){
        super(message);
    }
}
