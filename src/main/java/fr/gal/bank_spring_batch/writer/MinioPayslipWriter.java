package fr.gal.bank_spring_batch.writer;

import fr.gal.bank_spring_batch.model.PaySlip;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
public class MinioPayslipWriter implements ItemWriter<PaySlip> {

    private final MinioClient minioClient;
    private final String bucketName;

    @Override
    public void write(Chunk<? extends PaySlip> chunk) throws Exception {
        for (PaySlip paySlip : chunk) {
            String fileName = generateFileName(paySlip);

            log.info("Upload vers MinIO: {}", fileName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(new ByteArrayInputStream(paySlip.getPdfContent()),
                                    paySlip.getPdfContent().length, -1)
                            .contentType("application/pdf")
                            .build()
            );

            paySlip.setS3Path("s3://" + bucketName + "/" + fileName);

            log.info("Fiche de paie uploadée: {}", fileName);
        }
    }

    private String generateFileName(PaySlip payslip) {
        String date = payslip.getPaymentDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));

        String employeeNumber = payslip.getEmployee().getEmployeeNumber();

        return String.format("payslips/%s/%s_payslip_%s.pdf",
                date, employeeNumber, date);
    }
}
