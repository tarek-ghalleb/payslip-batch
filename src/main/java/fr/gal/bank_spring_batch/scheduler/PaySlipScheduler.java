package fr.gal.bank_spring_batch.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaySlipScheduler {

    private final JobLauncher jobLauncher;
    private final Job payslipGenerationJob;
    private final JobExplorer jobExplorer;

    @Scheduled(
            cron = "${payslip.generation.cron:0 0 2 1 * ?}",
            zone = "${payslip.generation.timezone:Europe/Paris}"
    )
    public void generateMonthlyPayslips() {
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        log.info("DÉMARRAGE GÉNÉRATION AUTOMATIQUE DES FICHES DE PAIE");
        log.info("Période: {}", month);
        log.info("Heure: {}", LocalDate.now());

        try {
            if (isJobAlreadyExecutedThisMonth(month)) {
                log.warn("Le job a déjà été exécuté pour le mois {}", month);
                   return;
            }

            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("month", month)
                    .addString("triggeredBy", "scheduler")
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(payslipGenerationJob, params);

            BatchStatus status = execution.getStatus();

            if (status == BatchStatus.COMPLETED) {
                log.info("Job terminé avec succès!");
                log.info("Items lus: {}", execution.getStepExecutions().stream()
                        .mapToLong(StepExecution::getReadCount).sum());
                log.info("Items écrits: {}", execution.getStepExecutions().stream()
                        .mapToLong(StepExecution::getWriteCount).sum());

            } else if (status == BatchStatus.FAILED) {
                log.error("Le job a échoué!");
                logFailureDetails(execution);
            }

        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("Instance du job déjà complétée pour ces paramètres", e);

        } catch (JobExecutionAlreadyRunningException e) {
            log.warn("Le job est déjà en cours d'exécution", e);
        } catch (Exception e) {
            log.error("ERREUR CRITIQUE lors de la génération des fiches de paie", e);

        } finally {
            log.info("FIN DE LA GÉNÉRATION");
        }
    }

    public JobExecution triggerManualGeneration(String month) throws Exception {
        log.info("Déclenchement MANUEL de la génération pour le mois: {}", month);

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("month", month)
                .addString("triggeredBy", "manual")
                .toJobParameters();

        return jobLauncher.run(payslipGenerationJob, params);
    }

    private boolean isJobAlreadyExecutedThisMonth(String month) {
        return jobExplorer.findJobInstancesByJobName("payslipGenerationJob", 0, 100)
                .stream()
                .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                .filter(execution -> execution.getStatus() == BatchStatus.COMPLETED)
                .anyMatch(execution -> {
                    String jobMonth = execution.getJobParameters().getString("month");
                    return month.equals(jobMonth);
                });
    }

    private void logFailureDetails(JobExecution execution) {
        execution.getStepExecutions().forEach(step -> {
            if (step.getStatus() == BatchStatus.FAILED) {
                log.error("Step échoué: {}", step.getStepName());
                log.error("Raison: {}", step.getExitStatus().getExitDescription());

                step.getFailureExceptions().forEach(ex ->
                        log.error("Exception: {}", ex.getMessage(), ex)
                );
            }
        });
    }
}
