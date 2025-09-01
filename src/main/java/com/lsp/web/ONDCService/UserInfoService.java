package com.lsp.web.ONDCService;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsp.web.util.ApiResponse;

import io.micrometer.common.util.StringUtils;

import com.lsp.web.configuration.*;
import com.lsp.web.dto.UserInfoDto;
import com.lsp.web.entity.UserInfo;
import com.lsp.web.Exception.InvalidInputException;
import com.lsp.web.Exception.OtpValidationException;
import com.lsp.web.repository.UserInfoRepository;



@Service
public class UserInfoService {
	
		@Autowired
	    private UserInfoRepository userInfoRepository;
	    
	    @Autowired
	    private UserInfoMapper userInfoMapper;
	    
	    @Autowired
	    private UserBureauDataService userBureauDataService;

	    
	    
	//*****************new code for otp generation******************************/
	  	        
	        // Main method to save user - handles both new and existing users
	        public UserInfoDto saveUser(JSONObject input) {
	            try {
	                // 1. Basic validation
	                if (input == null) {
	                    throw new InvalidInputException("Request payload cannot be null");
	                }

	                // 2. Convert input to DTO
	                UserInfoDto dto = convertToDto(input);
	                
	                // 3. Check if mobile number already exists in database
	                Optional<UserInfo> existingUser = userInfoRepository.findByMobileNumber(dto.getMobileNumber());
	                UserInfo savedUser;
	                
	                if (existingUser.isPresent()) {
	                    // Mobile number exists - update existing record
	                    System.out.println("Mobile number already exists, updating existing user: " + dto.getMobileNumber());
	                    savedUser = updateExistingUser(existingUser.get(), dto);
	                } else {
	                    // Mobile number doesn't exist - create new record
	                    System.out.println("New mobile number, creating new user: " + dto.getMobileNumber());
	                    savedUser = saveNewUser(dto);
	                }

//	                // 4. Process OTP for the user
//	                ApiResponse otpResponse = processOtp(input);
//	                if (otpResponse.getCode() != 200) {
//	                    throw new OtpValidationException(otpResponse.getMsg());
//	                }

	                // 5. Return the saved user data
	                return userInfoMapper.toUserInfoDto(savedUser);

	            } catch (InvalidInputException | OtpValidationException e) {
	                throw e;
	            } catch (DataAccessException e) {
	                throw new RuntimeException("Database operation failed", e);
	            } catch (Exception e) {
	                throw new RuntimeException("Unexpected error during user registration", e);
	            }
	        }

	        // Convert JSON input to DTO
	        private UserInfoDto convertToDto(JSONObject input) {
	            try {
	                UserInfoDto dto = new UserInfoDto();
	                
	                // Required fields
	                String mobileNumber = input.getString("Mobilenumber");
	                if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
	                    throw new InvalidInputException("Mobile number is required");
	                }
	                
//	                String name = input.getString("name");
	                String firstName = input.getString("firstname");
	                String fatherName = input.optString("fathername", null);
	                String lastName = input.optString("lastname", null);
//	                if (name == null || name.trim().isEmpty()) {
//	                    throw new InvalidInputException("Name is required");
//	                }
	                
	                dto.setMobileNumber(
	                	    input.optString("mobileNumber", input.optString("Mobilenumber", null))
	                	);

	                	dto.setFirstName(input.optString("firstName", null));
	                	dto.setLastName(input.optString("lastName", null));
	                	dto.setFatherName(input.optString("fatherName", null));

	                	// if you require full name:
	                	if (StringUtils.isBlank(dto.getFirstName()) && StringUtils.isBlank(dto.getLastName())) {
	                	    dto.setFirstName(input.optString("name", null));
	                	}
	                
	                // Optional fields
//	                dto.setEmail(input.optString("email", null));
	                dto.setPan(input.optString("pan", null));
	                
	                return dto;
	            } catch (JSONException e) {
	                throw new InvalidInputException("Required fields (name, Mobilenumber) are missing in request");
	            }
	        }

	        // Save new user when mobile number doesn't exist
	        private UserInfo saveNewUser(UserInfoDto dto) {
	            try {
	                UserInfo entity = userInfoMapper.toUserInfo(dto);
//	                entity.setRegisterTime(new Date()); // Set current time as register time
	                
	                UserInfo savedEntity = userInfoRepository.save(entity);
	                System.out.println("New user saved successfully with ID: " + savedEntity.getId());
	                
	                return savedEntity;
	            } catch (DataIntegrityViolationException e) {
	                // This shouldn't happen since we already checked, but just in case
	                throw new InvalidInputException("User with same mobile number already exists");
	            } catch (Exception e) {
	                throw new RuntimeException("Failed to save new user", e);
	            }
	        }
	        
	        // Update existing user when mobile number already exists
	        private UserInfo updateExistingUser(UserInfo existingUser, UserInfoDto dto) {
	            try {
	                // Update only changeable fields
//	                existingUser.setName(dto.getName());
	            	existingUser.setFirstName(dto.getFirstName());
	            	existingUser.setFatherName(dto.getFatherName());
	            	existingUser.setLastName(dto.getLastName());

	                
	                // Only update email if provided
	                if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
	                    existingUser.setEmail(dto.getEmail());
	                }
	                
	                // Only update PAN if provided
	                if (dto.getPan() != null && !dto.getPan().trim().isEmpty()) {
	                    existingUser.setPan(dto.getPan());
	                }
	                
	                // Don't update mobile number and register time - keep original values
	                
	                UserInfo updatedEntity = userInfoRepository.save(existingUser);
	                System.out.println("Existing user updated successfully with ID: " + updatedEntity.getId());
	                
	                return updatedEntity;
	            } catch (DataIntegrityViolationException e) {
	                throw new InvalidInputException("Update failed - duplicate email might exist");
	            } catch (Exception e) {
	                throw new RuntimeException("Failed to update existing user", e);
	            }
	        }
	        
	        // Method to check if mobile number exists (utility method)
	        public boolean isMobileNumberExists(String mobileNumber) {
	            if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
	                return false;
	            }
	            return userInfoRepository.findByMobileNumber(mobileNumber.trim()).isPresent();
	        }
	        
	        // Process OTP for the user
	        public ApiResponse processOtp(JSONObject input) {
	           try {
	                String url = "https://loan.credithaat.com/d2cforinternal/otpgeneration";
//	                String url = "http://localhost/d2cforinternal/otpgeneration";

	                // Create request body for OTP API
	                JSONObject requestBody = new JSONObject();
	                requestBody.put("Mobilenumber", input.getString("Mobilenumber"));

	                // Split full name into first and last name
//	                String fullName = input.optString("name", "");
//	                String[] nameParts = fullName.trim().split("\\s+");
//	                String firstName = nameParts.length > 0 ? nameParts[0] : "";
//	                String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : "";

	                requestBody.put("Firstname", input.getString("firstname"));
	                requestBody.put("Lastname", input.getString("lastname"));
	                requestBody.put("email", input.optString("email", ""));
	                requestBody.put("PAN", input.optString("pan", ""));
	                requestBody.put("agent_id", input.optInt("agent_id", 1246569));
	                requestBody.put("agent", input.optString("agent", "BTI"));

	                // Set headers
	                HttpHeaders headers = new HttpHeaders();
	                headers.setContentType(MediaType.APPLICATION_JSON);
	                headers.set("token", "Y3JlZGl0aGFhdHRlc3RzZXJ2ZXI=");

	                HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

	                // Make API call
	                RestTemplate restTemplate = new RestTemplate();
	                ResponseEntity<String> jsonResponse = restTemplate.postForEntity(url, request, String.class);

	                // Parse response
	                JSONObject responseJson = new JSONObject(jsonResponse.getBody());

	                String otpStatus = responseJson.optString("otpGenerationStatusfromexperian", "0");
	                String stgOneHitId = responseJson.optString("stgOneHitId", null);
	                String stgTwoHitId = responseJson.optString("stgTwoHitId", null);

	                // Create response
	                ApiResponse response = new ApiResponse();
	                response.setStgOneHitId(stgOneHitId);
	                response.setStgTwoHitId(stgTwoHitId);
	                response.setOtpGenerationStatus(otpStatus);
	                response.setCode(200);
	                response.setMsg("OTP generated successfully");
	                
	                return response;

	            } catch (Exception e) {
	                e.printStackTrace();
	                ApiResponse error = new ApiResponse();
	                error.setCode(500);
	                error.setMsg("Error in OTP processing: " + e.getMessage());
	                return error;
	            }
	        }
	  

