package com.lsp.web.ONDCService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MISAutoScheduler {

    @Autowired
    private MISExportService misExportService;

    //Run every day at 10:00 AM (India time)
    @Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Kolkata")
    public void generateMorningReport() {
        try {
            System.out.println("🔹 Generating 10 AM MIS report...");
            String url = misExportService.generateDailyMISCSV();
            System.out.println("10 AM MIS report done: " + url);
        } catch (Exception e) {
            System.err.println("10 AM MIS report failed: " + e.getMessage());
        }
    }

    // Run every day at 2:30 PM (India time)
    @Scheduled(cron = "0 30 14 * * ?", zone = "Asia/Kolkata")
    public void generateAfternoonReport() {
        try {
            System.out.println("🔹 Generating 2:30 PM MIS report...");
            String url = misExportService.generateDailyMISCSV();
            System.out.println("2:30 PM MIS report done: " + url);
        } catch (Exception e) {
            System.err.println("2:30 PM MIS report failed: " + e.getMessage());
        }
    }
}
