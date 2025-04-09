package com.company.uploadCsvAndXlsx.service;

import com.company.uploadCsvAndXlsx.dto.EmployeeDto;
import com.company.uploadCsvAndXlsx.entity.Employee;
import com.company.uploadCsvAndXlsx.exception.InvalidFileExtensionException;
import com.company.uploadCsvAndXlsx.repository.EmployeeRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.EmptyFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;

@Service
public class FileUploadService {

    @Autowired
    private EmployeeRepository employeeRepo;
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ModelMapper modelMapper;

    public void saveFile(MultipartFile file) throws IOException, CsvValidationException{
        String fileType = file.getContentType();
        if (Objects.equals(fileType, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                Objects.equals(fileType, "application/vnd.ms-excel")) {
            List<EmployeeDto> employees = saveExcelFile(file.getInputStream());
            System.out.println("employee list :  \n"+employees);
        } else if (fileType.equals("text/csv")) {
            saveCsvFile(file);
        }else{
            throw new InvalidFileExtensionException(messageSource.getMessage("InvalidFileExtension",null, LocaleContextHolder.getLocale()));
        }
    }

    private List<EmployeeDto> saveExcelFile(InputStream file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file);
        List<EmployeeDto> employeeDtoList = new ArrayList<>();
        //getting data from other rows and saving in database
        for (Sheet sheet : workbook) {
            if (sheet.getLastRowNum() == -1 || sheet.getPhysicalNumberOfRows()<=1)
                throw new EmptyFileException();

            Map<String, Integer> header = new HashMap<>();
            //setting column's heading and index in header(type: Map)
            Row headerRow = sheet.getRow(0);
            for(Cell cell : headerRow){
                header.put(cell.getStringCellValue(), cell.getColumnIndex());
            }

            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                EmployeeDto employeeDto = null;
                employeeDto = storeRowToEmployeeDto(header, row);
                employeeDtoList.add(employeeDto);
            }
        }
        //Converting List<EmployeeDto> to List<Employee> and then saveAll
        List<Employee> employees = employeeDtoList.stream().map(this::convertDtoToEntity).toList();
        employeeRepo.saveAll(employees);
        workbook.close();
        return employeeDtoList;
    }
    private EmployeeDto storeRowToEmployeeDto(Map<String, Integer> header, Row row){
        EmployeeDto employeeDto = new EmployeeDto();
        for(Map.Entry<String, Integer> entry: header.entrySet()){
            String fieldName = entry.getKey();
            int columnIndex = entry.getValue();
            Cell cell = row.getCell(columnIndex);
            Field field = null;
            try {
                field = EmployeeDto.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                switch (cell.getCellType()){
                    case STRING :
                        field.set(employeeDto, cell.getStringCellValue());
                        break;
                    case NUMERIC:
                        if(field.getType().equals(Long.class))
                            field.set(employeeDto, (long)cell.getNumericCellValue());
                        else if(field.getType().equals(Integer.class))
                            field.set(employeeDto, (int)cell.getNumericCellValue());
                        break;
                    case BOOLEAN:
                        field.setBoolean(employeeDto, cell.getBooleanCellValue());
                }
            } catch (NoSuchFieldException|IllegalAccessException e) {
                e.printStackTrace();
            }finally{
                if(field != null)
                    field.setAccessible(false);
            }
        }
        System.out.println("storeRowToEmployeeDto : "+employeeDto);
        return employeeDto;
    }

    private EmployeeDto convertEntityToDto(Employee employee){
        return modelMapper.map(employee, EmployeeDto.class);
    }
    private Employee convertDtoToEntity(EmployeeDto employeeDto){
        return modelMapper.map(employeeDto, Employee.class);
    }

    //    private EmployeeDto storeCellDataToEmployeeDtoField(String fieldName, Cell cell){
//        System.out.println("fieldName : "+fieldName);
//        EmployeeDto employeeDto = new EmployeeDto();
//        Field field = null;
//        try {
//            field = EmployeeDto.class.getDeclaredField(fieldName);
//            field.setAccessible(true);
//            switch (cell.getCellType()){
//                case STRING :
//                    field.set(employeeDto, cell.getStringCellValue());
//                break;
//                case NUMERIC:
//                    if(field.getType().equals(Long.class))
//                        field.set(employeeDto, (long)cell.getNumericCellValue());
//                    else if(field.getType().equals(Integer.class))
//                        field.set(employeeDto, (int)cell.getNumericCellValue());
//                break;
//                case BOOLEAN:
//                    field.setBoolean(employeeDto, cell.getBooleanCellValue());
//            }
//        } catch (NoSuchFieldException|IllegalAccessException e) {
    ////            throw new RuntimeException(e);
//            e.printStackTrace();
//        }finally{
//            if(field != null)
//                field.setAccessible(false);
//        }
//        System.out.println("storeCellDataToEmployeeDtoField : "+employeeDto);
//        return employeeDto;
//    }

//    private void saveCsvFile(MultipartFile file) throws IOException, CsvValidationException {
//        CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()));
//        String[] values;
//        csvReader.readNext();
//        while ((values = csvReader.readNext()) != null) {
//            employee employee = new employee();
//            employee.setName(values[0]);
//            employee.setAge(values[1]);  // values[1] it is coming as String
//            employeeRepo.save(employee);
//        }
//        csvReader.close();
//    }

    private void saveCsvFile(MultipartFile file) throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headerRow = csvReader.readNext();
            if (headerRow == null) return; // No header row

            Map<String, Integer> header = new HashMap<>();
            for (int i = 0; i < headerRow.length; i++) {
                header.put(headerRow[i], i);
            }

            String[] values;
            while ((values = csvReader.readNext()) != null) {
                Employee employee = new Employee();
                for (Map.Entry<String, Integer> entry : header.entrySet()) {
                    String fieldName = entry.getKey();
                    int index = entry.getValue();
                    Field field = null;
                    try {
                        field = Employee.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(employee, values[index]);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    } finally {
                        if (field != null)
                            field.setAccessible(false);
                    }
                }
                employeeRepo.save(employee);
            }
        }
    }
}