//	    } /********************************** Verify OTP *********************************************************/
//		    public Map<?,?> verifyOtp(JSONObject input) {
//		        try {
//		            // Prepare external API call
//		            HttpClient httpClient = HttpClients.createDefault();
//		           HttpPost httpPost = new HttpPost("https://loan.credithaat.com/d2cforinternal/otpvalidate");
//		            
////		           HttpPost httpPost = new HttpPost("http://localhost/d2cforinternal/otpvalidate");
//
//		            httpPost.setHeader("token", "Y3JlZGl0aGFhdHRlc3RzZXJ2ZXI="); // Replace with actual token
//		            httpPost.setHeader("Content-Type", "application/json");
//
//		            
//		            StringEntity entity = new StringEntity(input.toString());
//		            httpPost.setEntity(entity);
//
//		            HttpResponse httpResponse = httpClient.execute(httpPost);
//		            String jsonResponse = EntityUtils.toString(httpResponse.getEntity());
//
//		            System.out.println("Raw JSON Response from Experian API: " + jsonResponse);
//		            
////		            JSONObject responseJson = new JSONObject(jsonResponse);
////		            if (responseJson.has("errorString") && responseJson.getString("errorString") != null 
////		                && !responseJson.getString("errorString").trim().isEmpty()) {
////
////		                Map<String, Object> data = new HashMap<>();
////		                data.put("code", -1); // 👈 custom code for invalid OTP
////		                data.put("message", responseJson.getString("errorString"));
////		                return data;
////		            }
//		            JSONObject responseJson = new JSONObject(jsonResponse);
//		            if ("OTP validation failed, OTP is not match".equalsIgnoreCase(responseJson.optString("errorString"))) {
//		                Map<String, Object> data = new HashMap<>();
//		                data.put("code", -1); 
//		                data.put("message", responseJson.getString("errorString"));
//		                return data;
//		            }
//
//		            // Handle null or empty response
//		            if (jsonResponse == null || jsonResponse.trim().isEmpty() || jsonResponse.trim().equalsIgnoreCase("null")) {
//		            	Map<String, Object> data = new HashMap<>();
//		                data.put("score", null);
//		                data.put("active_account_count", 0);
//		                data.put("code", 2);
//		                data.put("message", "otp verified succesfully but experian not found");
//		                long finalScore = 20;
//		                data.put("finalScore", finalScore);
//
//			            
//		                String mobileNumber = input.optString("Mobilenumber", null);
//		                Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);
//
//			            if (optionalUser.isPresent()) {
//			                UserInfo user = optionalUser.get();
////			                user.setFinalScore((int)finalScore);  // ✅ Make sure this setter exists in your UserInfo entity
//			                userInfoRepository.save(user);
//			            }
//		                return data;
//
//
//		            }
//
//		            if (!jsonResponse.trim().startsWith("{")) {
//		                throw new Exception("Invalid response format. Not a JSON object: " + jsonResponse);
//		            }
//
//		            // Parse JSON response
//		            JSONObject json = new JSONObject(jsonResponse);
//
//		         // Step 1: Check if errorString is missing or null → means OTP is invalid
////		         String errorString = json.optString("errorString", null);
////		         if (errorString == null || errorString.trim().isEmpty()) {
////		             Map<String, Object> data = new HashMap<>();
////		             data.put("code", -1);
////		             data.put("message", "Invalid OTP");
////		             return data;
////		         }
//		            JSONObject inProfileResponse = json.optJSONObject("INProfileResponse");
//		            //JSONObject errorString = json.optJSONObject("errorString");
//
//		            if (inProfileResponse == null) { 
//		            	Map<String, Object> data = new HashMap<>();
//		                data.put("score", null);
//		                data.put("active_account_count", 0);
//			            //data.put("finalScore", finalScore);
//		                data.put("code", 2);//code 2 is for otp verified succesfully but experian not found
//		                data.put("message", "otp verified succesfully but experian not found");
//		                long finalScore = 20;
//		                data.put("finalScore", finalScore);
//		                String mobileNumber = input.optString("Mobilenumber", null);
//		                Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);
//
//			            if (optionalUser.isPresent()) {
//			                UserInfo user = optionalUser.get();
//			                user.setCreditProfile("1000");   // <-- update field
////		                    userInfoRepository.save(user);
////			                user.setFinalScore((int)finalScore);  // ✅ Make sure this setter exists in your UserInfo entity
//			                userInfoRepository.save(user);
//			            }
//		                return data;
//
//		            }
//		            
// 
//
//		            // Extract score
//		            JSONObject scoreJson = inProfileResponse.optJSONObject("SCORE");
//		            String score = scoreJson != null ? scoreJson.optString("BureauScore", null) : null;
//		            
////================================================this code is used to save  bereauresponse in table====== 
//
//		            String mobileNumber = input.optString("Mobilenumber", null);
//		            Optional<UserInfo> userInfoOpt = userInfoRepository.findByMobileNumber(mobileNumber);
//
//		            if (userInfoOpt.isPresent() && inProfileResponse != null) {
//		                UserInfo user = userInfoOpt.get();
//
//		                // Wrap the JSON in {"INProfileResponse": {...}}
//		                JSONObject wrappedResponse = new JSONObject();
//		                wrappedResponse.put("INProfileResponse", inProfileResponse);
//
//		                userBureauDataService.saveOrUpdateBureauData(
//		                    user,
//		                    score,
//		                    wrappedResponse.toString()
//		                );
//		                
//		                if (score != null) {
//		                    user.setCreditProfile(score);   // <-- update field
//		                    userInfoRepository.save(user);  // <-- persist changes
//		                }else {
//		                	user.setCreditProfile("1000");   // <-- update field
//		                    userInfoRepository.save(user);
//		                }
//		            }
//		            
//
//
////=======================================================================================================
//
//		            // Extract account info
//		            JSONObject caisAccountResponse = inProfileResponse.optJSONObject("CAIS_Account");
//		            if (caisAccountResponse == null) throw new Exception("Missing CAIS_Account");
//
//		            Object caisAccountDetailsObject = caisAccountResponse.opt("CAIS_Account_DETAILS");
//
//		            JSONArray accountDetails = caisAccountDetailsObject instanceof JSONArray
//		                    ? (JSONArray) caisAccountDetailsObject
//		                    : new JSONArray().put(caisAccountDetailsObject);
//		            
//		            
//		            
////==================================== Repayment Score Logic Starts ==================================================================
//		            ObjectMapper mapper = new ObjectMapper();
//		            List<Map<String, Object>> caisAccounts = mapper.readValue(
//		                accountDetails.toString(), new TypeReference<List<Map<String, Object>>>() {}
//		            );
//
//		            int totalMonths = 0;
//		            int delayedMonths = 0;
//
//		            for (Map<String, Object> account : caisAccounts) {
//		                @SuppressWarnings("unchecked")
//		                List<Map<String, Object>> history = (List<Map<String, Object>>) account.get("CAIS_Account_History");
//		                if (history == null) continue;
//
//		                for (Map<String, Object> month : history) {
//		                    Object dpdObj = month.get("Days_Past_Due");
//		                    if (dpdObj != null && !dpdObj.toString().trim().isEmpty()) {
//		                        try {
//		                            int dpd = Integer.parseInt(dpdObj.toString().trim());
//		                            totalMonths++;
//		                            if (dpd > 0) delayedMonths++;
//		                        } catch (NumberFormatException e) {
//		                            // Skip invalid values like "?"
//		                        }
//		                    }
//		                }
//		            }
//
//		            String repaymentScoreStr = "NA";
//		            if (totalMonths > 0) {
//		                int repaymentScore = (int) Math.round(((double)(totalMonths - delayedMonths) / totalMonths) * 100);
//		                repaymentScoreStr = String.valueOf(repaymentScore);
//		            }
//		            
//		            
//		            
//		            Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);
//
//		            if (optionalUser.isPresent()) {
//		                UserInfo user = optionalUser.get();
//
//		                // ✅ Set repayment score
////		                user.setRepaymentScore(repaymentScoreStr);
//
//		                // ✅ Save updated user
//		                userInfoRepository.save(user);
//		            }
//
//		            // ✅ Repayment Score Logic Ends
//
//		            
////================================ Get report date for DPD calculations ================================================================================
//		            
//		            
//		            Integer reportDate = inProfileResponse.optJSONObject("Header").optInt("ReportDate");
//		            LocalDate buro_report_fetch_date = LocalDate.parse(String.format("%08d", reportDate), DateTimeFormatter.BASIC_ISO_DATE);
//		            LocalDate buro_report_minus_one_month = buro_report_fetch_date.minusMonths(1);
//
//
//		            
//		            int maxVintageInMonths = calculateMaxVintageInMonths(accountDetails);
//		            double totalAmountPastDue = 0;
//		            
//
//		            int suitPoints = calculateSuitFiledWrittenOffPoints(json);
//		           
//
//		            // Initialize DPD counters for different time periods
//		            Map<String, Integer> dpdCounts = new HashMap<>();
//		            dpdCounts.put("last_3_months_dpd_count", 0);
//		            dpdCounts.put("last_6_months_dpd_count", 0);
//		            dpdCounts.put("last_12_months_dpd_count", 0);
//		            dpdCounts.put("last_24_months_dpd_count", 0);
//		            dpdCounts.put("last_36_months_dpd_count", 0);
//
//		            // Initialize max DPD tracking
//		            Map<String, Integer> maxDpd = new HashMap<>();
//		            maxDpd.put("max_dpd_3_months", 0);
//		            maxDpd.put("max_dpd_6_months", 0);
//		            maxDpd.put("max_dpd_12_months", 0);
//		            maxDpd.put("max_dpd_24_months", 0);
//		            maxDpd.put("max_dpd_36_months", 0);
//
//		            int totalUnsecuredLoans = 0;
//		            int unsecuredLoansLast30 = 0;
//		            int unsecuredLoansLast90 = 0;
//		            int lowAmountLoans30 = 0;
//		            int lowAmountLoans90 = 0;
//		            int active_account_count = 0;
//		            List<String> unsecuredTypes = Arrays.asList("61", "69", "71", "24", "5");
//		            
//			         // Replace the existing credit utilization logic in your verifyOtp method with this corrected version
//			         // Initialize totals
//			         double totalCCBalance = 0.0;
//			         double totalCCLimit = 0.0;
//			         double totalBalanceAll = 0.0;
//			         double totalLimitAll = 0.0;
//			         
//			         double totalLoanOriginalAmount = 0.0;
//			         double totalLoanCurrentBalance = 0.0;
//
//
//
//		            for (int i = 0; i < accountDetails.length(); i++) {
//		                JSONObject account = accountDetails.getJSONObject(i);
//		                Object statusValue = account.opt("Account_Status");
//		                if (accountStatusChecker(statusValue)) {
//		                    active_account_count++;
//		                }
//		                
//		                // ✅ Unsecured loan logic
//		                String accountType = account.optString("Account_Type");
//		                String dateClosed = account.optString("Date_Closed");
//		                String openDateStr = account.optString("Date_Opened");
//		                double origLoanAmt = account.optDouble("Highest_Credit_or_Original_Loan_Amount", 0);
//		                double creditLimit = account.optDouble("Credit_Limit_Amount", 0.0);
//		                double balance = account.optDouble("Current_Balance", 0.0);
//		                // For Credit Cards (Account_Type = "10")
//		                if ("10".equals(accountType) && (dateClosed == null || dateClosed.isEmpty())) {
//		                    // Only count active/open credit cards
//		                    totalCCBalance += balance;
//		                    totalCCLimit += creditLimit;
//		                    System.out.println("CC Account - Balance: " + balance + ", Limit: " + creditLimit);
//		                }
//		                // For all accounts with credit limits (for overall utilization)
//		                if (creditLimit > 0 && (dateClosed == null || dateClosed.isEmpty())) {
//		                    totalBalanceAll += balance;
//		                    totalLimitAll += creditLimit;
//		                }
//		                
//		             // ✅ Loan Utilization Score Logic (excluding Credit Cards)
//		                if (!"10".equals(accountType) && (dateClosed == null || dateClosed.isEmpty())) {
//		                	double loanOrigAmount = account.optDouble("Highest_Credit_or_Original_Loan_Amount", 0);
//		                    double loanBalance = account.optDouble("Current_Balance", 0);
//		                    totalLoanOriginalAmount += loanOrigAmount;
//		                    totalLoanCurrentBalance += loanBalance;
//		                }
//
//		                
//		                // Check if it's an unsecured loan that's still open
//		                if (unsecuredTypes.contains(accountType) && (dateClosed == null || dateClosed.isEmpty())) {
//		                    totalUnsecuredLoans++;
//
//		                    if (openDateStr != null && !openDateStr.isEmpty()) {
//		                        try {
//		                            LocalDate openDate = LocalDate.parse(openDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
//		                            long daysBetween = ChronoUnit.DAYS.between(openDate, buro_report_fetch_date);
//
//		                            if (daysBetween <= 30) {
//		                                unsecuredLoansLast30++;
//		                                if (origLoanAmt < 10000) {
//		                                    lowAmountLoans30++;
//		                                }
//		                            }
//		                            if (daysBetween <= 90) {
//		                                unsecuredLoansLast90++;
//		                                if (origLoanAmt < 10000) {
//		                                    lowAmountLoans90++;
//		                                }
//		                            }
//		                        } catch (Exception e) {
//		                            System.err.println("Error parsing date: " + openDateStr + " - " + e.getMessage());
//		                        }
//		                    }
//		                }
//			             // For Credit Cards (Account_Type = "10")
//			             if ("10".equals(accountType) && (dateClosed == null || dateClosed.isEmpty())) {
//			                 // Only count active/open credit cards
//			                 totalCCBalance += balance;
//			                 totalCCLimit += creditLimit;
//			                 System.out.println("CC Account - Balance: " + balance + ", Limit: " + creditLimit);
//			             }
//
//			             // For all accounts with credit limits (for overall utilization)
//			             if (creditLimit > 0 && (dateClosed == null || dateClosed.isEmpty())) {
//			                 totalBalanceAll += balance;
//			                 totalLimitAll += creditLimit;
//			             }
//		             
//		                double amountPastDue = 0;
//		                try {
//		                    amountPastDue = account.optDouble("Amount_Past_Due", 0);
//		                } catch (Exception e) {
//		                    // ignore
//		                }
//		                totalAmountPastDue += amountPastDue;
//		                
//		                // Process DPD history for this account
//		                Object historyValue = account.opt("CAIS_Account_History");
//		                if (historyValue != null) {
//		                    Map<String, Integer> accountDpdData = processAccountDPDHistory(historyValue, buro_report_minus_one_month);
//		                    
//		                    // Add account DPD data to overall counts
//		                    dpdCounts.put("last_3_months_dpd_count", 
//		                        dpdCounts.get("last_3_months_dpd_count") + accountDpdData.get("dpd_3_months"));
//		                    dpdCounts.put("last_6_months_dpd_count", 
//		                        dpdCounts.get("last_6_months_dpd_count") + accountDpdData.get("dpd_6_months"));
//		                    dpdCounts.put("last_12_months_dpd_count", 
//		                        dpdCounts.get("last_12_months_dpd_count") + accountDpdData.get("dpd_12_months"));
//		                    dpdCounts.put("last_24_months_dpd_count", 
//		                        dpdCounts.get("last_24_months_dpd_count") + accountDpdData.get("dpd_24_months"));
//		                    dpdCounts.put("last_36_months_dpd_count", 
//		                        dpdCounts.get("last_36_months_dpd_count") + accountDpdData.get("dpd_36_months"));
//
//		                    // Update max DPD values
//		                    maxDpd.put("max_dpd_3_months", 
//		                        Math.max(maxDpd.get("max_dpd_3_months"), accountDpdData.get("max_dpd_3_months")));
//		                    maxDpd.put("max_dpd_6_months", 
//		                        Math.max(maxDpd.get("max_dpd_6_months"), accountDpdData.get("max_dpd_6_months")));
//		                    maxDpd.put("max_dpd_12_months", 
//		                        Math.max(maxDpd.get("max_dpd_12_months"), accountDpdData.get("max_dpd_12_months")));
//		                    maxDpd.put("max_dpd_24_months", 
//		                        Math.max(maxDpd.get("max_dpd_24_months"), accountDpdData.get("max_dpd_24_months")));
//		                    maxDpd.put("max_dpd_36_months", 
//		                        Math.max(maxDpd.get("max_dpd_36_months"), accountDpdData.get("max_dpd_36_months")));
//		                }
//		                
//		                
//
//		                // You can log or use unsecuredLoanCount here
//
//		            
//		        }
//		            // Scoring logic (individual category scores)
//		            int unsecuredScoreOverall = 0;
//		            int unsecuredScoreLast30 = 0;
//		            int lowAmt30Score = 0;
//		            int lowAmt90Score = 0;
//
//		            if (totalUnsecuredLoans == 0) unsecuredScoreOverall = 20;
//		            else if (totalUnsecuredLoans <= 3) unsecuredScoreOverall = 15;
//
//		            if (unsecuredLoansLast30 == 0) unsecuredScoreLast30 = 20;
//		            else if (unsecuredLoansLast30 == 1) unsecuredScoreLast30 = 10;
//
//		            if (lowAmountLoans30 == 0) lowAmt30Score = 20;
//		            else if (lowAmountLoans30 <= 2) lowAmt30Score = 10;
//		            else lowAmt30Score = 0;
//
//		            if (lowAmountLoans90 == 0) lowAmt90Score = 20;
//		            else if (lowAmountLoans90 <= 3) lowAmt90Score = 5;
//		            else lowAmt90Score = 0;
//
//
//		         //======================Calculate CC Utilization Score==============================
//		         int ccUtilScore = 0;
//		         double ccUtilPercent = 0.0;
//		         if (totalCCLimit > 0) {
//		             ccUtilPercent = (totalCCBalance / totalCCLimit) * 100;
//
//		             if (ccUtilPercent < 50) {
//		                 ccUtilScore = 50;
//		             } else if (ccUtilPercent >= 50 && ccUtilPercent <= 75) {
//		                 ccUtilScore = 20;
//		             } else if (ccUtilPercent > 75 && ccUtilPercent <= 90) {
//		                 ccUtilScore = 10;
//		             } else if (ccUtilPercent > 90) {
//		                 ccUtilScore = 0;
//		             }
//		         } else {
//		             // No credit cards or no credit limit data
//		             ccUtilScore = 0; // or you might want to set this to a default value
//		             ccUtilPercent = 0.0;
//		         }
//	
//		         
////=============================Calculate Overall Credit Utilization Score=====================
//		         int creditUtilScore = 0;
//		         double overallUtilPercent = 0.0;
//		         if (totalLimitAll > 0) {
//		             overallUtilPercent = (totalBalanceAll / totalLimitAll) * 100;
//
//		             if (overallUtilPercent < 30) {
//		                 creditUtilScore = 5;
//		             } else if (overallUtilPercent >= 30 && overallUtilPercent <= 50) {
//		                 creditUtilScore = 3;
//		             } else if (overallUtilPercent > 50) {
//		                 creditUtilScore = 1;
//		             }
//		         } else {
//		             // No accounts with credit limits
//		             creditUtilScore = 0;
//		             overallUtilPercent = 0.0;
//		         }
////==========================================================================================================		         
//		      
//// ===================================== Calculate Loan Utilization Ratio and Score=========================
//		         int loanUtilScore = 0;
//		         double loanUtilRatio = 0.0;
//
//		         if (totalLoanOriginalAmount > 0) {
//		        	 loanUtilRatio = (totalLoanCurrentBalance / totalLoanOriginalAmount) * 100;
//
//		             if (loanUtilRatio < 30) {  //---------------0.3 = 30%
//		                 loanUtilScore = 5;
//		             } else if (loanUtilRatio <= 50) { //---------------0.5 = 50%
//		                 loanUtilScore = 3;
//		             } else {
//		                 loanUtilScore = 1;
//		             }
//		         }
//
////=========================================================================================================
//		            
//		         int activeAccountPoints = calculateActiveAccountPoints(active_account_count);
//
//		            // Calculate DPD-based points
//		            Map<String, Integer> dpdPoints = calculateDPDPoints(dpdCounts, maxDpd);
//		            //Map<String, Object> unsecuredMetrics = new UserInfoService().calculateMetrics(accountDetails, reportDateString);
//
//		            int overduePoints = calculateOverduePoints(totalAmountPastDue);
//		            int scorePoints = calculateScorePoints(score);
//		            int vintagePoints = calculateVintagePoints(maxVintageInMonths);		    
//		            
//		        //    JSONObject inProfileResponse = input.optJSONObject("INProfileResponse");
//		            JSONObject totalCAPSSummary = inProfileResponse != null ? inProfileResponse.optJSONObject("TotalCAPS_Summary") : null;
//
//		            int totalCAPSLast30Days = totalCAPSSummary != null ? totalCAPSSummary.optInt("TotalCAPSLast30Days", 0) : 0;
//		            int totalCAPSLast90Days = totalCAPSSummary != null ? totalCAPSSummary.optInt("TotalCAPSLast90Days", 0) : 0;
//
//		            int points30 = getPointsFor30Days(totalCAPSLast30Days);
//		            int points90 = getPointsFor90Days(totalCAPSLast90Days);
//
//
//
//	
//		            int dpd3MaxDaysPoints = dpdPoints.get("dpd_3_months_max_days_points");
//		            int dpd6MaxDaysPoints = dpdPoints.get("dpd_6_months_max_days_points");
//		            int dpd12MaxDaysPoints = dpdPoints.get("dpd_12_months_max_days_points");
//		            int dpd24MaxDaysPoints = dpdPoints.get("dpd_24_months_max_days_points");
//		            int dpd36MaxDaysPoints = dpdPoints.get("dpd_36_months_max_days_points");
//		            int dpd3CountPoints = dpdPoints.get("dpd_3_months_count_points");
//		            int dpd6CountPoints = dpdPoints.get("dpd_6_months_count_points");
//		            int dpd12CountPoints = dpdPoints.get("dpd_12_months_count_points");
//		            int dpd36CountPoints = dpdPoints.get("dpd_36_months_count_points");
//		            
//
//
//		            // Prepare response
//		            Map<String, Object> data = new HashMap<>();
//		            data.put("score", score);
//		            data.put("score_points", scorePoints);
//		            data.put("max_vintage_in_months", maxVintageInMonths);
//		            data.put("vintage_points", vintagePoints);
//		            data.put("active_account_count", active_account_count);
//		            data.put("active_account_points", activeAccountPoints);
//		            data.put("total_amount_past_due", totalAmountPastDue);
//		            data.put("overdue_points", overduePoints);	
//		            data.put("suitfiled_writtenoff_points", suitPoints);
//
//		            data.put("Points_CAPS_30_Days", points30);
//		            data.put("Points_CAPS_90_Days", points90);
//
//		            
//		            // Add individual DPD points in your requested format
//		            data.put("dpd_3_max_days_points", dpd3MaxDaysPoints);
//		            data.put("dpd_6_max_days_points", dpd6MaxDaysPoints);
//		            data.put("dpd_12_max_days_points", dpd12MaxDaysPoints);
//		            data.put("dpd_24_max_days_points", dpd24MaxDaysPoints);
//		            data.put("dpd_36_max_days_points", dpd36MaxDaysPoints);
//		            data.put("dpd_3_count_points", dpd3CountPoints);
//		            data.put("dpd_6_count_points", dpd6CountPoints);
//		            data.put("dpd_12_count_points", dpd12CountPoints);
//		            data.put("dpd_36_count_points", dpd36CountPoints);
//		            // Add DPD counts
//		            data.putAll(dpdCounts);
//		            
//		            // Add max DPD values
//		            data.putAll(maxDpd);
//		            
//		            // Add individual DPD points as requested
//		            data.put("dpd_3m_points", dpdPoints.get("dpd_3_months_max_days_points"));
//		            data.put("dpd_6m_points", dpdPoints.get("dpd_6_months_max_days_points"));
//		            data.put("dpd_12m_points", dpdPoints.get("dpd_12_months_max_days_points"));
//		            data.put("dpd_24m_points", dpdPoints.get("dpd_24_months_max_days_points"));
//		            data.put("dpd_36m_points", dpdPoints.get("dpd_36_months_max_days_points"));
//		            data.put("dpd_count_3m_points", dpdPoints.get("dpd_3_months_count_points"));
//		            data.put("dpd_count_6m_points", dpdPoints.get("dpd_6_months_count_points"));
//		            data.put("dpd_count_12m_points", dpdPoints.get("dpd_12_months_count_points"));
//		            data.put("dpd_count_36m_points", dpdPoints.get("dpd_36_months_count_points"));
//		            data.put("suitfiled_writtenoff_points", suitPoints);	
//		            
//		            // Add total DPD score
//		            data.put("total_dpd_score", dpdPoints.get("total_dpd_score"));
//		            
//		            // Add overall DPD summary
//		            data.put("dpd_summary", createDPDSummary(dpdCounts, maxDpd, dpdPoints));
//		            
//		   		 // ✅ Store values in your response map
//		            data.put("UnsecuredLoanScore", unsecuredScoreOverall);
//		            data.put("TotalUnsecuredLoans", totalUnsecuredLoans);
//		            data.put("UnsecuredLoansLast30Days", unsecuredScoreLast30);
//		            data.put("UnsecuredLoansLast90Days", unsecuredLoansLast90);//just counted not to use 
//		            data.put("LowAmountLoans30Days", lowAmt30Score);
//		            data.put("LowAmountLoans90Days", lowAmt90Score);
//	
//		            data.put("loanUtilRatio", String.valueOf(loanUtilRatio));
//		            data.put("loanUtilScore", loanUtilScore);
//
//		            data.put("CC_Utilization_Score", String.valueOf(ccUtilScore));//all  creditcard utilization ratio
//		            data.put("Credit_Utilization_Score", creditUtilScore);
//		            data.put("Overall_Credit_Utilization_Percent",overallUtilPercent);
//		            data.put("CC_Utilization_Percent",ccUtilPercent); // Round to 2 decimal places
//
//		            //data.put("CC_Utilization_Percent", Math.round(ccUtilPercent * 100.0) / 100.0); // Round to 2 decimal places
//		           // data.put("Overall_Credit_Utilization_Percent", Math.round(overallUtilPercent * 100.0) / 100.0);
//		            data.put("Total_CC_Balance", totalCCBalance);
//		            data.put("Total_CC_Limit", totalCCLimit);
//		            data.put("Total_Overall_Balance", totalBalanceAll);
//		            data.put("Total_Overall_Limit", totalLimitAll);
//		            
//		            //data.put("Loan_Utilization_Score", loanUtilScore);//total approved amount and current balance ratio for all active  loans 
//		            //data.put("Loan_Utilization_Ratio", Math.round(loanUtilRatio * 100.0) / 100.0); // in %, rounded to 2 decimals	            
//		            data.put("Total_Loan_Original_Amount", totalLoanOriginalAmount);
//		            data.put("Total_Loan_Current_Balance", totalLoanCurrentBalance);
//		            data.put("repaymentScore", repaymentScoreStr);
//		            double lcScore;
//		           // lcScore = (loanUtilRatio + ccUtilPercent)/2;
//		            
//		           lcScore = ((totalCCBalance+totalLoanCurrentBalance)/(totalCCLimit+totalLoanOriginalAmount))*100;
//		        // ✅ Round to 1 decimal places (truncate, not round up)
//		           lcScore = Math.round(lcScore * 10.0) / 10.0;
//		           data.put("Loan_creditCard_utilization_percentage",lcScore);
//		            long finalScore;
//		            finalScore = scorePoints+vintagePoints+activeAccountPoints+overduePoints+suitPoints+dpd3MaxDaysPoints+dpd6MaxDaysPoints+dpd12MaxDaysPoints+dpd24MaxDaysPoints+dpd36MaxDaysPoints+dpd3CountPoints+dpd6CountPoints+dpd12CountPoints+dpd36CountPoints
//		            +points90+points30+unsecuredScoreOverall+unsecuredScoreLast30+lowAmt30Score+lowAmt90Score+ccUtilScore+loanUtilScore;
//		            data.put("finalScore", finalScore);
//		            if (optionalUser.isPresent()) {
//		                UserInfo user = optionalUser.get();
////		                user.setFinalScore((int)finalScore);  // ✅ Make sure this setter exists in your UserInfo entity
//		                userInfoRepository.save(user);
//		            }
//		            
//		            //  data.put("unsecured_loan_purpose_code", unsecuredLoanPurpose);  // <==stored unsecuredloan count
//		            data.put("code", 1);//code 1 is for otp verified succesfully
//		            data.put("message", "otp verified succesfully");
//		            
//		            return data;
//
//		        }
//		        
//		        catch (Exception e) {
//		            e.printStackTrace();
//		            Map<String, Object> data = new HashMap<>();
//		            data.put("code", -1);//code -1 stands for OTP failed
//		            data.put("message", "OTP verification failed: " + e.getMessage());
//
//		            return data;
//
//		        }
//		    }
//
//
//	//============================================= verify otp end ===================================================================
//	
//		    
//	
//		    
//		    
//	//============================================ vintage calculate ==================================================================
//		    
//		    private int calculateMaxVintageInMonths(JSONArray accountDetails) {
//		        int maxVintageInMonths = 0;
//
//		        String[] dateFormats = new String[]{
//		                "yyyyMMdd", "yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy",
//		                "MM/dd/yyyy", "yyyy/MM/dd", "dd MMM yyyy", "MMM dd, yyyy"
//		        };
//
//		        for (int i = 0; i < accountDetails.length(); i++) {
//		            JSONObject account = null;
//					try {
//						account = accountDetails.getJSONObject(i);
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//		            String openDateStr = account.optString("Open_Date", "").trim();
//
//		            if (!openDateStr.isEmpty()) {
//		                Date openDate = null;
//
//		                for (String format : dateFormats) {
//		                    try {
//		                        SimpleDateFormat sdf = new SimpleDateFormat(format);
//		                        sdf.setLenient(false);
//		                        openDate = sdf.parse(openDateStr);
//		                        break;
//		                    } catch (Exception ignored) {
//		                    }
//		                }
//
//		                if (openDate == null) {
//		                    System.out.println("Could not parse Open_Date: " + openDateStr);
//		                    continue;
//		                }
//
//		                Calendar openCal = Calendar.getInstance();
//		                openCal.setTime(openDate);
//
//		                Calendar now = Calendar.getInstance();
//		                int monthsDiff = (now.get(Calendar.YEAR) - openCal.get(Calendar.YEAR)) * 12 +
//		                        (now.get(Calendar.MONTH) - openCal.get(Calendar.MONTH));
//
//		                if (monthsDiff > maxVintageInMonths) {
//		                    maxVintageInMonths = monthsDiff;
//		                }
//		            }
//		        }
//
//		        return maxVintageInMonths;
//		        
//		    }
//// ================================================= vintage points =============================================================
//		    private int calculateVintagePoints(int months) {
//		        if (months > 24) return 20;
//		        if (months >= 12) return 10;
//		        if (months >= 6) return 5;
//		        return 0;
//		    }
////===============================================================================================================================
////====================================== score points ===========================================================================
//		    private int calculateScorePoints(String score) {
//		        if (score == null || score.trim().isEmpty()) {
//		            return 20; // NTC
//		        }
//
//		        try {
//		            int scoreValue = Integer.parseInt(score);
//		            if (scoreValue > 780) return 100;
//		            if (scoreValue > 720) return 80;
//		            if (scoreValue >= 700) return 60;
//		            if (scoreValue >= 670) return 50;
//		            if (scoreValue >= 650) return 20;
//		            return 10;
//		        } catch (Exception e) {
//		            return 20; // fallback
//		        }
//		    }
//		    
////==========================================================================================================================
//		    
//		    
//		    
//// =============================================== overdues amount ==========================================================
//		    
//		    private int calculateOverduePoints(double totalOverdueAmount) {
//		        if (totalOverdueAmount == 0 ) {
//		            return 50;
//		        } else if (totalOverdueAmount > 0 && totalOverdueAmount <= 2000) {
//		            return 20;
//		        } else if (totalOverdueAmount > 2000 && totalOverdueAmount <= 5000) {
//		            return 0;
//		        } else if (totalOverdueAmount > 5000) {
//		            return -50;
//		        }
//		        return 0; // default fallback
//		    }
////=========================================================================================
//
//
//		    private Map<String, Object> createErrorResponse(String message, int code) {
//		        Map<String, Object> data = new HashMap<>();
//		        data.put("score", null);
//		        data.put("active_account_count", 0);
//		        data.put("code", code);
//		        data.put("message", message);
//		        return data;
//		    }
//		
//		    
////================================================= dpd =================================================================================
//		 // Process DPD history for a single account
//		    public Map<String, Integer> processAccountDPDHistory(Object historyValue, LocalDate buro_report_minus_one_month) {
//		        Map<String, Integer> result = new HashMap<>();
//		        
//		        // Initialize counters
//		        result.put("dpd_3_months", 0);
//		        result.put("dpd_6_months", 0);
//		        result.put("dpd_12_months", 0);
//		        result.put("dpd_24_months", 0);
//		        result.put("dpd_36_months", 0);
//		        result.put("max_dpd_3_months", 0);
//		        result.put("max_dpd_6_months", 0);
//		        result.put("max_dpd_12_months", 0);
//		        result.put("max_dpd_24_months", 0);
//		        result.put("max_dpd_36_months", 0);
//		        
//		        try {
//		            // Create a unified array to process
//		            JSONArray historyArray = historyValue instanceof JSONArray 
//		                ? (JSONArray) historyValue 
//		                : new JSONArray().put((JSONObject) historyValue);
//		            
//		            for (int j = 0; j < historyArray.length(); j++) {
//		                JSONObject caisAccountHistory = historyArray.optJSONObject(j);
//		                
//		                Integer daysPastDue = caisAccountHistory.optInt("Days_Past_Due");
//		                Integer month = caisAccountHistory.optInt("Month");
//		                Integer year = caisAccountHistory.optInt("Year");
//		                
//		                String formattedDate = formatDate(year, month);
//		                LocalDate startDate = LocalDate.parse(formattedDate + "-01");
//		                
//		                // Calculate months between dates
//		                long total_month = ChronoUnit.MONTHS.between(startDate, buro_report_minus_one_month);
//		                
//		                // Count DPD occurrences and track max DPD for different time periods
//		                if (daysPastDue > 0) {
//		                    if (total_month <= 3) {
//		                        result.put("dpd_3_months", result.get("dpd_3_months") + 1);
//		                        result.put("max_dpd_3_months", Math.max(result.get("max_dpd_3_months"), daysPastDue));
//		                    }
//		                    if (total_month <= 6) {
//		                        result.put("dpd_6_months", result.get("dpd_6_months") + 1);
//		                        result.put("max_dpd_6_months", Math.max(result.get("max_dpd_6_months"), daysPastDue));
//		                    }
//		                    if (total_month <= 12) {
//		                        result.put("dpd_12_months", result.get("dpd_12_months") + 1);
//		                        result.put("max_dpd_12_months", Math.max(result.get("max_dpd_12_months"), daysPastDue));
//		                    }
//		                    if (total_month <= 24) {
//		                        result.put("dpd_24_months", result.get("dpd_24_months") + 1);
//		                        result.put("max_dpd_24_months", Math.max(result.get("max_dpd_24_months"), daysPastDue));
//		                    }
//		                    if (total_month <= 36) {
//		                        result.put("dpd_36_months", result.get("dpd_36_months") + 1);
//		                        result.put("max_dpd_36_months", Math.max(result.get("max_dpd_36_months"), daysPastDue));
//		                    }
//		                }
//		            }
//		        } catch (Exception e) {
//		            System.err.println("Error processing account history: " + e.getMessage());
//		        }
//		        
//		        return result;
//		    }
//		    
//
//		    // Calculate points based on DPD analysis as per your exact requirements
//		    public Map<String, Integer> calculateDPDPoints(Map<String, Integer> dpdCounts, Map<String, Integer> maxDpd) {
//		        Map<String, Integer> points = new HashMap<>();
//		        
//		        // DPD Max Days Points - Last 3 Months
//		        int dpd3MaxDaysPoints = calculateDPDMaxDaysPoints_3M(maxDpd.get("max_dpd_3_months"));
//		        points.put("dpd_3_months_max_days_points", dpd3MaxDaysPoints);
//		        
//		        // DPD Max Days Points - Last 6 Months
//		        int dpd6MaxDaysPoints = calculateDPDMaxDaysPoints_6M(maxDpd.get("max_dpd_6_months"));
//		        points.put("dpd_6_months_max_days_points", dpd6MaxDaysPoints);
//		        
//		        // DPD Max Days Points - Last 12 Months
//		        int dpd12MaxDaysPoints = calculateDPDMaxDaysPoints_12M(maxDpd.get("max_dpd_12_months"));
//		        points.put("dpd_12_months_max_days_points", dpd12MaxDaysPoints);
//		        
//		        // DPD Max Days Points - Last 24 Months
//		        int dpd24MaxDaysPoints = calculateDPDMaxDaysPoints_24M(maxDpd.get("max_dpd_24_months"));
//		        points.put("dpd_24_months_max_days_points", dpd24MaxDaysPoints);
//		        
//		        // DPD Max Days Points - Last 36 Months
//		        int dpd36MaxDaysPoints = calculateDPDMaxDaysPoints_36M(maxDpd.get("max_dpd_36_months"));
//		        points.put("dpd_36_months_max_days_points", dpd36MaxDaysPoints);
//		        
//		        // DPD Count Points - Last 3 Months
//		        int dpd3CountPoints = calculateDPDCountPoints_3M(dpdCounts.get("last_3_months_dpd_count"));
//		        points.put("dpd_3_months_count_points", dpd3CountPoints);
//		        
//		        // DPD Count Points - Last 6 Months
//		        int dpd6CountPoints = calculateDPDCountPoints_6M(dpdCounts.get("last_6_months_dpd_count"));
//		        points.put("dpd_6_months_count_points", dpd6CountPoints);
//		        
//		        // DPD Count Points - Last 12 Months
//		        int dpd12CountPoints = calculateDPDCountPoints_12M(dpdCounts.get("last_12_months_dpd_count"));
//		        points.put("dpd_12_months_count_points", dpd12CountPoints);
//		        
//		        // DPD Count Points - Last 36 Months
//		        int dpd36CountPoints = calculateDPDCountPoints_36M(dpdCounts.get("last_36_months_dpd_count"));
//		        points.put("dpd_36_months_count_points", dpd36CountPoints);
//		        
//		        // Calculate total DPD score
//		        int totalDpdScore = dpd3MaxDaysPoints + dpd6MaxDaysPoints + dpd12MaxDaysPoints + 
//		                           dpd24MaxDaysPoints + dpd36MaxDaysPoints + dpd3CountPoints + 
//		                           dpd6CountPoints + dpd12CountPoints + dpd36CountPoints;
//		        
//		        points.put("total_dpd_score", totalDpdScore);
//		        
//		        return points;
//		    }
//
//		    // DPD Max Days Points - Last 3 Months
//		    private int calculateDPDMaxDaysPoints_3M(int maxDpdDays) {
//		        if (maxDpdDays == 0) return 20;
//		        if (maxDpdDays >= 1 && maxDpdDays <= 5) return 10;
//		        if (maxDpdDays >= 6 && maxDpdDays <= 10) return 5;
//		        if (maxDpdDays > 10) return 0;
//		        return 0;
//		    }
//
//		    // DPD Max Days Points - Last 6 Months
//		    private int calculateDPDMaxDaysPoints_6M(int maxDpdDays) {
//		        if (maxDpdDays == 0) return 20;
//		        if (maxDpdDays >= 1 && maxDpdDays <= 5) return 10;
//		        if (maxDpdDays >= 6 && maxDpdDays <= 10) return 5;
//		        if (maxDpdDays > 10) return 0;
//		        return 0;
//		    }
//
//		    // DPD Max Days Points - Last 12 Months
//		    private int calculateDPDMaxDaysPoints_12M(int maxDpdDays) {
//		        if (maxDpdDays == 0) return 20;
//		        if (maxDpdDays >= 1 && maxDpdDays <= 15) return 10;
//		        if (maxDpdDays >= 16 && maxDpdDays <= 30) return 5;
//		        if (maxDpdDays > 30) return 0;
//		        return 0;
//		    }
//
//		    // DPD Max Days Points - Last 24 Months
//		    private int calculateDPDMaxDaysPoints_24M(int maxDpdDays) {
//		        if (maxDpdDays == 0) return 20;
//		        if (maxDpdDays >= 1 && maxDpdDays <= 30) return 10;
//		        if (maxDpdDays >= 31 && maxDpdDays <= 60) return 5;
//		        if (maxDpdDays > 60) return 0;
//		        return 0;
//		    }
//
//		    // DPD Max Days Points - Last 36 Months
//		    private int calculateDPDMaxDaysPoints_36M(int maxDpdDays) {
//		        if (maxDpdDays == 0) return 20;
//		        if (maxDpdDays >= 1 && maxDpdDays <= 30) return 15;
//		        if (maxDpdDays >= 31 && maxDpdDays <= 60) return 10;
//		        if (maxDpdDays >= 61 && maxDpdDays <= 90) return 5;
//		        if (maxDpdDays > 90) return 0;
//		        return 0;
//		    }
//
//		    // DPD Count Points - Last 3 Months
//		    private int calculateDPDCountPoints_3M(int dpdCount) {
//		        if (dpdCount == 0) return 10;
//		        if (dpdCount == 1) return 5;
//		        if (dpdCount > 1) return 0;
//		        return 0;
//		    }
//
//		    // DPD Count Points - Last 6 Months
//		    private int calculateDPDCountPoints_6M(int dpdCount) {
//		        if (dpdCount == 0) return 10;
//		        if (dpdCount >= 1 && dpdCount <= 2) return 5;
//		        if (dpdCount > 2) return 0;
//		        return 0;
//		    }
//
//		    // DPD Count Points - Last 12 Months
//		    private int calculateDPDCountPoints_12M(int dpdCount) {
//		        if (dpdCount == 0) return 10;
//		        if (dpdCount >= 1 && dpdCount <= 3) return 5;
//		        if (dpdCount > 3) return 0;
//		        return 0;
//		    }
//
//		    // DPD Count Points - Last 36 Months
//		    private int calculateDPDCountPoints_36M(int dpdCount) {
//		        if (dpdCount == 0) return 10;
//		        if (dpdCount >= 1 && dpdCount <= 3) return 5;
//		        if (dpdCount > 3) return 0;
//		        return 0;
//		    }
//
//		    // Create DPD summary object with detailed breakdown
//		    private Map<String, Object> createDPDSummary(Map<String, Integer> dpdCounts, Map<String, Integer> maxDpd, Map<String, Integer> dpdPoints) {
//		        Map<String, Object> summary = new HashMap<>();
//		        
//		        // DPD Counts Summary
//		        summary.put("dpd_counts_summary", Map.of(
//		            "last_3_months", dpdCounts.get("last_3_months_dpd_count"),
//		            "last_6_months", dpdCounts.get("last_6_months_dpd_count"),
//		            "last_12_months", dpdCounts.get("last_12_months_dpd_count"),
//		            "last_24_months", dpdCounts.get("last_24_months_dpd_count"),
//		            "last_36_months", dpdCounts.get("last_36_months_dpd_count")
//		        ));
//		        
//		        // Max DPD Days Summary
//		        summary.put("max_dpd_days_summary", Map.of(
//		            "last_3_months", maxDpd.get("max_dpd_3_months"),
//		            "last_6_months", maxDpd.get("max_dpd_6_months"),
//		            "last_12_months", maxDpd.get("max_dpd_12_months"),
//		            "last_24_months", maxDpd.get("max_dpd_24_months"),
//		            "last_36_months", maxDpd.get("max_dpd_36_months")
//		        ));
//		        
//		        // Points Breakdown
//		        summary.put("points_breakdown", Map.of(
//		            "dpd_3m_max_days_points", dpdPoints.get("dpd_3_months_max_days_points"),
//		            "dpd_6m_max_days_points", dpdPoints.get("dpd_6_months_max_days_points"),
//		            "dpd_12m_max_days_points", dpdPoints.get("dpd_12_months_max_days_points"),
//		            "dpd_24m_max_days_points", dpdPoints.get("dpd_24_months_max_days_points"),
//		            "dpd_36m_max_days_points", dpdPoints.get("dpd_36_months_max_days_points"),
//		            "dpd_3m_count_points", dpdPoints.get("dpd_3_months_count_points"),
//		            "dpd_6m_count_points", dpdPoints.get("dpd_6_months_count_points"),
//		            "dpd_12m_count_points", dpdPoints.get("dpd_12_months_count_points"),
//		            "dpd_36m_count_points", dpdPoints.get("dpd_36_months_count_points"),
//		            "total_dpd_score", dpdPoints.get("total_dpd_score")
//		        ));
//		        
//		        // Risk categorization based on total DPD score
//		        String riskCategory = "LOW";
//		        int totalScore = dpdPoints.get("total_dpd_score");
//		        
//		        if (totalScore >= 140) { // Near maximum points (160 max possible)
//		            riskCategory = "VERY_LOW";
//		        } else if (totalScore >= 100) {
//		            riskCategory = "LOW";
//		        } else if (totalScore >= 60) {
//		            riskCategory = "MEDIUM";
//		        } else if (totalScore >= 30) {
//		            riskCategory = "HIGH";
//		        } else {
//		            riskCategory = "VERY_HIGH";
//		        }
//		        
//		        summary.put("risk_category", riskCategory);
//		        summary.put("total_possible_points", 160); // Maximum possible points
//		        summary.put("percentage_score", Math.round((totalScore * 100.0) / 160));
//		        
//		        // Eligibility flag based on your scoring logic
//		        boolean isEligible = totalScore >= 60; // Minimum threshold
//		        summary.put("is_eligible", isEligible);
//		        
//		        return summary;
//		    }
//
//		    // Helper method to format date
//		    private String formatDate(Integer year, Integer month) {
//		        return String.format("%04d-%02d", year, month);
//		    }		    
//		    
////====================================== Active account counts ================================================================================		    
//		    public Boolean accountStatusChecker(Object statusValue) {
//		  		if (statusValue instanceof Integer) {
//		  				Set<Integer> allowedStatus_integer = new HashSet<>(Arrays.asList(0,00,11,21,22,23,24,25,71,78,80,82,83,84,130,131));
//		  				if (allowedStatus_integer.contains(statusValue)) {
//		                  return true;
//		  			   }
//		  		}else {
//		  				Set<String> allowedStatus_string = new HashSet<>(Arrays.asList("","00","DEFAULTVALUE"));
//		  			if (allowedStatus_string.contains(statusValue)) {
//		                 return true;
//		  			}
//		  				
//		  	      }
//		  			return false;
//		  	}
//		    
//		    private int calculateActiveAccountPoints(int count) {
//		        if (count >= 1 && count <= 4) {
//		            return 20;
//		        } else if (count >= 5 && count <= 8) {
//		            return 10;
//		        } else if (count > 8) {
//		            return 0;
//		        } else {
//		            return 0;
//		        }
//		    }
//		    
////============================================ written off and suitfilled =========================================================================
//		    public int calculateSuitFiledWrittenOffPoints(JSONObject json) {
//		        try {
//		            JSONObject inProfileResponse = json.optJSONObject("INProfileResponse");
//		            if (inProfileResponse == null) return 0;
//
//		            JSONObject caisAccount = inProfileResponse.optJSONObject("CAIS_Account");
//		            if (caisAccount == null) return 0;
//
//		            Object details = caisAccount.opt("CAIS_Account_DETAILS");
//		            if (details == null) return 0;
//
//		            JSONArray accountArray = (details instanceof JSONArray)
//		                    ? (JSONArray) details
//		                    : new JSONArray().put(details);
//
//		            if (accountArray.length() == 0) return 0;
//
//		            Set<Integer> allowedStatuses = new HashSet<>(Arrays.asList(0, 99, 11, 12));
//
//		            for (int i = 0; i < accountArray.length(); i++) {
//		                JSONObject account = accountArray.getJSONObject(i);
//
//		                int status = account.optInt("Written_off_Settled_Status", 0);
//		                String suitFiled = account.optString("SuitFiled_WilfulDefault", "");
//
//		                if (!allowedStatuses.contains(status)) {
//		                    return -50;
//		                }
//
//		                if (!"00".equalsIgnoreCase(suitFiled) && !suitFiled.isEmpty()) {
//		                    return -50;
//		                }
//		            }
//
//		            return 50; // All pass
//
//		        } catch (Exception e) {
//		            e.printStackTrace();
//		            return -50;
//		        }
//		    }
//		    
//		    private int getPointsFor30Days(int totalCAPSLast30Days) {
//		        if (totalCAPSLast30Days == 0) {
//		            return 50;
//		        } else if (totalCAPSLast30Days >= 1 && totalCAPSLast30Days <= 5) {
//		            return 25;
//		        } else if (totalCAPSLast30Days >= 6 && totalCAPSLast30Days <= 8) {
//		            return 0;
//		        } else {
//		            return -10;
//		        }
//		    }
//
//		    private int getPointsFor90Days(int totalCAPSLast90Days) {
//		        if (totalCAPSLast90Days == 0) {
//		            return 50;
//		        } else if (totalCAPSLast90Days >= 1 && totalCAPSLast90Days <= 8) {
//		            return 25;
//		        } else if (totalCAPSLast90Days >= 9 && totalCAPSLast90Days <= 10) {
//		            return 0;
//		        } else {
//		            return -10;
//		        }
//
//
//		    }
	        
	        public Map<?,?> verifyOtp(JSONObject input) {
		        try {
		            // Prepare external API call
		            HttpClient httpClient = HttpClients.createDefault();
		           HttpPost httpPost = new HttpPost("https://loan.credithaat.com/d2cforinternal/otpvalidate");
		            
//		           HttpPost httpPost = new HttpPost("http://localhost/d2cforinternal/otpvalidate");

		            httpPost.setHeader("token", "Y3JlZGl0aGFhdHRlc3RzZXJ2ZXI="); // Replace with actual token
		            httpPost.setHeader("Content-Type", "application/json");

		            
		            StringEntity entity = new StringEntity(input.toString());
		            httpPost.setEntity(entity);

		            HttpResponse httpResponse = httpClient.execute(httpPost);
		            String jsonResponse = EntityUtils.toString(httpResponse.getEntity());

		            //System.out.println("Raw JSON Response from Experian API: " + jsonResponse);
		            
		            if (jsonResponse == null || jsonResponse.trim().isEmpty() || jsonResponse.trim().equalsIgnoreCase("null")) {
		                Map<String, Object> data = new HashMap<>();
		                data.put("score", null);
		                data.put("active_account_count", 0);
		                data.put("code", 2);
		                data.put("message", "otp verified successfully but experian not found");
		                long finalScore = 20;
		                data.put("finalScore", finalScore);

		                String mobileNumber = input.optString("Mobilenumber", null);
		                Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);

		                if (optionalUser.isPresent()) {
		                    UserInfo user = optionalUser.get();
		                    user.setCreditProfile("1000");
		                    userInfoRepository.save(user);
		                }

		                return data; // ⬅️ return early if response is null/empty
		            }
		            
		            JSONObject responseJson = new JSONObject(jsonResponse);
		            if ("OTP validation failed, OTP is not match".equalsIgnoreCase(responseJson.optString("errorString"))) {
		                Map<String, Object> data = new HashMap<>();
		                data.put("code", -1);  
		                data.put("message", responseJson.getString("errorString"));
		                return data;
		            }

		            // Handle null or empty response
		            if (jsonResponse == null || jsonResponse.trim().isEmpty() || jsonResponse.trim().equalsIgnoreCase("null")) {
		            	Map<String, Object> data = new HashMap<>();
		                data.put("score", null);
		                data.put("active_account_count", 0);
		                data.put("code", 2);
		                data.put("message", "otp verified succesfully but experian not found");
		                long finalScore = 20;
		                data.put("finalScore", finalScore);

			            
		                String mobileNumber = input.optString("Mobilenumber", null);
		                Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);

			            if (optionalUser.isPresent()) {
			                UserInfo user = optionalUser.get();
//			                user.setFinalScore((int)finalScore);  // ✅ Make sure this setter exists in your UserInfo entity
			                userInfoRepository.save(user);
			            }
		                return data;


		            }

		            if (!jsonResponse.trim().startsWith("{")) {
		                throw new Exception("Invalid response format. Not a JSON object: " + jsonResponse);
		            }

		            // Parse JSON response
		            JSONObject json = new JSONObject(jsonResponse);

		         // Step 1: Check if errorString is missing or null → means OTP is invalid
