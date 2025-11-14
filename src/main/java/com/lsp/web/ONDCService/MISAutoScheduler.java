package com.lsp.web.ONDCService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("prod") //  Only active when spring.profiles.active=prod
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

        // Run every day at 9:00 AM (India time)
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Kolkata")
    public void generateAgentReport() {
        try {
            System.out.println("Starting 9 AM Agent MIS batch...");

            misExportService.generateAndSendMISForAllAgents();  // 🔹 Call the multi-agent method

            System.out.println("9 AM Agent MIS batch completed successfully!");
        } catch (Exception e) {
            System.err.println("9 AM Agent MIS batch failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
