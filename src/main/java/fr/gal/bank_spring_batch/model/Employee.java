package fr.gal.bank_spring_batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String position;
    private BigDecimal baseSalary;
    private Integer workedHours;
    private String department;
    private String employeeNumber;


}