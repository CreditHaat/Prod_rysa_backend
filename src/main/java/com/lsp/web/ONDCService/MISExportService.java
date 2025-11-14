package com.lsp.web.ONDCService;

import java.util.Properties;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

import com.lsp.web.entity.MIS;
import com.lsp.web.repository.MISRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;



import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

import org.springframework.mail.javamail.JavaMailSender;

@Service
public class MISExportService {

    @Autowired
    private MISRepository misRepository;

    @Autowired
    private S3UploaderService s3UploaderService;

//    @Autowired
//    private JavaMailSender mailSender;

//    private final String fromEmail = "noreply@lowscore.club"; // sender email
    // Email credentials (Zoho)
//    private final String fromEmail = "support@arysefin.com";
//    private final String password = "Aryse@11!$04^25$%"; // keep safe in env later
//    private final String toEmail = "yogitatekale1911@gmail.com";
//    private final String emailSubject = "Daily MIS CSV Report";
//
//    // Build mail sender manually (no properties file)
//    private JavaMailSenderImpl getMailSender() {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        //mailSender.setHost("smtppro.zoho.in");
//        //mailSender.setPort(587);
//        mailSender.setUsername(fromEmail);
//        mailSender.setPassword(password);
//
//        Properties props = mailSender.getJavaMailProperties();
//        props.put("mail.smtp.host", "smtppro.zoho.in");
//		props.put("mail.smtp.port", "587");
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.ssl.trust", "smtppro.zoho.in");
//        props.put("mail.debug", "false");
//        props.put("mail.debug.auth", "true");
//        return mailSender;
//    }
//
    // Helper for null safety
    
    
    
    private String csvSafe(Object value) {
        if (value == null) return "";
        String str = value.toString();
        // Escape quotes
        str = str.replace("\"", "\"\"");
        // Wrap every field in quotes
        return "\"" + str + "\"";
    }

    public String generateDailyMISCSV() throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String fileName = "MIS_" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".csv";
        File file = new File(System.getProperty("java.io.tmpdir"), fileName);

        List<MIS> records = misRepository.findByCreateTimeBetween(
                LocalDate.now().minusDays(60).atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay()
        );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Header
            writer.write(
                    "\"id\",\"mobileNumber\",\"userId\",\"customUserId\",\"transactionId\",\"clickId\",\"journeyFlag\",\"breFlag\",\"productFlag\",\"pahal\",\"kissht\",\"BFL\",\"ABCL\",\"HDB\",\"aspireFin\",\"primeFlag\",\"status\",\"offerFlag\",\"cancelFlag\",\"createTime\"\n"

//                "\"id\",\"mobileNumber\",\"userId\",\"customUserId\",\"transactionId\",\"clickId\",\"journeyFlag\",\"breFlag\",\"productFlag\",\"pahal\",\"kissht\",\"BFL\",\"ABCL\",\"aspireFin\",\"status\",\"offerFlag\",\\\"cancelFlag\\\",\"createTime\"\n"
            );

            // Records
            for (MIS record : records) {
                writer.write(
                        csvSafe(record.getId()) + "," +
                        csvSafe(record.getMobileNumber()) + "," +
                        csvSafe(record.getUser() != null ? record.getUser().getId() : "") + "," +
                        csvSafe(record.getCustomUserId()) + "," +
                        csvSafe(record.getTransactionId()) + "," +
                        csvSafe(record.getClickId()) + "," +
                        csvSafe(record.getJourneyFlag()) + "," +
                        csvSafe(record.getBreFlag()) + "," +
                        csvSafe(record.getProductFlag()) + "," +
                        csvSafe(record.getPahal()) + "," +
                        csvSafe(record.getKissht()) + "," +
                        csvSafe(record.getBFL()) + "," +
                        csvSafe(record.getABCL()) + "," +
                        csvSafe(record.getHDB()) + "," +
                        csvSafe(record.getAspireFin()) + "," +
                        csvSafe(record.getPrimeFlag()) + "," + // prime flag added
                        csvSafe(record.getStatus()) + "," +
                        csvSafe(record.getOfferFlag()) + "," +
                        csvSafe(record.getCancelFlag())+","+
                        csvSafe(record.getCreateTime()) + "\n"
                );
            }
        }

        System.out.println("CSV Generated: " + file.getAbsolutePath());

        // Upload to S3 and send email
        String s3FileUrl = s3UploaderService.uploadFileToS3(file);
        if (s3FileUrl != null) {
            sendEmail(s3FileUrl);
        }

        return s3FileUrl;
    }

