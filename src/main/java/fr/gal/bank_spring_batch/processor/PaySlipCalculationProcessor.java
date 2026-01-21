package fr.gal.bank_spring_batch.processor;


import fr.gal.bank_spring_batch.model.Employee;
import fr.gal.bank_spring_batch.model.PaySlip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Slf4j
public class PaySlipCalculationProcessor implements ItemProcessor<Employee, PaySlip> {

    private static final BigDecimal SOCIAL_CHARGES_RATE = new BigDecimal("0.22");
    private static final BigDecimal INCOME_TAX_RATE = new BigDecimal("0.10");

    @Override
    public PaySlip process(Employee employee) {
        log.info("Calcul de la fiche de paie pour: {} {}",
                employee.getFirstName(), employee.getLastName());

        BigDecimal grossSalary = employee.getBaseSalary();

        BigDecimal socialCharges = grossSalary
                .multiply(SOCIAL_CHARGES_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal incomeTax = grossSalary
                .multiply(INCOME_TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netSalary = grossSalary
                .subtract(socialCharges)
                .subtract(incomeTax)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Salaire net calculé: {} EUR", netSalary);

        return PaySlip.builder()
                .employee(employee)
                .paymentDate(LocalDate.now())
                .grossSalary(grossSalary)
                .socialCharges(socialCharges)
                .incomeTax(incomeTax)
                .netSalary(netSalary)
                .build();
    }
}