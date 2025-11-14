package com.lsp.web.ONDCService;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HDBYubi {

    public static String client_loan_id;

    public static Map<String, Object> create_application_with_details(
    		String firstName,          // 1
    	    String lastName,           // 2
    	    String middlename,         // 3
    	    String mobileNumber,       // 4
    	    String dob,                // 5
    	    Integer gender,            // 6
    	    String address,            // 7
    	    String currentCity,        // 8
    	    String currentState,       // 9
    	    Integer pincode,           // 10
    	    String companyName,        // 11
    	    Float income,              // 12
    	    Integer maritalStatus,     // 13
    	    String phone,              // 14
    	    String pan,                // 15
    	    Float loanAmount,          // 16
    	    String email     
    ) {
    	Map<String, Object> result = new HashMap<>();
        try {
//            String url = "https://colend-uat-01-api.go-yubi.in/colending/clients/vibhuprada/api/v2/loans";
            
            String url = "https://uapi.go-yubi.com/colending/clients/vibhuprada/api/v2/loans";

            // === Generate IDs ===
//            String uniqueId = UUID.randomUUID().toString();
            String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 30);


            // === Gender logic ===
//            String genderCode = "o";
//            String title = "Mr";

         // === Gender Mapping ===
          String genderCode = switch (gender) {
              case 1 -> "m";
              case 2 -> "f";
              case 3 -> "o";
              default -> "o";
          };
//
//          // === Title Logic ===
          String title = switch (gender) {
              case 2 -> (maritalStatus != null && maritalStatus == 1) ? "Mrs" : "Miss";
              case 1 -> "Mr";
              default -> "Mr";
          };
//
//          // === Marital Status Mapping ===
          String maritalStatusStr = switch (maritalStatus) {
              case 1 -> "married";
              case 2 -> "unmarried";
              case 3 -> "divorced";
              case 4 -> "widowed";
              default -> "unknown";
          };

            // === DOB format and age calculation ===
            String dobFormatted = "";
            int age = 0;

            try {
                String[] possibleFormats = {"yyyy-MM-dd", "yyyy/MM/dd", "dd/MM/yyyy", "dd-MM-yyyy"};
                Date parsedDate = null;
                String matchedFormat = null;

                for (String format : possibleFormats) {
                    try {
                        parsedDate = new SimpleDateFormat(format).parse(dob);
                        matchedFormat = format;
                        break;
                    } catch (Exception ignored) {
                    }
                }

                if (parsedDate != null) {
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dobFormatted = outputFormat.format(parsedDate);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(matchedFormat);
                    LocalDate birthDate = LocalDate.parse(dob, formatter);
                    age = Period.between(birthDate, LocalDate.now()).getYears();
                } else {
                    throw new IllegalArgumentException("Invalid date format: " + dob);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // === Build payload ===
            Map<String, Object> data = new HashMap<>();
            data.put("customer_category", "salaried");
            data.put("client_customer_id", uniqueId);
            data.put("product_id", "VIBHUP_CHDB_PL_DLP");
            data.put("application_id", uniqueId);
            data.put("client_loan_id", uniqueId);
            data.put("title", title);
            data.put("first_name", firstName);
            data.put("middle_name", middlename);
            data.put("last_name", lastName);
            data.put("date_of_birth", dobFormatted);
            data.put("gender", genderCode);
            data.put("employment_details_name", companyName);
            data.put("primary_borrower_type", "individual");
            data.put("current_address", address);
            data.put("current_city", currentCity);
            data.put("current_state", currentState);
            data.put("mobile_number", phone);
            data.put("email", email);
            data.put("current_pincode", pincode);
            data.put("father_name", middlename);
            data.put("net_monthly_income", income);
            data.put("marital_status", maritalStatusStr);
            data.put("pan_number", pan);
            data.put("category", "unsecured");
            data.put("sub_category", "fresh");
            data.put("principal_amount", loanAmount);
            data.put("disbursement_type", "single");
            data.put("interest_rate", 25.8);
            data.put("tenure", 18);
            data.put("tenure_frequency", "monthly");
            data.put("repayment_frequency", "monthly");
            data.put("number_of_repayments", 18);
            data.put("age", String.valueOf(age));

            String payload = new JSONObject(data).toString();

            // === Build headers ===
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("Api-Key", "395a7cd0-19e6-41b2-9d45-61f7a2749d11");
            headers.set("Api-Key", "df52d6c1-0415-485b-97ad-e24a318d6e2e");
            headers.set("Product-Id", "VIBHUP_CHDB_PL_DLP");

            HttpEntity<String> entity = new HttpEntity<>(payload, headers);

            // === Call API ===
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            String jsonStr = response.getBody();
            JSONObject json = new JSONObject(jsonStr);

            // Save for logging
            result.put("url", "");
            result.put("requestPayload", new JSONObject(data));
            result.put("responsePayload", json);
            result.put("client_loan_id", json.optString("client_loan_id"));

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    
    
    public static JSONObject getApplicationStatus(String clientLoanId) {
        try {
            // Make sure client_loan_id is already set
            if (clientLoanId == null || clientLoanId.isEmpty()) {
                throw new IllegalStateException("client_loan_id is not set. Call create_application_with_details first.");
            }

            // Construct the status URL
//            String url = "https://colend-uat-01-api.go-yubi.in/colending/clients/vibhuprada/api/v2/loans/" 
//                          + clientLoanId + "/get_status";
            
            String url = "https://uapi.go-yubi.com/colending/clients/vibhuprada/api/v2/loans/" + clientLoanId + "/get_status";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
//            headers.add("api-key", "395a7cd0-19e6-41b2-9d45-61f7a2749d11");
            headers.add("api-key", "df52d6c1-0415-485b-97ad-e24a318d6e2e");
            headers.add("Product-Id", "VIBHUP_CHDB_PL_DLP");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // NOTE: This should be POST, not GET. But since you said not to change anything else, I'll keep GET.
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,  // <-- should be POST in reality
                    entity,
                    String.class
            );

            JSONObject result = new JSONObject();
            result.put("response", new JSONObject(response.getBody()));

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    
    public static Map<String, Object> initiateAA(String client_loan_id) {
        try {
//            String url = "https://uapi-uat.go-yubi.in/investor_integration/actions/invoke_api?type=initiate_aa";

            String url = "https://uapi.go-yubi.com/investor_integration/actions/invoke_api?type=initiate_aa";
            // Build payload
            Map<String, Object> body = new HashMap<>();
            Map<String, Object> result = new HashMap<>();
            body.put("client_loan_id", client_loan_id);
//            body.put("agreement_id", 2420);
            body.put("agreement_id", 400);
            body.put("redirect_url", "https://www.arysefin.com/yubi/YubiSteps?client_loan_id=" + client_loan_id);
//            body.put("redirect_url", "https://fe.getrysa.com/yubi/YubiSteps?client_loan_id=" + client_loan_id);
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
//            headers.add("api-key", "de707678e98ec2d3637b0c889153353a4a");
            headers.add("api-key", "5f022e6d-b3b7-46c5-9193-aac9f383c3b2");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Combine headers + body
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();

            // Call POST
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject(response.getBody());

            // Extract optional fields
            int requestId = json.optInt("request_Id");
            String redirect_Url = json.optString("redirection_url");

            System.out.println("Request ID: " + requestId);
            System.out.println("Redirect URL: " + redirect_Url);

            result.put("url", redirect_Url);
            result.put("requestPayload", new JSONObject(body));
            result.put("responsePayload", new JSONObject(response.getBody()));

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    

    public static JSONObject retrieveReport(String client_loan_id, int requestId) {
        try {
//            String url = "https://uapi-uat.go-yubi.in/investor_integration/actions/invoke_api?type=retrieve_report";

            String url = "https://uapi.go-yubi.com/investor_integration/actions/invoke_api?type=retrieve_report";
            // Construct payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("client_loan_id", client_loan_id);
//            payload.put("agreement_id", 2420);
            payload.put("agreement_id", 400);
            payload.put("request_id", requestId);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
//            headers.add("api-key", "de707678e98ec2d3637b0c889153353a4a");
            headers.add("api-key", "5f022e6d-b3b7-46c5-9193-aac9f383c3b2");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Wrap payload + headers
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            RestTemplate restTemplate = new RestTemplate();

            // POST request
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject(response.getBody());

            System.out.println("Retrieve Report Response: " + json.toString(2));
            json.put("requestPayload", new JSONObject(payload));
            json.put("responsePayload", new JSONObject(response.getBody()));

            return json;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    
    public static JSONObject initiateKYC(String client_loan_id) {
        try {
//            String url = "https://integ-gateway-sit.go-yubi.in/investor_integration/actions/invoke_api?type=kyc";

            String url = "https://uapi.go-yubi.com/investor_integration/actions/invoke_api?type=kyc";
            // Build JSON payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("client_loan_id", client_loan_id);
//            payload.put("agreement_id", 2420);
            payload.put("agreement_id", 400);
            payload.put("redirection_url", "https://www.arysefin.com/yubi/Selfiepage?client_loan_id=" + client_loan_id);
//            payload.put("redirection_url", "https://fe.getrysa.com/yubi/Selfiepage?client_loan_id=" + client_loan_id);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
//            headers.add("api-key", "de707678e98ec2d3637b0c889153353a4a");
            headers.add("api-key", "5f022e6d-b3b7-46c5-9193-aac9f383c3b2");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Wrap payload + headers
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            RestTemplate restTemplate = new RestTemplate();

            // POST request
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject(response.getBody());

            // Optional: log KYC redirection or response details
            System.out.println("KYC API Response: " + json.toString(2));
            json.put("requestPayload", new JSONObject(payload));
            json.put("responsePayload", new JSONObject(response.getBody()));
            return json;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static JSONObject updateKYC(String client_loan_id, String selfieImageUrl) {
        try {
            // Prepare payload
            Map<String, Object> data = new HashMap<>();
            data.put("client_loan_id", client_loan_id);
//            data.put("agreement_id", 2420);
            data.put("agreement_id", 400);
            data.put("selfie_image", selfieImageUrl); // Pass as String URL

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
//            headers.add("api-key", "de707678e98ec2d3637b0c889153353a4a");
            headers.add("api-key", "5f022e6d-b3b7-46c5-9193-aac9f383c3b2");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Wrap payload + headers
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);

            RestTemplate restTemplate = new RestTemplate();

//            String url = "https://integ-gateway-sit.go-yubi.in/investor_integration/actions/invoke_api?type=update_kyc";

            String url = "https://uapi.go-yubi.com/investor_integration/actions/invoke_api?type=update_kyc";
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject(response.getBody());
            
            System.out.println("KYC API Response: " + json.toString(2));
           
           json.put("requestPayload", new JSONObject(data));
           json.put("responsePayload", new JSONObject(response.getBody()));
           return json;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
     
    
    
    
    public static JSONObject callDisbursementAPI(
            String client_loan_id,
            int amount,
            String bankName,
            String accountName,
            String ifscCode,
            String bankBranchName,
            String accountNo
    ) {
        try {
            // Prepare the disbursement_accounts array
            Map<String, Object> account = new HashMap<>();
            account.put("amount", amount);
            account.put("bank_name", bankName);
            account.put("account_name", accountName);
            account.put("ifsc_code", ifscCode);
            account.put("bank_branch_name", bankBranchName);
            account.put("account_no", accountNo);
            account.put("account_type", "borrower");

            List<Map<String, Object>> accountList = new ArrayList<>();
            accountList.add(account);

            Map<String, Object> disbursementData = new HashMap<>();
            disbursementData.put("disbursement_accounts", accountList);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Product-Id", "VIBHUP_CHDB_PL_DLP");
            headers.add("product_id", "VIBHUP_CHDB_PL_DLP");
//            headers.add("api-key", "395a7cd0-19e6-41b2-9d45-61f7a2749d11");
            headers.add("api-key", "df52d6c1-0415-485b-97ad-e24a318d6e2e");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(disbursementData, headers);

            RestTemplate restTemplate = new RestTemplate();

//            String url = "https://colend-uat-01-api.go-yubi.in/colending/clients/vibhuprada/api/v2/loans/" + client_loan_id;

            String url = "https://uapi.go-yubi.com/colending/clients/vibhuprada/api/v2/loans/" + client_loan_id;
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            JSONObject result = new JSONObject(response.getBody());
            System.out.println("Disbursement API response: " + result);
            result.put("requestPayload", new JSONObject(disbursementData));
            result.put("responsePayload", new JSONObject(response.getBody()));
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public static JSONObject callUpdateLoanAPI(
            String client_loan_id,
            int principalAmount,
//            int numberOfRepayments,
            List<String> photoLinks,
            List<String> payslipLinks,
            String employmentAddress,
            String employmentCity,
            String employmentState,
            int employmentPincode,
            String mothersFirstName,
            int yearsOfExperience,
            String spouseName,
            List<Map<String, String>> references, // each map with name, phone, relationship, address
            int tenure,
            double interestRate
    ) {
        try {
            // Calculate EMI within the API method
            double monthlyInterestRate = (interestRate / 100) / 12;
            int tenureMonths = tenure;

            double emi = (principalAmount * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, tenureMonths))
                    / (Math.pow(1 + monthlyInterestRate, tenureMonths) - 1);
            int emiTruncated = (int) emi;

            String formattedInterestRate = (interestRate % 1 == 0)
                    ? String.valueOf((int) interestRate)
                    : String.valueOf(interestRate);

            // Build JSON payload
            JSONObject payload = new JSONObject();
            payload.put("principal_amount", principalAmount);
            payload.put("interest_rate", formattedInterestRate);
            payload.put("tenure", tenure);
            payload.put("number_of_repayments", tenure);
            payload.put("loan_emi", emiTruncated);
            payload.put("payslip_link", new JSONArray(payslipLinks));
            payload.put("photo_link", new JSONArray(photoLinks));
            payload.put("employment_details_address", employmentAddress);
            payload.put("employment_city", employmentCity);
            payload.put("employment_state", employmentState);
            payload.put("employment_pincode", employmentPincode);
            payload.put("mothers_first_name", mothersFirstName);
            payload.put("employment_details_years_of_experience", yearsOfExperience);

            if (spouseName != null && !spouseName.isEmpty()) {
                payload.put("spouse_name", spouseName);
            }

            JSONArray referencesArray = new JSONArray();
            for (Map<String, String> ref : references) {
                JSONObject refObj = new JSONObject();
                refObj.put("name", ref.get("name"));
                refObj.put("phone", ref.get("phone"));
                refObj.put("relationship", ref.get("relationship"));
                refObj.put("address", ref.get("address"));
                referencesArray.put(refObj);
            }
            payload.put("references", referencesArray);

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Product-Id", "VIBHUP_CHDB_PL_DLP");
            headers.add("product_id", "VIBHUP_CHDB_PL_DLP");
//            headers.add("api-key", "395a7cd0-19e6-41b2-9d45-61f7a2749d11");
            headers.add("api-key", "df52d6c1-0415-485b-97ad-e24a318d6e2e");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);

            RestTemplate restTemplate = new RestTemplate();
//            String url = "https://uapi-uat.go-yubi.in/colending/clients/vibhuprada/api/v2/loans/" + client_loan_id;

            String url = "https://uapi.go-yubi.com/colending/clients/vibhuprada/api/v2/loans/" + client_loan_id;
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            JSONObject result = new JSONObject(response.getBody());
            System.out.println("Update Loan API response: " + result);
            result.put("requestPayload", new JSONObject(payload.toString()));
            result.put("responsePayload", new JSONObject(response.getBody()));
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    
    
    public static JSONObject generateKfsDocument(String client_loan_id) {
        try {
//            String url = "https://colend-uat-01-api.go-yubi.in/colending/clients/vibhuprada/api/v2/loans/"
//                    + client_loan_id + "/documents?document_types=kfs_doc&agreement_id=VIBHUP_CHDB_PL_DLP";

            String url = "https://uapi.go-yubi.com/colending/clients/vibhuprada/api/v2/loans/"
                    + client_loan_id + "/documents?document_types=kfs_doc&agreement_id=VIBHUP_CHDB_PL_DLP_1";

            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
//            headers.add("api-key", "395a7cd0-19e6-41b2-9d45-61f7a2749d11");
            headers.add("api-key", "df52d6c1-0415-485b-97ad-e24a318d6e2e");
            headers.add("product-id", "VIBHUP_CHDB_PL_DLP");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(null, headers); // No body

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JSONObject result = new JSONObject(response.getBody());
            System.out.println("Generate KFS Document API response: " + result);
            result.put("response", new JSONObject(response.getBody()));
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    
    public static JSONObject generateLoanAgreementDocument(String client_loan_id) {
        try {
//            String url = "https://colend-uat-01-api.go-yubi.in/colending/clients/vibhuprada/api/v2/loans/"
//                    + client_loan_id + "/documents?document_types=loan_agreement_doc&agreement_id=VIBHUP_CHDB_PL_DLP";

            String url = "https://uapi.go-yubi.com/colending/clients/vibhuprada/api/v2/loans/"
                    + client_loan_id + "/documents?document_types=loan_agreement_doc&agreement_id=VIBHUP_CHDB_PL_DLP_1";

            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
//            headers.add("api-key", "395a7cd0-19e6-41b2-9d45-61f7a2749d11");
            headers.add("api-key", "df52d6c1-0415-485b-97ad-e24a318d6e2e");
            headers.add("product-id", "VIBHUP_CHDB_PL_DLP");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(null, headers); // No body

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JSONObject result = new JSONObject(response.getBody());
            System.out.println("Loan Agreement Document API response: " + result);
            result.put("response", new JSONObject(response.getBody()));
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    
    public static JSONObject requestEsign(
            String client_loan_id,
            String email,
            String phone,
            String firstName,
            String lastName
    ) {
        String signerName = firstName + " " + lastName;

        try {
//            String url = "https://uapi-uat.go-yubi.in/investor_integration/actions/invoke_api?type=eSign";

            String url = "https://uapi.go-yubi.com/investor_integration/actions/invoke_api?type=eSign";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.add("api-key", "de707678e98ec2d3637b0c889153353a4a");
            headers.add("api-key", "5f022e6d-b3b7-46c5-9193-aac9f383c3b2");

            // JSON body
            JSONObject payload = new JSONObject();
            payload.put("client_loan_id", client_loan_id);
//            payload.put("agreement_id", 2420);
            payload.put("agreement_id", 400);
            payload.put("document", "www.drive.google.com");
            payload.put("document_type", "KFS/Loan_agreement");
//            payload.put("redirect_url", "https://fe.getrysa.com/yubi/AgreementDone?cid=" + client_loan_id);
            payload.put("redirect_url", "https://www.arysefin.com/yubi/AgreementDone?cid=" + client_loan_id);
            // Signer details
            JSONArray signers = new JSONArray();
            JSONObject signer = new JSONObject();
            signer.put("email", email);
            signer.put("phone_number", phone);
            signer.put("name", signerName);
            signer.put("primary_channel", "Email/App");
            signer.put("secondary_channel", "Email/App");
            signers.put(signer);

            payload.put("signers", signers);

            HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JSONObject result = new JSONObject(response.getBody());
            System.out.println("eSign Request API response: " + result);
            result.put("requestPayload", new JSONObject(payload.toString()));
            result.put("responsePayload", new JSONObject(response.getBody()));

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    
    
    
    public static JSONObject Register_Mandate(String client_loan_id, String accountNo, Float loanAmount) {
        try {
//            String url = "https://uapi-uat.go-yubi.in/investor_integration/actions/invoke_api?type=nach";

            String url = "https://uapi.go-yubi.com/investor_integration/actions/invoke_api?type=nach";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.add("api-key", "de707678e98ec2d3637b0c889153353a4a");
            headers.add("api-key", "5f022e6d-b3b7-46c5-9193-aac9f383c3b2");
            // Current date = firstCollectionDate
            LocalDate firstDate = LocalDate.now();
            // finalCollectionDate = two months after first date
            LocalDate finalDate = firstDate.plusMonths(358);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            Map<String, Object> data = new HashMap<>();
            data.put("client_loan_id", client_loan_id);
//            data.put("agreement_id", 2420);
            data.put("agreement_id", 400);
            data.put("maximumAmount", String.valueOf(loanAmount));
            data.put("collectionAmount", String.valueOf(loanAmount));
//            data.put("redirect_url", "https://fe.getrysa.com/yubi/MandateDone?cid=" + client_loan_id);
            data.put("redirect_url", "https://www.arysefin.com/yubi/MandateDone?cid=" + client_loan_id);
            data.put("firstCollectionDate", formatter.format(firstDate));
            data.put("finalCollectionDate", formatter.format(finalDate));
            data.put("account_no", accountNo);
            data.put("account_type", "savings");  // "savings" or "current"

            HttpEntity<String> entity = new HttpEntity<>(new JSONObject(data).toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JSONObject root = new JSONObject(response.getBody());

            // 🔍 Extract redirect URL
            String mandateUrl = root
                    .getJSONObject("eMandateNewCustomerAPIRequest")
                    .getJSONObject("messageBody")
                    .getJSONObject("responseDetails")
                    .getJSONObject("redirect")
                    .getString("url");

            JSONObject result = new JSONObject();
            result.put("mandate_url", mandateUrl);
            result.put("requestPayload", new JSONObject(data));
            result.put("responsePayload", new JSONObject(response.getBody()));
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static JSONObject GetMandateStatus(String clientLoanId) {
        JSONObject result = new JSONObject();
        try {
//            String url = "https://uapi-uat.go-yubi.in/investor_integration/actions/invoke_api?type=update_nach";
            String url = "https://uapi.go-yubi.com/investor_integration/actions/invoke_api?type=update_nach";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.add("api-key", "de707678e98ec2d3637b0c889153353a4a");
            headers.add("api-key", "5f022e6d-b3b7-46c5-9193-aac9f383c3b2");
            String txnId = "EN" + System.currentTimeMillis();

            Map<String, Object> data = new HashMap<>();
            data.put("client_loan_id", clientLoanId);
//            data.put("agreement_id", 2420);
            data.put("agreement_id", 400);
            data.put("txn_id", txnId);

            HttpEntity<String> entity = new HttpEntity<>(new JSONObject(data).toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            result.put("success", true);
            result.put("requestPayload", new JSONObject(data));
            result.put("responsePayload", new JSONObject(response.getBody()));
        } catch (Exception e) {
            e.printStackTrace();
//            result.put("success", false);
//            result.put("error", e.getMessage());
        }
        return result;
    }


    
    public static JSONObject sendAgreementSignedAPI(String clientLoanId) {
        try {
//            String url = "https://colend-uat-01-api.go-yubi.in/colending/clients/vibhuprada/api/v2/loans/" + clientLoanId + "/agreement_signed";

            String url = "https://uapi.go-yubi.com/colending/clients/vibhuprada/api/v2/loans/" + clientLoanId + "/agreement_signed";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.add("api-key", "395a7cd0-19e6-41b2-9d45-61f7a2749d11");
            headers.add("api-key", "df52d6c1-0415-485b-97ad-e24a318d6e2e");
            headers.add("Product-Id", "VIBHUP_CHDB_PL_DLP");

            // ✅ Dummy data
            Map<String, Object> payload = new HashMap<>();
            payload.put("entire_set_of_loan_agreements_link", Arrays.asList(
                    "https://cdn.pixabay.com/photo/2017/06/22/20/22/green-2432374_1280.jpg"
            ));
            payload.put("entire_set_of_loan_agreements_link_password", Arrays.asList(
                    "12345678", "12345678"
            ));
            payload.put("agreement_consent_date", "2025-04-29"); // or LocalDate.now().toString()

            HttpEntity<String> entity = new HttpEntity<>(new JSONObject(payload).toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            JSONObject result = new JSONObject();
            result.put("requestPayload", new JSONObject(payload));
            result.put("responsePayload", new JSONObject(response.getBody()));
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    

}
