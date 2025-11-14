package com.lsp.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lsp.web.ONDCService.MISExportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lsp.web.ONDCService.MISExportService;

@RestController
public class MISExportController {

    @Autowired
    private MISExportService misExportService;
@CrossOrigin("*")
    @GetMapping("/generate-mis-csv")
    public String generateMISCSV() {
        try {
            String s3FileUrl = misExportService.generateDailyMISCSV();
            if (s3FileUrl != null) {
                return "CSV uploaded to S3 successfully: " + s3FileUrl;
            } else {
                return "CSV creation/upload failed!";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

@CrossOrigin("*")

//Agent-specific MIS generation
//@GetMapping("/agent-mis-csv")
//public String generateAgentMIS() {
//    try {
//        // Hardcoded agent ID and email
//    	Integer agentId = 114521780;
//        String email = "yogitatekale1911@gmail.com";
//
//        String s3FileUrlagent = misExportService.generateMISForAgent(agentId, email);
//        if (s3FileUrlagent != null) {
//            return "Agent MIS for " + agentId + " uploaded to S3 successfully: " + s3FileUrlagent;
//        } else {
//            return "CSV creation/upload failed for Agent ID: " + agentId;
//        }
//    } catch (Exception e) {
//        e.printStackTrace();
//        return "Error generating agent MIS: " + e.getMessage();
//    }
//}

// 3. Generate and send MIS for ALL agents (the new batch logic)
@GetMapping("/generate-all-agent-mis")
public String generateAllAgentsMIS() {
    try {
        misExportService.generateAndSendMISForAllAgents();
        return "All Agent MIS files generated, uploaded, and emails sent successfully!";
    } catch (Exception e) {
        e.printStackTrace();
        return "Failed to generate all agents MIS: " + e.getMessage();
    }
}s
}



//package com.lsp.web.controller;
//
//import java.io.File;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.lsp.web.ONDCService.MISExportService;
//
//@RestController
//public class MISExportController {
//
//    @Autowired
//    private MISExportService misExportService;
//
//    @GetMapping("/generate-mis-csv")
//    public String generateMISCSV() {
//        try {
//            File csvFile = misExportService.generateDailyMISCSV();
//            if (csvFile.exists()) {
//                return "CSV file created: " + csvFile.getAbsolutePath();
//            } else {
//                return "CSV file creation failed!";
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Error: " + e.getMessage();
//        }
//    }
//}
