package fr.gal.bank_spring_batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PaySlipSpringBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaySlipSpringBatchApplication.class, args);
	}

}
