package com.company.uploadCsvAndXlsx.repository;

import com.company.uploadCsvAndXlsx.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