//		         String errorString = json.optString("errorString", null);
//		         if (errorString == null || errorString.trim().isEmpty()) {
//		             Map<String, Object> data = new HashMap<>();
//		             data.put("code", -1);
//		             data.put("message", "Invalid OTP");
//		             return data;
//		         }
		            JSONObject inProfileResponse = json.optJSONObject("INProfileResponse");
		            //JSONObject errorString = json.optJSONObject("errorString");

		            if (inProfileResponse == null) { 
		            	Map<String, Object> data = new HashMap<>();
		                data.put("score", null);
		                data.put("active_account_count", 0);
			            //data.put("finalScore", finalScore);
		                data.put("code", 2);//code 2 is for otp verified succesfully but experian not found
		                data.put("message", "otp verified succesfully but experian not found");
		                long finalScore = 20;
		                data.put("finalScore", finalScore);
		                String mobileNumber = input.optString("Mobilenumber", null);
		                Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);

			            if (optionalUser.isPresent()) {
			                UserInfo user = optionalUser.get();
//			                user.setFinalScore((int)finalScore);  // ✅ Make sure this setter exists in your UserInfo entity
			                user.setCreditProfile("1000");
			                userInfoRepository.save(user);
			            }
		                return data;

		            }
		            
 

		            // Extract score
		            JSONObject scoreJson = inProfileResponse.optJSONObject("SCORE");
		            String pan = inProfileResponse
		                    .optJSONObject("Current_Application")
		                    .optJSONObject("Current_Application_Details")
		                    .optJSONObject("Current_Applicant_Details")
		                    .optString("IncomeTaxPan", "");

		                String dob = inProfileResponse
		                    .optJSONObject("Current_Application")
		                    .optJSONObject("Current_Application_Details")
		                    .optJSONObject("Current_Applicant_Details")
		                    .optString("Date_Of_Birth_Applicant", "");

		                String email = inProfileResponse
		                    .optJSONObject("Current_Application")
		                    .optJSONObject("Current_Application_Details")
		                    .optJSONObject("Current_Applicant_Details")
		                    .optString("EMailId", "");

		                String gender = inProfileResponse
		                    .optJSONObject("Current_Application")
		                    .optJSONObject("Current_Application_Details")
		                    .optJSONObject("Current_Applicant_Details")
		                    .optString("Gender_Code", "");

		                String pincode = inProfileResponse
		                    .optJSONObject("Current_Application")
		                    .optJSONObject("Current_Application_Details")
		                    .optJSONObject("Current_Applicant_Address_Details")
		                    .optString("PINCode", "");
		            String score = scoreJson != null ? scoreJson.optString("BureauScore", null) : null;
		            
