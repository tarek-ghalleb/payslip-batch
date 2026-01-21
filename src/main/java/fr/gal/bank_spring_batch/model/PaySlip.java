package fr.gal.bank_spring_batch.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaySlip {
    private Employee employee;
    private LocalDate paymentDate;
    private BigDecimal grossSalary;
    private BigDecimal socialCharges;
    private BigDecimal incomeTax;
    private BigDecimal netSalary;
    private byte[] pdfContent;
    private String s3Path;
}
