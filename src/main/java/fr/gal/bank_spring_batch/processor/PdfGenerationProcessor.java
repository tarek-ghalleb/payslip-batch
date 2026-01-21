package fr.gal.bank_spring_batch.processor;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import fr.gal.bank_spring_batch.model.PaySlip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
public class PdfGenerationProcessor implements ItemProcessor<PaySlip, PaySlip> {

    @Override
    public PaySlip process(PaySlip payslip) throws Exception {
        log.info("Génération du PDF pour: {} {}",
                payslip.getEmployee().getFirstName(),
                payslip.getEmployee().getLastName());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("FICHE DE PAIE")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        document.add(new Paragraph("Période: " +
                payslip.getPaymentDate().format(DateTimeFormatter.ofPattern("MMMM yyyy")))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30));

        Table employeeTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth();

        employeeTable.addCell(createHeaderCell("Informations Employé"));
        employeeTable.addCell(createCell(""));

        employeeTable.addCell(createCell("Nom"));
        employeeTable.addCell(createCell(payslip.getEmployee().getLastName()));

        employeeTable.addCell(createCell("Prénom"));
        employeeTable.addCell(createCell(payslip.getEmployee().getFirstName()));

        employeeTable.addCell(createCell("Matricule"));
        employeeTable.addCell(createCell(payslip.getEmployee().getEmployeeNumber()));

        employeeTable.addCell(createCell("Poste"));
        employeeTable.addCell(createCell(payslip.getEmployee().getPosition()));

        employeeTable.addCell(createCell("Département"));
        employeeTable.addCell(createCell(payslip.getEmployee().getDepartment()));

        document.add(employeeTable);
        document.add(new Paragraph("\n"));

        Table salaryTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth();

        salaryTable.addCell(createHeaderCell("Libellé"));
        salaryTable.addCell(createHeaderCell("Montant (EUR)"));

        salaryTable.addCell(createCell("Salaire brut"));
        salaryTable.addCell(createAmountCell(payslip.getGrossSalary().toString()));

        salaryTable.addCell(createCell("Charges sociales (22%)"));
        salaryTable.addCell(createAmountCell("- " + payslip.getSocialCharges().toString()));

        salaryTable.addCell(createCell("Impôt sur le revenu (10%)"));
        salaryTable.addCell(createAmountCell("- " + payslip.getIncomeTax().toString()));

        salaryTable.addCell(createBoldCell("NET À PAYER"));
        salaryTable.addCell(createNetAmountCell(payslip.getNetSalary().toString()));

        document.add(salaryTable);

        document.close();

        byte[] pdfBytes = baos.toByteArray();
        payslip.setPdfContent(pdfBytes);

        log.info("PDF généré ({} bytes)", pdfBytes.length);

        return payslip;
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell createCell(String text) {
        return new Cell().add(new Paragraph(text));
    }

    private Cell createBoldCell(String text) {
        return new Cell().add(new Paragraph(text).setBold());
    }

    private Cell createAmountCell(String amount) {
        return new Cell()
                .add(new Paragraph(amount))
                .setTextAlignment(TextAlignment.RIGHT);
    }

    private Cell createNetAmountCell(String amount) {
        return new Cell()
                .add(new Paragraph(amount).setBold())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }
}