//================================================this code is used to save  bereauresponse in table====== 

		            String mobileNumber = input.optString("Mobilenumber", null);
		            Optional<UserInfo> userInfoOpt = userInfoRepository.findByMobileNumber(mobileNumber);

		            if (userInfoOpt.isPresent() && inProfileResponse != null) {
		                UserInfo user = userInfoOpt.get();

		                // Wrap the JSON in {"INProfileResponse": {...}}
		                JSONObject wrappedResponse = new JSONObject();
		                wrappedResponse.put("INProfileResponse", inProfileResponse);

		                userBureauDataService.saveOrUpdateBureauData(
		                    user,
		                    score,
		                    wrappedResponse.toString()
		                );
		                
//		                if (score != null && !score.trim().isEmpty()) {
//		                    user.setCreditProfile(score);
//		                    userInfoRepository.save(user); 
//		                }
		                if (score != null && !score.trim().isEmpty()) {
		                    user.setCreditProfile(score);
		                } else {
		                    // fallback to 1000 if score is missing
		                    user.setCreditProfile("1000");
		                }

		                // ✅ Save additional fields if present
		                if (email != null && !email.trim().isEmpty()) {
		                    user.setEmail(email);
		                }
		                if (dob != null && !dob.trim().isEmpty()) {
		                    try {
		                        // Parse "19700101" into LocalDate
		                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		                        LocalDate parsedDob = LocalDate.parse(dob, inputFormatter);

		                        // If your UserInfo entity dob is String → save formatted String
		                        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		                        user.setDob(parsedDob.format(outputFormatter));

		                        // If your UserInfo entity dob is LocalDate → just set parsedDob
		                        // user.setDob(parsedDob);
		                    } catch (Exception e) {
		                        System.out.println("Invalid DOB format: " + dob);
		                    }
		                }

		                // ✅ Gender (convert String → Integer)
		                if (gender != null && !gender.trim().isEmpty()) {
		                    try {
		                        user.setGender(Integer.parseInt(gender));
		                    } catch (NumberFormatException e) {
		                        System.out.println("Invalid gender value: " + gender);
		                    }
		                }

		                // ✅ Pincode (convert String → Integer)
		                if (pincode != null && !pincode.trim().isEmpty()) {
		                    try {
		                        user.setResidentialPincode(Integer.parseInt(pincode));
		                    } catch (NumberFormatException e) {
		                        System.out.println("Invalid pincode value: " + pincode);
		                    }
		                }

		                // ✅ Persist updates
		                userInfoRepository.save(user);
		            }
		            