//    private String safe(Object value) {
//        return value == null ? "" : value.toString();
//    }
//    public String generateDailyMISCSV() throws IOException {
//        LocalDate today = LocalDate.now();
//        LocalDateTime startOfDay = today.atStartOfDay();
//        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
//
//        List<MIS> records = misRepository.findByCreateTimeBetween(startOfDay, endOfDay);
//    	LocalDate today = LocalDate.now();
//    	LocalDateTime startDate = today.minusDays(60).atStartOfDay();
//    	LocalDateTime endDate = today.plusDays(1).atStartOfDay();
//
//    	List<MIS> records = misRepository.findByCreateTimeBetween(startDate, endDate);
//
//
//// ====================================Generate CSV=========
////        String fileName = "MIS_" + today + ".csv";
//    	LocalDateTime now = LocalDateTime.now();
//    	String fileName = "MIS_" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".csv";
//
//        File file = new File(System.getProperty("java.io.tmpdir"), fileName);
//
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
//            // Header
//            writer.write("id,mobileNumber,userId,customUserId,transactionId,clickId,journeyFlag,breFlag,productFlag,pahal,kissht,BFL,ABCL,aspireFin,status,offerFlag,createTime\n");
//
//            // Records
//            for (MIS record : records) {
//                writer.write(
//                        safe(record.getId()) + "," +
//                        safe(record.getMobileNumber()) + "," +
//                        safe(record.getCustomUserId()) + "," +
//                        safe(record.getTransactionId()) + "," +
//                        safe(record.getClickId()) + "," +
//                        safe(record.getJourneyFlag()) + "," +
//                        safe(record.getBreFlag()) + "," +
//                        safe(record.getProductFlag()) + "," +
//                        safe(record.getPahal()) + "," +
//                        safe(record.getKissht()) + "," +
//                        safe(record.getBFL()) + "," +
//                        safe(record.getABCL()) + "," +
//                        safe(record.getAspireFin()) + "," +
//                        safe(record.getStatus()) + "," +
//                        safe(record.getOfferFlag()) + "," +
//                        safe(record.getCreateTime()) + "\n"
//                );
//            }
//        }
//
//        System.out.println("CSV Generated: " + file.getAbsolutePath());
//
//        // Upload to S3
//        String s3FileUrl = s3UploaderService.uploadFileToS3(file);
//        if (s3FileUrl != null) {
//            System.out.println("File uploaded to S3: " + s3FileUrl);
//
//            // Send email
//            sendEmail(s3FileUrl);
//        } else {
//            System.err.println("S3 upload failed.");
//        }
//
//        return s3FileUrl; // API will return this
//    }
//    
//    private void sendEmail(String s3FileUrl) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(fromEmail);
//            message.setTo(toEmail);
//            message.setSubject(emailSubject);
//            message.setText("Hello,\n\nThe daily MIS CSV report has been uploaded to S3.\n\n" +
//                    "You can download it from the following link:\n" + s3FileUrl + "\n\nBest regards,\nMIS System");
//
//            // Use the manual sender
//            JavaMailSenderImpl mailSender = getMailSender();
//            mailSender.send(message);
//
//            System.out.println("Email sent successfully to " + toEmail);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("Failed to send email: " + e.getMessage());
//        }
//    }


