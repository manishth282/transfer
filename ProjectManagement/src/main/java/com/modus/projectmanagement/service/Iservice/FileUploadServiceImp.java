package com.modus.projectmanagement.service.Iservice;

import com.modus.projectmanagement.entity.Employee;
import com.modus.projectmanagement.exception.InvalidFileExtensionException;
import com.modus.projectmanagement.payload.EmployeeDto;
import com.modus.projectmanagement.repository.EmployeeRepository;
import com.modus.projectmanagement.service.FileUploadService;
import com.modus.projectmanagement.util.CsvUtil;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileUploadServiceImp implements FileUploadService {

    @Autowired
    private EmployeeRepository employeeRepo;
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ModelMapper modelMapper;

    public void saveFile(MultipartFile file) throws IOException {
        String fileType = file.getContentType();
        if (Objects.equals(fileType, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                Objects.equals(fileType, "application/vnd.ms-excel")) {
            List<EmployeeDto> employees = saveExcelFile(file.getInputStream());
            System.out.println("saveExcelFile, employee list :  \n"+employees);
        } else {
            if (fileType == null)
                throw new IllegalArgumentException(("fileType cannot be null"));
            if (fileType.equals("text/csv")) {
                List<EmployeeDto> employees = saveCsvFile(file);
                System.out.println("saveCsvFile, employee list : \n"+employees);
            }else{
                throw new InvalidFileExtensionException(messageSource.getMessage("InvalidFileExtension",null, LocaleContextHolder.getLocale()));
            }
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
        List<Employee> employeeList = employeeDtoList.stream().map(this::convertDtoToEntity).toList();
        System.out.println("from saveExcelFile to use saveAll : "+employeeList);
        List<Employee> employees = employeeRepo.saveAll(employeeList);
        workbook.close();
        return employees.stream().map(this::convertEntityToDto).toList();
    }
    private EmployeeDto storeRowToEmployeeDto(Map<String, Integer> header, Row row) {
        EmployeeDto employeeDto = new EmployeeDto();
        for (Map.Entry<String, Integer> entry : header.entrySet()) {
            String fieldName = entry.getKey();
            int columnIndex = entry.getValue();
            Cell cell = row.getCell(columnIndex);
            Field field = null;
            System.out.println("Cell : " + cell.getCellType() + " Value : " + cell.toString());
            try {
                field = EmployeeDto.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                switch (cell.getCellType()) {
                    case STRING:
                        field.set(employeeDto, cell.getStringCellValue());
                        break;
                    case NUMERIC:
                        if (field.getType().equals(Long.class))
                            field.set(employeeDto, (long) cell.getNumericCellValue());
                        else if (field.getType().equals(Integer.class))
                            field.set(employeeDto, (int) cell.getNumericCellValue());
                        else if (field.getType().equals(BigDecimal.class)) {
                            System.out.println("for BigDecimal : " + cell.getCellType());
                            field.set(employeeDto, BigDecimal.valueOf(cell.getNumericCellValue()));
                        }
//                        else if(DateUtil.isCellDateFormatted(cell)) { //Date is coming in STRING datatype from excel
//                            Date date = cell.getDateCellValue();
//                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//                            field.set(employeeDto, dateFormat.format(date));
//                        }
                        break;
                    case BOOLEAN:
                        field.setBoolean(employeeDto, cell.getBooleanCellValue());
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                if (field != null)
                    field.setAccessible(false);
            }
        }
        System.out.println("storeRowToEmployeeDto : " + employeeDto);
        return employeeDto;
    }
    private EmployeeDto convertEntityToDto(Employee employee){
        return modelMapper.map(employee, EmployeeDto.class);
    }
    private Employee convertDtoToEntity(EmployeeDto employeeDto){
        return modelMapper.map(employeeDto, Employee.class);
    }

    private List<EmployeeDto> saveCsvFile(MultipartFile file) throws IOException{
        List<EmployeeDto> employeeDtoList = CsvUtil.read(EmployeeDto.class, file.getInputStream());
        List<Employee> employeeList = employeeDtoList.stream().map(this::convertDtoToEntity).toList();
        List<Employee> employees = employeeRepo.saveAll(employeeList);
        return employees.stream().map(this::convertEntityToDto).toList();
    }
}

