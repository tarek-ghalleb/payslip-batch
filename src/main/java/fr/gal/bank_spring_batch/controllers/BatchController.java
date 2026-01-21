package fr.gal.bank_spring_batch.controllers;


import fr.gal.bank_spring_batch.scheduler.PaySlipScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final PaySlipScheduler scheduler;

    @PostMapping("/payslips/generate")
    public ResponseEntity<Map<String, Object>> generatePayslips(
            @RequestParam String month) {

        try {
            log.info("API - Déclenchement manuel pour le mois: {}", month);

            JobExecution execution = scheduler.triggerManualGeneration(month);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jobId", execution.getJobId());
            response.put("status", execution.getStatus().name());
            response.put("message", "Job lancé avec succès");
            response.put("startTime", execution.getStartTime());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors du lancement manuel", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("schedulerActive", true);
        status.put("timezone", "Europe/Paris");
        status.put("nextRun", "1er du mois à 2h00");
        status.put("cronExpression", "0 0 2 1 * ?");

        return ResponseEntity.ok(status);
    }
}
