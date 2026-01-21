package fr.gal.bank_spring_batch.config;


import fr.gal.bank_spring_batch.model.Employee;
import fr.gal.bank_spring_batch.model.PaySlip;
import fr.gal.bank_spring_batch.processor.PaySlipCalculationProcessor;
import fr.gal.bank_spring_batch.processor.PdfGenerationProcessor;
import fr.gal.bank_spring_batch.writer.MinioPayslipWriter;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class PayslipBatchConfiguration {

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Bean
    public JdbcCursorItemReader<Employee> employeeReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Employee>()
                .name("employeeReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM employees WHERE active = true")
                .rowMapper(new BeanPropertyRowMapper<>(Employee.class))
                .build();
    }


    @Bean
    public PaySlipCalculationProcessor payslipCalculationProcessor() {
        return new PaySlipCalculationProcessor();
    }

    @Bean
    public PdfGenerationProcessor pdfGenerationProcessor() {
        return new PdfGenerationProcessor();
    }

    @Bean
    public CompositeItemProcessor<Employee, PaySlip> compositeProcessor() {
        CompositeItemProcessor<Employee, PaySlip> processor =
                new CompositeItemProcessor<>();
        processor.setDelegates(Arrays.asList(
                payslipCalculationProcessor(),
                pdfGenerationProcessor()
        ));
        return processor;
    }

    @Bean
    public MinioPayslipWriter minioPayslipWriter(MinioClient minioClient) {
        return new MinioPayslipWriter(minioClient, bucketName);
    }

    @Bean
    public Step generatePayslipsStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     JdbcCursorItemReader<Employee> reader,
                                     CompositeItemProcessor<Employee, PaySlip> processor,
                                     MinioPayslipWriter writer) {
        return new StepBuilder("generatePayslips", jobRepository)
                .<Employee, PaySlip>chunk(2, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job payslipGenerationJob(JobRepository jobRepository,
                                    Step generatePayslipsStep) {
        return new JobBuilder("payslipGenerationJob", jobRepository)
                .start(generatePayslipsStep)
                .build();
    }
}