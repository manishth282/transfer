package com.modus.projectmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "employee")
public class Employee {
    @Id
    @Column(name = "emp_id", nullable = false)
    private Long empId;

    @Column(name = "emp_name", nullable = false)
    private String empName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "designation", nullable = false)
    private String designation;

    @Column(name = "salary", nullable = false)
    private String salary;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "joining_date", nullable = false)
    private String joiningDate;
}
