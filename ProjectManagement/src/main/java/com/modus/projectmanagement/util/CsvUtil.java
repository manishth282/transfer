package com.modus.projectmanagement.util;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class CsvUtil {
    public static <T> List<T> read(Class<T> classs, InputStream inputStream){
        return new CsvToBeanBuilder<T>(new InputStreamReader(inputStream))
                .withType(classs)
                .withIgnoreLeadingWhiteSpace(true)
                .build().parse();
    }
}
