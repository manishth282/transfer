package com.company.uploadCsvAndXlsx.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadErrorResponse{
    private int status;
    private String message;

}