//=======================================================================================================

		            // Extract account info
		            JSONObject caisAccountResponse = inProfileResponse.optJSONObject("CAIS_Account");
		            if (caisAccountResponse == null) throw new Exception("Missing CAIS_Account");

		            Object caisAccountDetailsObject = caisAccountResponse.opt("CAIS_Account_DETAILS");

		            JSONArray accountDetails = caisAccountDetailsObject instanceof JSONArray
		                    ? (JSONArray) caisAccountDetailsObject
		                    : new JSONArray().put(caisAccountDetailsObject);
		            
		            
		            
//==================================== Repayment Score Logic Starts ==================================================================
		            ObjectMapper mapper = new ObjectMapper();
		            List<Map<String, Object>> caisAccounts = mapper.readValue(
		                accountDetails.toString(), new TypeReference<List<Map<String, Object>>>() {}
		            );

		            int totalMonths = 0;
		            int delayedMonths = 0;

		            for (Map<String, Object> account : caisAccounts) {
		                @SuppressWarnings("unchecked")
		                List<Map<String, Object>> history = (List<Map<String, Object>>) account.get("CAIS_Account_History");
		                if (history == null) continue;

		                for (Map<String, Object> month : history) {
		                    Object dpdObj = month.get("Days_Past_Due");
		                    if (dpdObj != null && !dpdObj.toString().trim().isEmpty()) {
		                        try {
		                            int dpd = Integer.parseInt(dpdObj.toString().trim());
		                            totalMonths++;
		                            if (dpd > 0) delayedMonths++;
		                        } catch (NumberFormatException e) {
		                            // Skip invalid values like "?"
		                        }
		                    }
		                }
		            }

		            String repaymentScoreStr = "NA";
		            if (totalMonths > 0) {
		                int repaymentScore = (int) Math.round(((double)(totalMonths - delayedMonths) / totalMonths) * 100);
		                repaymentScoreStr = String.valueOf(repaymentScore);
		            }
		            
		            
		            
		            Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);

		            if (optionalUser.isPresent()) {
		                UserInfo user = optionalUser.get();

		                // ✅ Set repayment score
//		                user.setRepaymentScore(repaymentScoreStr);

		                // ✅ Save updated user
		                userInfoRepository.save(user);
		            }

		            // ✅ Repayment Score Logic Ends

		            