//    private void sendEmail(String s3FileUrl) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(fromEmail);
//            message.setTo(toEmail);
//            message.setSubject(emailSubject);
//            message.setText("Hello,\n\nThe daily MIS CSV report has been uploaded to S3.\n\n" +
//                    "You can download it from the following link:\n" + s3FileUrl + "\n\nBest regards,\nMIS System");
//
//            mailSender.send(message);
//            System.out.println("Email sent successfully to " + toEmail);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("Failed to send email: " + e.getMessage());
//        }
//    }
    
	/*
	 * public static void sendEmail(String s3FileUrl) {
	 * 
	 * // final String username = "support@arysefin.com"; // final String password =
	 * "Aryse@11!$04^25$%";
	 * 
	 * final String username = "mis@credithaat.com"; final String password =
	 * "misCH@12##$5";
	 * 
	 * Properties properties = new Properties(); properties.put("mail.smtp.host",
	 * "smtppro.zoho.in"); properties.put("mail.smtp.port", "587");
	 * properties.put("mail.smtp.starttls.enable", "true");
	 * properties.put("mail.smtp.auth", "true"); properties.put("mail.debug",
	 * "true"); properties.put("mail.transport.protocol", "smtp");
	 * properties.put("mail.debug.auth", "true");
	 * properties.put("mail.smtp.ssl.trust", "smtppro.zoho.in");
	 * 
	 * Session session = Session.getInstance(properties, new
	 * javax.mail.Authenticator() { protected PasswordAuthentication
	 * getPasswordAuthentication() { return new PasswordAuthentication(username,
	 * password); } }); try { MimeMessage message = new MimeMessage(session);
	 * message.setFrom(new InternetAddress(username));
	 * message.setRecipients(MimeMessage.RecipientType.TO,
	 * InternetAddress.parse("yogitatekale1911@gmail.com"));
	 * 
	 * // Add CC recipients //Address[] ccRecipients = InternetAddress.parse("");
	 * //message.setRecipients(Message.RecipientType.CC, ccRecipients);
	 * 
	 * message.setSubject("CreditHaat MIS"); message.setText(s3FileUrl+"");
	 * Transport.send(message); } catch (MessagingException e) {
	 * e.printStackTrace(); } }
	 */
    
    public static void sendEmail(String s3FileUrl) {

//   	 
//     final String username = "support@arysefin.com";
//     final String password = "Aryse@11!$04^25$%";

        final String username = "mis@credithaat.com";
        final String password = "misCH@12##$5";

        // SMTP properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtppro.zoho.in");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.ssl.trust", "smtppro.zoho.in");
        properties.put("mail.debug", "true");
        properties.put("mail.debug.auth", "true");

        // Create session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("shital.more@credithaat.com"));
            message.setSubject("CreditHaat MIS");
            message.setText("Hello,\n\nThe daily MIS CSV report has been uploaded to S3.\n\n" +
                  "You can download it from the following link:\n" + s3FileUrl + "\n\nBest regards,\nMIS System");

            // Send email
            Transport.send(message);

            System.out.println("Email sent successfully to shital.more@credithaat.com");

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }


        // =================Generate MIS for all agents (Main entry method) started================================
    // ====================================================================================================================================
    public void generateAndSendMISForAllAgents() {
        List<Agent> agents = agentRepository.findAll();

        if (agents.isEmpty()) {
            System.out.println("No agents found in the t_agent table.");
            return;
        }

        System.out.println("Total Agents Found: " + agents.size());

        for (Agent agent : agents) {
            try {
                Long agentIdLong = agent.getId();
                Integer agentId = (agentIdLong != null) ? agentIdLong.intValue() : null;

                if (agentId == null) {
                    System.out.println("Skipping agent with NULL ID");
                    continue;
                }

                String email = agent.getEmail();
                if (email == null || email.trim().isEmpty()) {
                    System.out.println("Skipping Agent ID " + agentId + " — No email found.");
                    continue;
                }

                System.out.println(" Processing Agent ID " + agentId + " (" + email + ")");

                // Generate and upload MIS for this agent
                String s3Url = generateMISForAgent(agentId, email);

                if (s3Url != null) {
                    System.out.println("MIS uploaded & emailed successfully for Agent ID " + agentId);
                } else {
                    System.out.println(" No MIS data found for Agent ID " + agentId);
                }

            } catch (Exception e) {
                System.err.println("Error processing agent " + agent.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // =========================================================
    // Generate individual agent MIS CSV and upload to S3
    // =========================================================
    public String generateMISForAgent(Integer agentId, String email) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String fileName = "AgentMIS_" + agentId + "_" +
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".csv";
        File file = new File(System.getProperty("java.io.tmpdir"), fileName);

        // Fetch MIS data for this agent (last 60 days)
        List<MIS> records = misRepository.findByAgentIdAndCreateTimeBetween(
                agentId,
                LocalDate.now().minusDays(60).atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay()
        );

        if (records == null || records.isEmpty()) {
            return null;
        }

        // Write CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("\"id\",\"userId\",\"customUserId\",\"transactionId\",\"clickId\","
                    + "\"journeyFlag\",\"primeFlag\",\"status\",\"offerFlag\",\"cancelFlag\",\"createTime\"\n");

            for (MIS record : records) {
                writer.write(
                        csvSafe(record.getId()) + "," +
//                        csvSafe(record.getAgentId()) + "," +
                        csvSafe(record.getUser() != null ? record.getUser().getId() : "") + "," +
                        csvSafe(record.getCustomUserId()) + "," +
                        csvSafe(record.getTransactionId()) + "," +
                        csvSafe(record.getClickId()) + "," +
                        csvSafe(record.getJourneyFlag()) + "," +
                        csvSafe(record.getPrimeFlag()) + "," +
                        csvSafe(record.getStatus()) + "," +
                        csvSafe(record.getOfferFlag()) + "," +
                        csvSafe(record.getCancelFlag()) + "," +
                        csvSafe(record.getCreateTime()) + "\n"
                );
            }
        }

        System.out.println("CSV Generated for Agent " + agentId + ": " + file.getAbsolutePath());

        // Upload to S3
        String s3FileUrl = s3UploaderService.uploadAgentMISFileToS3(file);

        if (s3FileUrl != null) {
            sendAgentEmail(email, s3FileUrl, agentId);
            file.delete(); // cleanup
        }

        return s3FileUrl;
    }

    // =========================================================
    // Send Email to Agent with S3 Link
    // =========================================================
    private void sendAgentEmail(String toEmail, String s3Url, Integer agentId) {
    	
        final String username = "mis@credithaat.com";
        final String password = "misCH@12##$5";
    	
//        final String username = "support@arysefin.com";
//        final String password = "Aryse@11!$04^25$%";
//

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtppro.zoho.in");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtppro.zoho.in");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("CreditHaat - MIS Report for Agent ID: " + agentId);
            message.setText("Hi,\n\nYour 60-day MIS CSV report has been uploaded to S3.\n\n"
                    + "Download here:\n" + s3Url + "\n\nBest Regards,\nCreditHaat MIS System");

            Transport.send(message);
            System.out.println("Email sent successfully to " + toEmail);

        } catch (MessagingException e) {
            System.err.println(" Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }
    
}



//package com.lsp.web.ONDCService;
//
//
//import com.lsp.web.entity.MIS;
//import com.lsp.web.repository.MISRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Paths;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//
//
//@Service
//public class MISExportService {
//
//    @Autowired
//    private MISRepository misRepository;
//
//    @Autowired
//    private S3UploaderService s3UploaderService;
//    
// // @Autowired // private JavaMailSender mailSender; // Email details 
//    
//    private final String fromEmail = "noreply@lowscore.club"; // Valid SMTP sender
//    private final String toEmail = "yogitatekale0802@gmail.com";
//    private final String emailSubject = "Daily MIS CSV Report";
////    private final String fromEmail = "yogitatekale0802@gmail.com"; 
////    private final String toEmail = "yogitatekale1911@gmail.com"; 
////    private final String emailSubject = "Daily MIS CSV Report";
//
//    // Helper method to handle null values
//    private String safe(Object value) {
//        return value == null ? "" : value.toString();
//    }
//
//    public String generateDailyMISCSV() throws IOException {
//        LocalDate today = LocalDate.now();
//        LocalDateTime startOfDay = today.atStartOfDay();
//        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
//
//        // Fetch records
//        List<MIS> records = misRepository.findByCreateTimeBetween(startOfDay, endOfDay);
//
//        // Generate file
//        String fileName = "MIS_" + today + ".csv";
//        File file = new File(System.getProperty("java.io.tmpdir"), fileName);
//
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
//            // CSV header
//            writer.write("id,mobileNumber,userId,customUserId,transactionId,clickId,journeyFlag,breFlag,productFlag,pahal,kissht,BFL,ABCL,aspireFin,status,offerFlag,createTime\n");
//
//            // Write records, handling nulls
//            for (MIS record : records) {
//                writer.write(
//                        safe(record.getId()) + "," +
//                        safe(record.getMobileNumber()) + "," +
//                        safe(record.getCustomUserId()) + "," +
//                        safe(record.getTransactionId()) + "," +
//                        safe(record.getClickId()) + "," +
//                        safe(record.getJourneyFlag()) + "," +
//                        safe(record.getBreFlag()) + "," +
//                        safe(record.getProductFlag()) + "," +
//                        safe(record.getPahal()) + "," +
//                        safe(record.getKissht()) + "," +
//                        safe(record.getBFL()) + "," +
//                        safe(record.getABCL()) + "," +
//                        safe(record.getAspireFin()) + "," +
//                        safe(record.getStatus()) + "," +
//                        safe(record.getOfferFlag()) + "," +
//                        safe(record.getCreateTime()) + "\n"
//                );
//            }
//
//            if (records.isEmpty()) {
//                System.out.println("No MIS records found for today. CSV will contain only the header.");
//            }
//        }
//
//        System.out.println("CSV Generated: " + file.getAbsolutePath());
//
//        // Upload to S3 and return URL
//        String s3FileUrl = s3UploaderService.uploadFileToS3(file);
//        if (s3FileUrl != null) {
//            System.out.println("File uploaded to S3: " + s3FileUrl);
//        } else {
//            System.err.println("S3 upload failed.");
//        }
//
//        return s3FileUrl;
//    }
//
//        // Upload to S3
//        String s3FileUrl = s3UploaderService.uploadFileToS3(file);
//
//        // Send email with S3 link
//        if (s3FileUrl != null) {
//            sendEmail(s3FileUrl);
//        } else {
//            System.err.println("S3 upload failed. Email not sent.");
//        }
////
//        // Upload to S3 and return URL
////        String s3FileUrl = s3UploaderService.uploadFileToS3(file);
////        
////        return s3FileUrl;    
////        }
//
//    private void sendEmail(String s3FileUrl) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom("noreply@lowscore.club");
//            message.setTo("yogitatekale0802@gmail.com");
//            message.setSubject(emailSubject);
//            message.setText("Hello,\n\nThe daily MIS CSV report has been uploaded to S3.\n\n" +
//                    "You can download it from the following link:\n" + s3FileUrl + "\n\nBest regards,\nMIS System");
//
//            //mailSender.send(message);
//            System.out.println("Email sent successfully to " + toEmail);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("Failed to send email: " + e.getMessage());
//        }
//    }
//}
//
//
//
//
////    /**
////     * Generates a CSV file of today's MIS records.
////     * @return File object pointing to the generated CSV.
////     * @throws Exception if file writing fails
////     */
////    public File generateDailyMISCSV() throws Exception {
////
////        LocalDate today = LocalDate.now();
////        LocalDateTime startOfDay = today.atStartOfDay();
////        LocalDateTime endOfDay = startOfDay.plusDays(1);
////
////        // Fetch today's MIS records
////     // Fetch today's MIS records
////        List<MIS> records = misRepository.findByCreateTimeBetween(startOfDay, endOfDay);
////
////        // Create CSV file in temp directory
////        String fileName = "MIS_" + today + ".csv";
////        File csvFile = Paths.get(System.getProperty("java.io.tmpdir"), fileName).toFile();
////
////        try (BufferedWriter writer = new BufferedWriter(
////                new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8))) {
////
////            // CSV header
////        	writer.write("id,mobileNumber,userId,customUserId,transactionId,clickId,journeyFlag,breFlag,productFlag,pahal,kissht,BFL,ABCL,aspireFin,status,offerFlag,createTime");
////            writer.newLine();
////
////            // CSV rows
////            for (MIS mis : records) {
////                String row = String.join(",",
////                        safe(mis.getId()),
////                        safe(mis.getMobileNumber()),
////                        safe(mis.getUser() != null ? mis.getUser().getId() : ""),
////                        safe(mis.getCustomUserId()),
////                        safe(mis.getTransactionId()),
////                        safe(mis.getClickId()),
////                        safe(mis.getJourneyFlag()),
////                        safe(mis.getBreFlag()),
////                        safe(mis.getProductFlag()),
////                        safe(mis.getPahal()),
////                        safe(mis.getKissht()),
////                        safe(mis.getBFL()),
////                        safe(mis.getABCL()),
////                        safe(mis.getAspireFin()),
////                        safe(mis.getStatus()),
////                        safe(mis.getOfferFlag()),
////                        safe(mis.getCreateTime())
////                );
////                writer.write(row);
////                writer.newLine();
////            }
////        }
////
////        System.out.println("CSV Generated: " + csvFile.getAbsolutePath());
////        return csvFile;
////    }
////
////    /**
////     * Converts object to string safely and removes commas to prevent CSV issues.
////     */
////    private String safe(Object obj) {
////        return obj == null ? "" : obj.toString().replace(",", " ");
////    }
////}