//================================ Get report date for DPD calculations ================================================================================
		            
		            
		            Integer reportDate = inProfileResponse.optJSONObject("Header").optInt("ReportDate");
		            LocalDate buro_report_fetch_date = LocalDate.parse(String.format("%08d", reportDate), DateTimeFormatter.BASIC_ISO_DATE);
		            LocalDate buro_report_minus_one_month = buro_report_fetch_date.minusMonths(1);


		            
		            int maxVintageInMonths = calculateMaxVintageInMonths(accountDetails);
		            double totalAmountPastDue = 0;
		            

		            int suitPoints = calculateSuitFiledWrittenOffPoints(json);
		           

		            // Initialize DPD counters for different time periods
		            Map<String, Integer> dpdCounts = new HashMap<>();
		            dpdCounts.put("last_3_months_dpd_count", 0);
		            dpdCounts.put("last_6_months_dpd_count", 0);
		            dpdCounts.put("last_12_months_dpd_count", 0);
		            dpdCounts.put("last_24_months_dpd_count", 0);
		            dpdCounts.put("last_36_months_dpd_count", 0);

		            // Initialize max DPD tracking
		            Map<String, Integer> maxDpd = new HashMap<>();
		            maxDpd.put("max_dpd_3_months", 0);
		            maxDpd.put("max_dpd_6_months", 0);
		            maxDpd.put("max_dpd_12_months", 0);
		            maxDpd.put("max_dpd_24_months", 0);
		            maxDpd.put("max_dpd_36_months", 0);

		            int totalUnsecuredLoans = 0;
		            int unsecuredLoansLast30 = 0;
		            int unsecuredLoansLast90 = 0;
		            int lowAmountLoans30 = 0;
		            int lowAmountLoans90 = 0;
		            int active_account_count = 0;
		            List<String> unsecuredTypes = Arrays.asList("61", "69", "71", "24", "5");
		            
			         // Replace the existing credit utilization logic in your verifyOtp method with this corrected version
			         // Initialize totals
			         double totalCCBalance = 0.0;
			         double totalCCLimit = 0.0;
			         double totalBalanceAll = 0.0;
			         double totalLimitAll = 0.0;
			         
			         double totalLoanOriginalAmount = 0.0;
			         double totalLoanCurrentBalance = 0.0;



		            for (int i = 0; i < accountDetails.length(); i++) {
		                JSONObject account = accountDetails.getJSONObject(i);
		                Object statusValue = account.opt("Account_Status");
		                if (accountStatusChecker(statusValue)) {
		                    active_account_count++;
		                }
		                
		                // ✅ Unsecured loan logic
		                String accountType = account.optString("Account_Type");
		                String dateClosed = account.optString("Date_Closed");
		                String openDateStr = account.optString("Date_Opened");
		                double origLoanAmt = account.optDouble("Highest_Credit_or_Original_Loan_Amount", 0);
		                double creditLimit = account.optDouble("Credit_Limit_Amount", 0.0);
		                double balance = account.optDouble("Current_Balance", 0.0);
		                // For Credit Cards (Account_Type = "10")
		                if ("10".equals(accountType) && (dateClosed == null || dateClosed.isEmpty())) {
		                    // Only count active/open credit cards
		                    totalCCBalance += balance;
		                    totalCCLimit += creditLimit;
		                    System.out.println("CC Account - Balance: " + balance + ", Limit: " + creditLimit);
		                }
		                // For all accounts with credit limits (for overall utilization)
		                if (creditLimit > 0 && (dateClosed == null || dateClosed.isEmpty())) {
		                    totalBalanceAll += balance;
		                    totalLimitAll += creditLimit;
		                }
		                
		             // ✅ Loan Utilization Score Logic (excluding Credit Cards)
		                if (!"10".equals(accountType) && (dateClosed == null || dateClosed.isEmpty())) {
		                	double loanOrigAmount = account.optDouble("Highest_Credit_or_Original_Loan_Amount", 0);
		                    double loanBalance = account.optDouble("Current_Balance", 0);
		                    totalLoanOriginalAmount += loanOrigAmount;
		                    totalLoanCurrentBalance += loanBalance;
		                }

		                
		                // Check if it's an unsecured loan that's still open
		                if (unsecuredTypes.contains(accountType) && (dateClosed == null || dateClosed.isEmpty())) {
		                    totalUnsecuredLoans++;

		                    if (openDateStr != null && !openDateStr.isEmpty()) {
		                        try {
		                            LocalDate openDate = LocalDate.parse(openDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
		                            long daysBetween = ChronoUnit.DAYS.between(openDate, buro_report_fetch_date);

		                            if (daysBetween <= 30) {
		                                unsecuredLoansLast30++;
		                                if (origLoanAmt < 10000) {
		                                    lowAmountLoans30++;
		                                }
		                            }
		                            if (daysBetween <= 90) {
		                                unsecuredLoansLast90++;
		                                if (origLoanAmt < 10000) {
		                                    lowAmountLoans90++;
		                                }
		                            }
		                        } catch (Exception e) {
		                            System.err.println("Error parsing date: " + openDateStr + " - " + e.getMessage());
		                        }
		                    }
		                }
			             // For Credit Cards (Account_Type = "10")
			             if ("10".equals(accountType) && (dateClosed == null || dateClosed.isEmpty())) {
			                 // Only count active/open credit cards
			                 totalCCBalance += balance;
			                 totalCCLimit += creditLimit;
			                 System.out.println("CC Account - Balance: " + balance + ", Limit: " + creditLimit);
			             }

			             // For all accounts with credit limits (for overall utilization)
			             if (creditLimit > 0 && (dateClosed == null || dateClosed.isEmpty())) {
			                 totalBalanceAll += balance;
			                 totalLimitAll += creditLimit;
			             }
		             
		                double amountPastDue = 0;
		                try {
		                    amountPastDue = account.optDouble("Amount_Past_Due", 0);
		                } catch (Exception e) {
		                    // ignore
		                }
		                totalAmountPastDue += amountPastDue;
		                
		                // Process DPD history for this account
		                Object historyValue = account.opt("CAIS_Account_History");
		                if (historyValue != null) {
		                    Map<String, Integer> accountDpdData = processAccountDPDHistory(historyValue, buro_report_minus_one_month);
		                    
		                    // Add account DPD data to overall counts
		                    dpdCounts.put("last_3_months_dpd_count", 
		                        dpdCounts.get("last_3_months_dpd_count") + accountDpdData.get("dpd_3_months"));
		                    dpdCounts.put("last_6_months_dpd_count", 
		                        dpdCounts.get("last_6_months_dpd_count") + accountDpdData.get("dpd_6_months"));
		                    dpdCounts.put("last_12_months_dpd_count", 
		                        dpdCounts.get("last_12_months_dpd_count") + accountDpdData.get("dpd_12_months"));
		                    dpdCounts.put("last_24_months_dpd_count", 
		                        dpdCounts.get("last_24_months_dpd_count") + accountDpdData.get("dpd_24_months"));
		                    dpdCounts.put("last_36_months_dpd_count", 
		                        dpdCounts.get("last_36_months_dpd_count") + accountDpdData.get("dpd_36_months"));

		                    // Update max DPD values
		                    maxDpd.put("max_dpd_3_months", 
		                        Math.max(maxDpd.get("max_dpd_3_months"), accountDpdData.get("max_dpd_3_months")));
		                    maxDpd.put("max_dpd_6_months", 
		                        Math.max(maxDpd.get("max_dpd_6_months"), accountDpdData.get("max_dpd_6_months")));
		                    maxDpd.put("max_dpd_12_months", 
		                        Math.max(maxDpd.get("max_dpd_12_months"), accountDpdData.get("max_dpd_12_months")));
		                    maxDpd.put("max_dpd_24_months", 
		                        Math.max(maxDpd.get("max_dpd_24_months"), accountDpdData.get("max_dpd_24_months")));
		                    maxDpd.put("max_dpd_36_months", 
		                        Math.max(maxDpd.get("max_dpd_36_months"), accountDpdData.get("max_dpd_36_months")));
		                }
		                
		                

		                // You can log or use unsecuredLoanCount here

		            
		        }
		            // Scoring logic (individual category scores)
		            int unsecuredScoreOverall = 0;
		            int unsecuredScoreLast30 = 0;
		            int lowAmt30Score = 0;
		            int lowAmt90Score = 0;

		            if (totalUnsecuredLoans == 0) unsecuredScoreOverall = 20;
		            else if (totalUnsecuredLoans <= 3) unsecuredScoreOverall = 15;

		            if (unsecuredLoansLast30 == 0) unsecuredScoreLast30 = 20;
		            else if (unsecuredLoansLast30 == 1) unsecuredScoreLast30 = 10;

		            if (lowAmountLoans30 == 0) lowAmt30Score = 20;
		            else if (lowAmountLoans30 <= 2) lowAmt30Score = 10;
		            else lowAmt30Score = 0;

		            if (lowAmountLoans90 == 0) lowAmt90Score = 20;
		            else if (lowAmountLoans90 <= 3) lowAmt90Score = 5;
		            else lowAmt90Score = 0;


		         //======================Calculate CC Utilization Score==============================
		         int ccUtilScore = 0;
		         double ccUtilPercent = 0.0;
		         if (totalCCLimit > 0) {
		             ccUtilPercent = (totalCCBalance / totalCCLimit) * 100;

		             if (ccUtilPercent < 50) {
		                 ccUtilScore = 50;
		             } else if (ccUtilPercent >= 50 && ccUtilPercent <= 75) {
		                 ccUtilScore = 20;
		             } else if (ccUtilPercent > 75 && ccUtilPercent <= 90) {
		                 ccUtilScore = 10;
		             } else if (ccUtilPercent > 90) {
		                 ccUtilScore = 0;
		             }
		         } else {
		             // No credit cards or no credit limit data
		             ccUtilScore = 0; // or you might want to set this to a default value
		             ccUtilPercent = 0.0;
		         }
	
		         
//=============================Calculate Overall Credit Utilization Score=====================
		         int creditUtilScore = 0;
		         double overallUtilPercent = 0.0;
		         if (totalLimitAll > 0) {
		             overallUtilPercent = (totalBalanceAll / totalLimitAll) * 100;

		             if (overallUtilPercent < 30) {
		                 creditUtilScore = 5;
		             } else if (overallUtilPercent >= 30 && overallUtilPercent <= 50) {
		                 creditUtilScore = 3;
		             } else if (overallUtilPercent > 50) {
		                 creditUtilScore = 1;
		             }
		         } else {
		             // No accounts with credit limits
		             creditUtilScore = 0;
		             overallUtilPercent = 0.0;
		         }
//==========================================================================================================		         
		      
// ===================================== Calculate Loan Utilization Ratio and Score=========================
		         int loanUtilScore = 0;
		         double loanUtilRatio = 0.0;

		         if (totalLoanOriginalAmount > 0) {
		        	 loanUtilRatio = (totalLoanCurrentBalance / totalLoanOriginalAmount) * 100;

		             if (loanUtilRatio < 30) {  //---------------0.3 = 30%
		                 loanUtilScore = 5;
		             } else if (loanUtilRatio <= 50) { //---------------0.5 = 50%
		                 loanUtilScore = 3;
		             } else {
		                 loanUtilScore = 1;
		             }
		         }

//=========================================================================================================
		            
		         int activeAccountPoints = calculateActiveAccountPoints(active_account_count);

		            // Calculate DPD-based points
		            Map<String, Integer> dpdPoints = calculateDPDPoints(dpdCounts, maxDpd);
		            //Map<String, Object> unsecuredMetrics = new UserInfoService().calculateMetrics(accountDetails, reportDateString);

		            int overduePoints = calculateOverduePoints(totalAmountPastDue);
		            int scorePoints = calculateScorePoints(score);
		            int vintagePoints = calculateVintagePoints(maxVintageInMonths);		    
		            
		        //    JSONObject inProfileResponse = input.optJSONObject("INProfileResponse");
		            JSONObject totalCAPSSummary = inProfileResponse != null ? inProfileResponse.optJSONObject("TotalCAPS_Summary") : null;

		            int totalCAPSLast30Days = totalCAPSSummary != null ? totalCAPSSummary.optInt("TotalCAPSLast30Days", 0) : 0;
		            int totalCAPSLast90Days = totalCAPSSummary != null ? totalCAPSSummary.optInt("TotalCAPSLast90Days", 0) : 0;

		            int points30 = getPointsFor30Days(totalCAPSLast30Days);
		            int points90 = getPointsFor90Days(totalCAPSLast90Days);



	
		            int dpd3MaxDaysPoints = dpdPoints.get("dpd_3_months_max_days_points");
		            int dpd6MaxDaysPoints = dpdPoints.get("dpd_6_months_max_days_points");
		            int dpd12MaxDaysPoints = dpdPoints.get("dpd_12_months_max_days_points");
		            int dpd24MaxDaysPoints = dpdPoints.get("dpd_24_months_max_days_points");
		            int dpd36MaxDaysPoints = dpdPoints.get("dpd_36_months_max_days_points");
		            int dpd3CountPoints = dpdPoints.get("dpd_3_months_count_points");
		            int dpd6CountPoints = dpdPoints.get("dpd_6_months_count_points");
		            int dpd12CountPoints = dpdPoints.get("dpd_12_months_count_points");
		            int dpd36CountPoints = dpdPoints.get("dpd_36_months_count_points");
		            


		            // Prepare response
		            Map<String, Object> data = new HashMap<>();
		            data.put("score", score);
		            data.put("pan", pan);
		            data.put("dob", dob);
		            data.put("email", email);
		            data.put("gender", gender);
		            data.put("pincode", pincode);
		            data.put("score_points", scorePoints);
		            data.put("max_vintage_in_months", maxVintageInMonths);
		            data.put("vintage_points", vintagePoints);
		            data.put("active_account_count", active_account_count);
		            data.put("active_account_points", activeAccountPoints);
		            data.put("total_amount_past_due", totalAmountPastDue);
		            data.put("overdue_points", overduePoints);	
		            data.put("suitfiled_writtenoff_points", suitPoints);

		            data.put("Points_CAPS_30_Days", points30);
		            data.put("Points_CAPS_90_Days", points90);

		            
		            // Add individual DPD points in your requested format
		            data.put("dpd_3_max_days_points", dpd3MaxDaysPoints);
		            data.put("dpd_6_max_days_points", dpd6MaxDaysPoints);
		            data.put("dpd_12_max_days_points", dpd12MaxDaysPoints);
		            data.put("dpd_24_max_days_points", dpd24MaxDaysPoints);
		            data.put("dpd_36_max_days_points", dpd36MaxDaysPoints);
		            data.put("dpd_3_count_points", dpd3CountPoints);
		            data.put("dpd_6_count_points", dpd6CountPoints);
		            data.put("dpd_12_count_points", dpd12CountPoints);
		            data.put("dpd_36_count_points", dpd36CountPoints);
		            // Add DPD counts
		            data.putAll(dpdCounts);
		            
		            // Add max DPD values
		            data.putAll(maxDpd);
		            
		            // Add individual DPD points as requested
		            data.put("dpd_3m_points", dpdPoints.get("dpd_3_months_max_days_points"));
		            data.put("dpd_6m_points", dpdPoints.get("dpd_6_months_max_days_points"));
		            data.put("dpd_12m_points", dpdPoints.get("dpd_12_months_max_days_points"));
		            data.put("dpd_24m_points", dpdPoints.get("dpd_24_months_max_days_points"));
		            data.put("dpd_36m_points", dpdPoints.get("dpd_36_months_max_days_points"));
		            data.put("dpd_count_3m_points", dpdPoints.get("dpd_3_months_count_points"));
		            data.put("dpd_count_6m_points", dpdPoints.get("dpd_6_months_count_points"));
		            data.put("dpd_count_12m_points", dpdPoints.get("dpd_12_months_count_points"));
		            data.put("dpd_count_36m_points", dpdPoints.get("dpd_36_months_count_points"));
		            data.put("suitfiled_writtenoff_points", suitPoints);	
		            
		            // Add total DPD score
		            data.put("total_dpd_score", dpdPoints.get("total_dpd_score"));
		            
		            // Add overall DPD summary
		            data.put("dpd_summary", createDPDSummary(dpdCounts, maxDpd, dpdPoints));
		            
		   		 // ✅ Store values in your response map
		            data.put("UnsecuredLoanScore", unsecuredScoreOverall);
		            data.put("TotalUnsecuredLoans", totalUnsecuredLoans);
		            data.put("UnsecuredLoansLast30Days", unsecuredScoreLast30);
		            data.put("UnsecuredLoansLast90Days", unsecuredLoansLast90);//just counted not to use 
		            data.put("LowAmountLoans30Days", lowAmt30Score);
		            data.put("LowAmountLoans90Days", lowAmt90Score);
	
		            data.put("loanUtilRatio", String.valueOf(loanUtilRatio));
		            data.put("loanUtilScore", loanUtilScore);

		            data.put("CC_Utilization_Score", String.valueOf(ccUtilScore));//all  creditcard utilization ratio
		            data.put("Credit_Utilization_Score", creditUtilScore);
		            data.put("Overall_Credit_Utilization_Percent",overallUtilPercent);
		            data.put("CC_Utilization_Percent",ccUtilPercent); // Round to 2 decimal places

		            //data.put("CC_Utilization_Percent", Math.round(ccUtilPercent * 100.0) / 100.0); // Round to 2 decimal places
		           // data.put("Overall_Credit_Utilization_Percent", Math.round(overallUtilPercent * 100.0) / 100.0);
		            data.put("Total_CC_Balance", totalCCBalance);
		            data.put("Total_CC_Limit", totalCCLimit);
		            data.put("Total_Overall_Balance", totalBalanceAll);
		            data.put("Total_Overall_Limit", totalLimitAll);
		            
		            //data.put("Loan_Utilization_Score", loanUtilScore);//total approved amount and current balance ratio for all active  loans 
		            //data.put("Loan_Utilization_Ratio", Math.round(loanUtilRatio * 100.0) / 100.0); // in %, rounded to 2 decimals	            
		            data.put("Total_Loan_Original_Amount", totalLoanOriginalAmount);
		            data.put("Total_Loan_Current_Balance", totalLoanCurrentBalance);
		            data.put("repaymentScore", repaymentScoreStr);
		            double lcScore;
		           // lcScore = (loanUtilRatio + ccUtilPercent)/2;
		            
		           lcScore = ((totalCCBalance+totalLoanCurrentBalance)/(totalCCLimit+totalLoanOriginalAmount))*100;
		        // ✅ Round to 1 decimal places (truncate, not round up)
		           lcScore = Math.round(lcScore * 10.0) / 10.0;
		           data.put("Loan_creditCard_utilization_percentage",lcScore);
		            long finalScore;
		            finalScore = scorePoints+vintagePoints+activeAccountPoints+overduePoints+suitPoints+dpd3MaxDaysPoints+dpd6MaxDaysPoints+dpd12MaxDaysPoints+dpd24MaxDaysPoints+dpd36MaxDaysPoints+dpd3CountPoints+dpd6CountPoints+dpd12CountPoints+dpd36CountPoints
		            +points90+points30+unsecuredScoreOverall+unsecuredScoreLast30+lowAmt30Score+lowAmt90Score+ccUtilScore+loanUtilScore;
		            data.put("finalScore", finalScore);
		            if (optionalUser.isPresent()) {
		                UserInfo user = optionalUser.get();
//		                user.setFinalScore((int)finalScore);  // ✅ Make sure this setter exists in your UserInfo entity
		                userInfoRepository.save(user);
		            }
		            
		            //  data.put("unsecured_loan_purpose_code", unsecuredLoanPurpose);  // <==stored unsecuredloan count
		            data.put("code", 1);//code 1 is for otp verified succesfully
		            data.put("message", "otp verified succesfully");
		            
		            return data;

		        }
		        
		        catch (Exception e) {
		            e.printStackTrace();
		            Map<String, Object> data = new HashMap<>();
		            data.put("code", -1);//code -1 stands for OTP failed
		            data.put("message", "OTP verification failed: " + e.getMessage());

		            return data;

		        }
		    }


	//============================================= verify otp end ===================================================================
	
		    
	
		    
		    
	//============================================ vintage calculate ==================================================================
		    
		    private int calculateMaxVintageInMonths(JSONArray accountDetails) {
		        int maxVintageInMonths = 0;

		        String[] dateFormats = new String[]{
		                "yyyyMMdd", "yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy",
		                "MM/dd/yyyy", "yyyy/MM/dd", "dd MMM yyyy", "MMM dd, yyyy"
		        };

		        for (int i = 0; i < accountDetails.length(); i++) {
		            JSONObject account = null;
					try {
						account = accountDetails.getJSONObject(i);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            String openDateStr = account.optString("Open_Date", "").trim();

		            if (!openDateStr.isEmpty()) {
		                Date openDate = null;

		                for (String format : dateFormats) {
		                    try {
		                        SimpleDateFormat sdf = new SimpleDateFormat(format);
		                        sdf.setLenient(false);
		                        openDate = sdf.parse(openDateStr);
		                        break;
		                    } catch (Exception ignored) {
		                    }
		                }

		                if (openDate == null) {
		                    System.out.println("Could not parse Open_Date: " + openDateStr);
		                    continue;
		                }

		                Calendar openCal = Calendar.getInstance();
		                openCal.setTime(openDate);

		                Calendar now = Calendar.getInstance();
		                int monthsDiff = (now.get(Calendar.YEAR) - openCal.get(Calendar.YEAR)) * 12 +
		                        (now.get(Calendar.MONTH) - openCal.get(Calendar.MONTH));

		                if (monthsDiff > maxVintageInMonths) {
		                    maxVintageInMonths = monthsDiff;
		                }
		            }
		        }

		        return maxVintageInMonths;
		        
		    }
// ================================================= vintage points =============================================================
		    private int calculateVintagePoints(int months) {
		        if (months > 24) return 20;
		        if (months >= 12) return 10;
		        if (months >= 6) return 5;
		        return 0;
		    }
//===============================================================================================================================
//====================================== score points ===========================================================================
		    private int calculateScorePoints(String score) {
		        if (score == null || score.trim().isEmpty()) {
		            return 20; // NTC
		        }

		        try {
		            int scoreValue = Integer.parseInt(score);
		            if (scoreValue > 780) return 100;
		            if (scoreValue > 720) return 80;
		            if (scoreValue >= 700) return 60;
		            if (scoreValue >= 670) return 50;
		            if (scoreValue >= 650) return 20;
		            return 10;
		        } catch (Exception e) {
		            return 20; // fallback
		        }
		    }
		    
//==========================================================================================================================
		    
		    
		    
// =============================================== overdues amount ==========================================================
		    
		    private int calculateOverduePoints(double totalOverdueAmount) {
		        if (totalOverdueAmount == 0 ) {
		            return 50;
		        } else if (totalOverdueAmount > 0 && totalOverdueAmount <= 2000) {
		            return 20;
		        } else if (totalOverdueAmount > 2000 && totalOverdueAmount <= 5000) {
		            return 0;
		        } else if (totalOverdueAmount > 5000) {
		            return -50;
		        }
		        return 0; // default fallback
		    }
//=========================================================================================


		    private Map<String, Object> createErrorResponse(String message, int code) {
		        Map<String, Object> data = new HashMap<>();
		        data.put("score", null);
		        data.put("active_account_count", 0);
		        data.put("code", code);
		        data.put("message", message);
		        return data;
		    }
		
		    
//================================================= dpd =================================================================================
		 // Process DPD history for a single account
		    public Map<String, Integer> processAccountDPDHistory(Object historyValue, LocalDate buro_report_minus_one_month) {
		        Map<String, Integer> result = new HashMap<>();
		        
		        // Initialize counters
		        result.put("dpd_3_months", 0);
		        result.put("dpd_6_months", 0);
		        result.put("dpd_12_months", 0);
		        result.put("dpd_24_months", 0);
		        result.put("dpd_36_months", 0);
		        result.put("max_dpd_3_months", 0);
		        result.put("max_dpd_6_months", 0);
		        result.put("max_dpd_12_months", 0);
		        result.put("max_dpd_24_months", 0);
		        result.put("max_dpd_36_months", 0);
		        
		        try {
		            // Create a unified array to process
		            JSONArray historyArray = historyValue instanceof JSONArray 
		                ? (JSONArray) historyValue 
		                : new JSONArray().put((JSONObject) historyValue);
		            
		            for (int j = 0; j < historyArray.length(); j++) {
		                JSONObject caisAccountHistory = historyArray.optJSONObject(j);
		                
		                Integer daysPastDue = caisAccountHistory.optInt("Days_Past_Due");
		                Integer month = caisAccountHistory.optInt("Month");
		                Integer year = caisAccountHistory.optInt("Year");
		                
		                String formattedDate = formatDate(year, month);
		                LocalDate startDate = LocalDate.parse(formattedDate + "-01");
		                
		                // Calculate months between dates
		                long total_month = ChronoUnit.MONTHS.between(startDate, buro_report_minus_one_month);
		                
		                // Count DPD occurrences and track max DPD for different time periods
		                if (daysPastDue > 0) {
		                    if (total_month <= 3) {
		                        result.put("dpd_3_months", result.get("dpd_3_months") + 1);
		                        result.put("max_dpd_3_months", Math.max(result.get("max_dpd_3_months"), daysPastDue));
		                    }
		                    if (total_month <= 6) {
		                        result.put("dpd_6_months", result.get("dpd_6_months") + 1);
		                        result.put("max_dpd_6_months", Math.max(result.get("max_dpd_6_months"), daysPastDue));
		                    }
		                    if (total_month <= 12) {
		                        result.put("dpd_12_months", result.get("dpd_12_months") + 1);
		                        result.put("max_dpd_12_months", Math.max(result.get("max_dpd_12_months"), daysPastDue));
		                    }
		                    if (total_month <= 24) {
		                        result.put("dpd_24_months", result.get("dpd_24_months") + 1);
		                        result.put("max_dpd_24_months", Math.max(result.get("max_dpd_24_months"), daysPastDue));
		                    }
		                    if (total_month <= 36) {
		                        result.put("dpd_36_months", result.get("dpd_36_months") + 1);
		                        result.put("max_dpd_36_months", Math.max(result.get("max_dpd_36_months"), daysPastDue));
		                    }
		                }
		            }
		        } catch (Exception e) {
		            System.err.println("Error processing account history: " + e.getMessage());
		        }
		        
		        return result;
		    }
		    

		    // Calculate points based on DPD analysis as per your exact requirements
		    public Map<String, Integer> calculateDPDPoints(Map<String, Integer> dpdCounts, Map<String, Integer> maxDpd) {
		        Map<String, Integer> points = new HashMap<>();
		        
		        // DPD Max Days Points - Last 3 Months
		        int dpd3MaxDaysPoints = calculateDPDMaxDaysPoints_3M(maxDpd.get("max_dpd_3_months"));
		        points.put("dpd_3_months_max_days_points", dpd3MaxDaysPoints);
		        
		        // DPD Max Days Points - Last 6 Months
		        int dpd6MaxDaysPoints = calculateDPDMaxDaysPoints_6M(maxDpd.get("max_dpd_6_months"));
		        points.put("dpd_6_months_max_days_points", dpd6MaxDaysPoints);
		        
		        // DPD Max Days Points - Last 12 Months
		        int dpd12MaxDaysPoints = calculateDPDMaxDaysPoints_12M(maxDpd.get("max_dpd_12_months"));
		        points.put("dpd_12_months_max_days_points", dpd12MaxDaysPoints);
		        
		        // DPD Max Days Points - Last 24 Months
		        int dpd24MaxDaysPoints = calculateDPDMaxDaysPoints_24M(maxDpd.get("max_dpd_24_months"));
		        points.put("dpd_24_months_max_days_points", dpd24MaxDaysPoints);
		        
		        // DPD Max Days Points - Last 36 Months
		        int dpd36MaxDaysPoints = calculateDPDMaxDaysPoints_36M(maxDpd.get("max_dpd_36_months"));
		        points.put("dpd_36_months_max_days_points", dpd36MaxDaysPoints);
		        
		        // DPD Count Points - Last 3 Months
		        int dpd3CountPoints = calculateDPDCountPoints_3M(dpdCounts.get("last_3_months_dpd_count"));
		        points.put("dpd_3_months_count_points", dpd3CountPoints);
		        
		        // DPD Count Points - Last 6 Months
		        int dpd6CountPoints = calculateDPDCountPoints_6M(dpdCounts.get("last_6_months_dpd_count"));
		        points.put("dpd_6_months_count_points", dpd6CountPoints);
		        
		        // DPD Count Points - Last 12 Months
		        int dpd12CountPoints = calculateDPDCountPoints_12M(dpdCounts.get("last_12_months_dpd_count"));
		        points.put("dpd_12_months_count_points", dpd12CountPoints);
		        
		        // DPD Count Points - Last 36 Months
		        int dpd36CountPoints = calculateDPDCountPoints_36M(dpdCounts.get("last_36_months_dpd_count"));
		        points.put("dpd_36_months_count_points", dpd36CountPoints);
		        
		        // Calculate total DPD score
		        int totalDpdScore = dpd3MaxDaysPoints + dpd6MaxDaysPoints + dpd12MaxDaysPoints + 
		                           dpd24MaxDaysPoints + dpd36MaxDaysPoints + dpd3CountPoints + 
		                           dpd6CountPoints + dpd12CountPoints + dpd36CountPoints;
		        
		        points.put("total_dpd_score", totalDpdScore);
		        
		        return points;
		    }

		    // DPD Max Days Points - Last 3 Months
		    private int calculateDPDMaxDaysPoints_3M(int maxDpdDays) {
		        if (maxDpdDays == 0) return 20;
		        if (maxDpdDays >= 1 && maxDpdDays <= 5) return 10;
		        if (maxDpdDays >= 6 && maxDpdDays <= 10) return 5;
		        if (maxDpdDays > 10) return 0;
		        return 0;
		    }

		    // DPD Max Days Points - Last 6 Months
		    private int calculateDPDMaxDaysPoints_6M(int maxDpdDays) {
		        if (maxDpdDays == 0) return 20;
		        if (maxDpdDays >= 1 && maxDpdDays <= 5) return 10;
		        if (maxDpdDays >= 6 && maxDpdDays <= 10) return 5;
		        if (maxDpdDays > 10) return 0;
		        return 0;
		    }

		    // DPD Max Days Points - Last 12 Months
		    private int calculateDPDMaxDaysPoints_12M(int maxDpdDays) {
		        if (maxDpdDays == 0) return 20;
		        if (maxDpdDays >= 1 && maxDpdDays <= 15) return 10;
		        if (maxDpdDays >= 16 && maxDpdDays <= 30) return 5;
		        if (maxDpdDays > 30) return 0;
		        return 0;
		    }

		    // DPD Max Days Points - Last 24 Months
		    private int calculateDPDMaxDaysPoints_24M(int maxDpdDays) {
		        if (maxDpdDays == 0) return 20;
		        if (maxDpdDays >= 1 && maxDpdDays <= 30) return 10;
		        if (maxDpdDays >= 31 && maxDpdDays <= 60) return 5;
		        if (maxDpdDays > 60) return 0;
		        return 0;
		    }

		    // DPD Max Days Points - Last 36 Months
		    private int calculateDPDMaxDaysPoints_36M(int maxDpdDays) {
		        if (maxDpdDays == 0) return 20;
		        if (maxDpdDays >= 1 && maxDpdDays <= 30) return 15;
		        if (maxDpdDays >= 31 && maxDpdDays <= 60) return 10;
		        if (maxDpdDays >= 61 && maxDpdDays <= 90) return 5;
		        if (maxDpdDays > 90) return 0;
		        return 0;
		    }

		    // DPD Count Points - Last 3 Months
		    private int calculateDPDCountPoints_3M(int dpdCount) {
		        if (dpdCount == 0) return 10;
		        if (dpdCount == 1) return 5;
		        if (dpdCount > 1) return 0;
		        return 0;
		    }

		    // DPD Count Points - Last 6 Months
		    private int calculateDPDCountPoints_6M(int dpdCount) {
		        if (dpdCount == 0) return 10;
		        if (dpdCount >= 1 && dpdCount <= 2) return 5;
		        if (dpdCount > 2) return 0;
		        return 0;
		    }

		    // DPD Count Points - Last 12 Months
		    private int calculateDPDCountPoints_12M(int dpdCount) {
		        if (dpdCount == 0) return 10;
		        if (dpdCount >= 1 && dpdCount <= 3) return 5;
		        if (dpdCount > 3) return 0;
		        return 0;
		    }

		    // DPD Count Points - Last 36 Months
		    private int calculateDPDCountPoints_36M(int dpdCount) {
		        if (dpdCount == 0) return 10;
		        if (dpdCount >= 1 && dpdCount <= 3) return 5;
		        if (dpdCount > 3) return 0;
		        return 0;
		    }

		    // Create DPD summary object with detailed breakdown
		    private Map<String, Object> createDPDSummary(Map<String, Integer> dpdCounts, Map<String, Integer> maxDpd, Map<String, Integer> dpdPoints) {
		        Map<String, Object> summary = new HashMap<>();
		        
		        // DPD Counts Summary
		        summary.put("dpd_counts_summary", Map.of(
		            "last_3_months", dpdCounts.get("last_3_months_dpd_count"),
		            "last_6_months", dpdCounts.get("last_6_months_dpd_count"),
		            "last_12_months", dpdCounts.get("last_12_months_dpd_count"),
		            "last_24_months", dpdCounts.get("last_24_months_dpd_count"),
		            "last_36_months", dpdCounts.get("last_36_months_dpd_count")
		        ));
		        
		        // Max DPD Days Summary
		        summary.put("max_dpd_days_summary", Map.of(
		            "last_3_months", maxDpd.get("max_dpd_3_months"),
		            "last_6_months", maxDpd.get("max_dpd_6_months"),
		            "last_12_months", maxDpd.get("max_dpd_12_months"),
		            "last_24_months", maxDpd.get("max_dpd_24_months"),
		            "last_36_months", maxDpd.get("max_dpd_36_months")
		        ));
		        
		        // Points Breakdown
		        summary.put("points_breakdown", Map.of(
		            "dpd_3m_max_days_points", dpdPoints.get("dpd_3_months_max_days_points"),
		            "dpd_6m_max_days_points", dpdPoints.get("dpd_6_months_max_days_points"),
		            "dpd_12m_max_days_points", dpdPoints.get("dpd_12_months_max_days_points"),
		            "dpd_24m_max_days_points", dpdPoints.get("dpd_24_months_max_days_points"),
		            "dpd_36m_max_days_points", dpdPoints.get("dpd_36_months_max_days_points"),
		            "dpd_3m_count_points", dpdPoints.get("dpd_3_months_count_points"),
		            "dpd_6m_count_points", dpdPoints.get("dpd_6_months_count_points"),
		            "dpd_12m_count_points", dpdPoints.get("dpd_12_months_count_points"),
		            "dpd_36m_count_points", dpdPoints.get("dpd_36_months_count_points"),
		            "total_dpd_score", dpdPoints.get("total_dpd_score")
		        ));
		        
		        // Risk categorization based on total DPD score
		        String riskCategory = "LOW";
		        int totalScore = dpdPoints.get("total_dpd_score");
		        
		        if (totalScore >= 140) { // Near maximum points (160 max possible)
		            riskCategory = "VERY_LOW";
		        } else if (totalScore >= 100) {
		            riskCategory = "LOW";
		        } else if (totalScore >= 60) {
		            riskCategory = "MEDIUM";
		        } else if (totalScore >= 30) {
		            riskCategory = "HIGH";
		        } else {
		            riskCategory = "VERY_HIGH";
		        }
		        
		        summary.put("risk_category", riskCategory);
		        summary.put("total_possible_points", 160); // Maximum possible points
		        summary.put("percentage_score", Math.round((totalScore * 100.0) / 160));
		        
		        // Eligibility flag based on your scoring logic
		        boolean isEligible = totalScore >= 60; // Minimum threshold
		        summary.put("is_eligible", isEligible);
		        
		        return summary;
		    }

		    // Helper method to format date
		    private String formatDate(Integer year, Integer month) {
		        return String.format("%04d-%02d", year, month);
		    }		    
		    
//====================================== Active account counts ================================================================================		    
		    public Boolean accountStatusChecker(Object statusValue) {
		  		if (statusValue instanceof Integer) {
		  				Set<Integer> allowedStatus_integer = new HashSet<>(Arrays.asList(0,00,11,21,22,23,24,25,71,78,80,82,83,84,130,131));
		  				if (allowedStatus_integer.contains(statusValue)) {
		                  return true;
		  			   }
		  		}else {
		  				Set<String> allowedStatus_string = new HashSet<>(Arrays.asList("","00","DEFAULTVALUE"));
		  			if (allowedStatus_string.contains(statusValue)) {
		                 return true;
		  			}
		  				
		  	      }
		  			return false;
		  	}
		    
		    private int calculateActiveAccountPoints(int count) {
		        if (count >= 1 && count <= 4) {
		            return 20;
		        } else if (count >= 5 && count <= 8) {
		            return 10;
		        } else if (count > 8) {
		            return 0;
		        } else {
		            return 0;
		        }
		    }
		    
//============================================ written off and suitfilled =========================================================================
		    public int calculateSuitFiledWrittenOffPoints(JSONObject json) {
		        try {
		            JSONObject inProfileResponse = json.optJSONObject("INProfileResponse");
		            if (inProfileResponse == null) return 0;

		            JSONObject caisAccount = inProfileResponse.optJSONObject("CAIS_Account");
		            if (caisAccount == null) return 0;

		            Object details = caisAccount.opt("CAIS_Account_DETAILS");
		            if (details == null) return 0;

		            JSONArray accountArray = (details instanceof JSONArray)
		                    ? (JSONArray) details
		                    : new JSONArray().put(details);

		            if (accountArray.length() == 0) return 0;

		            Set<Integer> allowedStatuses = new HashSet<>(Arrays.asList(0, 99, 11, 12));

		            for (int i = 0; i < accountArray.length(); i++) {
		                JSONObject account = accountArray.getJSONObject(i);

		                int status = account.optInt("Written_off_Settled_Status", 0);
		                String suitFiled = account.optString("SuitFiled_WilfulDefault", "");

		                if (!allowedStatuses.contains(status)) {
		                    return -50;
		                }

		                if (!"00".equalsIgnoreCase(suitFiled) && !suitFiled.isEmpty()) {
		                    return -50;
		                }
		            }

		            return 50; // All pass

		        } catch (Exception e) {
		            e.printStackTrace();
		            return -50;
		        }
		    }
		    
		    private int getPointsFor30Days(int totalCAPSLast30Days) {
		        if (totalCAPSLast30Days == 0) {
		            return 50;
		        } else if (totalCAPSLast30Days >= 1 && totalCAPSLast30Days <= 5) {
		            return 25;
		        } else if (totalCAPSLast30Days >= 6 && totalCAPSLast30Days <= 8) {
		            return 0;
		        } else {
		            return -10;
		        }
		    }

		    private int getPointsFor90Days(int totalCAPSLast90Days) {
		        if (totalCAPSLast90Days == 0) {
		            return 50;
		        } else if (totalCAPSLast90Days >= 1 && totalCAPSLast90Days <= 8) {
		            return 25;
		        } else if (totalCAPSLast90Days >= 9 && totalCAPSLast90Days <= 10) {
		            return 0;
		        } else {
		            return -10;
		        }


		    }
//===================================================================================
		    

//==========================================================================		    
}

		    	

