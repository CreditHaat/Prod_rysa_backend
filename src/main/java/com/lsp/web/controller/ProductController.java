package com.lsp.web.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.lsp.web.ONDCService.ProductService;
import com.lsp.web.entity.CompanyMaster;
import com.lsp.web.entity.Product;
import com.lsp.web.entity.UserBureauData;
import com.lsp.web.entity.UserInfo;
import com.lsp.web.repository.CompanyMasterRepository;
import com.lsp.web.repository.ProductRepository;
import com.lsp.web.repository.UserBureauDataRepository;
import com.lsp.web.repository.UserInfoRepository;
import com.lsp.web.util.StringUtil;

@RestController
@CrossOrigin("*")
public class ProductController {
	
	@Autowired
	private ProductService productService;
//	@Autowired
//	private UserInfo userInfo;
	@Autowired
	private UserInfoRepository userinfoRepository;
	
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private UserBureauDataRepository userBureauDataRepository;
	@Autowired
	private CompanyMasterRepository companyMasterRepository;
	
	@GetMapping("/getSortedProducts")
    public ResponseEntity<?> getProductInfo(
            @RequestParam String mobile
    ) {
		
		try {
			
			UserInfo globalUserInfo;
			Optional<UserInfo> optionalUserInfo1 = userinfoRepository.findByMobileNumber(mobile);
			if(optionalUserInfo1.isEmpty()) {
				return null;
			}
			
			globalUserInfo = optionalUserInfo1.get();
			
			
			
			if(!globalUserInfo.getCreditProfile().equals("1000")) {
				
				ResponseEntity<Map<String, Object>> response = checkBREONDC(mobile);
				
				try {
					if (response != null && response.getBody() != null) {
					    Map<String, Object> body = response.getBody();

					    String creditCard = (String) body.get("creditCard");

					    // creditCardCount might come as Integer or Long
					    Number creditCardCountNumber = (Number) body.get("creditCardCount");
					    int creditCardCount = creditCardCountNumber != null ? creditCardCountNumber.intValue() : 0;

					    System.out.println("Credit Card: " + creditCard);
					    System.out.println("Credit Card Count: " + creditCardCount);
					    
					    UserBureauData userBureauData = null;
						Optional<UserBureauData> optionalUserBureauData = userBureauDataRepository.findByMobileNumber(mobile);
						if(optionalUserBureauData.isPresent()) {
							
							userBureauData = optionalUserBureauData.get();
							
							
							
							userBureauData.setActiveCreditCards(creditCardCount);
							userBureauData.setPrime(Integer.parseInt(creditCard));
							
							userBureauDataRepository.save(userBureauData);
							
						}
					}
				}catch(Exception e) {
					
				}
				
				if(response.getStatusCode().value() == 200) {
					
					Map<String, Object> body = response.getBody();
				    if (body != null) {
				        Boolean status = (Boolean) body.get("status");
				        String reason = (String) body.get("reason");

				        if (Boolean.FALSE.equals(status)) {
				            System.out.println("Error: " + reason);
				            
				             Optional<Product> chproduct = productRepository.findByProductName("CreditHaat");
				             if(chproduct.isPresent()) {
				            	 List<Product> finalList = new ArrayList();
					             finalList.add(chproduct.get());
					             return ResponseEntity.ok(finalList);
				             }
				             
				             return null;
				            
				        } else {
				        	
				        	System.out.println("Inside after passing masterBre");
				            System.out.println("Success: " + body);
				            
				            //Old code ////////////////////////////////////////////////////////////////////
				            UserInfo userInfo;
							Optional<UserInfo> optionalUserInfo = userinfoRepository.findByMobileNumber(mobile);
							if(optionalUserInfo.isEmpty()) {
								return null;
							}
							
							userInfo = optionalUserInfo.get();
							String profession = null;
							if (userInfo.getEmploymentType() == 1) {
//					        	ondcFormDataDTO.setEmploymentType("Salaried");
								profession = "salaried";
							} else if (userInfo.getEmploymentType() == 2) {
								profession = "Self Employment";
							} else if (userInfo.getEmploymentType() == 3) {
								profession = "Self Employment";// as ondc form has only two fields salaried and
																						// self employed otherwise here would be
																						// Business
							}
							

//							String paymentType = null;
							int paymentType = userInfo.getPaymentType();
							float income = userInfo.getMonthlyIncome();
							String pincode = String.valueOf(userInfo.getResidentialPincode());
							String company = userInfo.getCompanyName();
							
							List<Product> products = productRepository.findByStatus(0);
							
							List<Product> finalList = new ArrayList();
							
							for(Product product : products) {
								System.out.println("product we got is : "+product);
								String partner = product.getProductName();
								
								if(partner.equalsIgnoreCase("CreditHaat")) {
									finalList.add(product);
									continue;
								}
								
								Product sortedProduct = productService.productInfoList(mobile, partner, profession, paymentType, income, pincode);
								if(sortedProduct!=null) {
									
									System.out.println("product that we got after sorting is : "+sortedProduct);
									
									if(sortedProduct.getProductName().equalsIgnoreCase("bfl")) {
										ResponseEntity<Map<String, Object>> bajajResponse = checkSubscriberBajaj(mobile,(int) income, company);
										if(bajajResponse.getStatusCode().value() == 200) {
											
											Map<String, Object> responseBody = bajajResponse.getBody();
										    if (body != null) {
										        Boolean responseStatus = (Boolean) responseBody.get("status");
										        String responseReason = (String) responseBody.get("reason");
										        
										        if (Boolean.FALSE.equals(responseStatus)) {
										            System.out.println("Error: " + responseReason);
										        } else {
										        	
										        	System.out.println("Product before returning is : "+sortedProduct);
										        	finalList.add(sortedProduct);
										        }
										        
										    }
										}
										        
									}else {
										finalList.add(sortedProduct);
									}
									
									
								}
								
							}
							
//							if(intPaymentType == 0) {
//								paymentType = "cash";
//							}else if(intPaymentType == 1) {
//								paymentType = "cheque";
//							}else if(intPaymentType == 2) {
//								paymentType = "bank transfer";
//							}
//							
					        
					        return ResponseEntity.ok(finalList);
				            //////////////////////////////////////////////////////////////////////////////////
				            
				        }
				    }
					
				}
				
				
			}
			
			//use this if if we want the user to be entered without checking the bre----------------------------
			
//			if(!globalUserInfo.getCreditProfile().equals("1000")) {
//				//Old code ////////////////////////////////////////////////////////////////////
//	            UserInfo userInfo;
//				Optional<UserInfo> optionalUserInfo = userinfoRepository.findByMobileNumber(mobile);
//				if(optionalUserInfo.isEmpty()) {
//					return null;
//				}
//				
//				userInfo = optionalUserInfo.get();
//				String profession = null;
//				if (userInfo.getEmploymentType() == 1) {
////		        	ondcFormDataDTO.setEmploymentType("Salaried");
//					profession = "salaried";
//				} else if (userInfo.getEmploymentType() == 2) {
//					profession = "Self Employment";
//				} else if (userInfo.getEmploymentType() == 3) {
//					profession = "Self Employment";// as ondc form has only two fields salaried and
//																			// self employed otherwise here would be
//																			// Business
//				}
//				
//
////				String paymentType = null;
//				int paymentType = userInfo.getPaymentType();
//				float income = userInfo.getMonthlyIncome();
//				String pincode = String.valueOf(userInfo.getResidentialPincode());
//				String company = userInfo.getCompanyName();
//				
//				List<Product> products = productRepository.findByStatus(0);
//				
//				List<Product> finalList = new ArrayList();
//				
//				for(Product product : products) {
//					String partner = product.getProductName();
//					Product sortedProduct = productService.productInfoList(mobile, partner, profession, paymentType, income, pincode);
//					if(sortedProduct!=null) {
//						
//						if(sortedProduct.getProductName().equalsIgnoreCase("bfl")) {
//							ResponseEntity<Map<String, Object>> bajajResponse = checkSubscriberBajaj(mobile,(int) income, company);
//							if(bajajResponse.getStatusCode().value() == 200) {
//								
//								Map<String, Object> responseBody = bajajResponse.getBody();
//							    if (responseBody != null) {
//							        Boolean responseStatus = (Boolean) responseBody.get("status");
//							        String responseReason = (String) responseBody.get("reason");
//							        
//							        if (Boolean.FALSE.equals(responseStatus)) {
//							            System.out.println("Error: " + responseStatus);
//							        } else {
//							        	finalList.add(sortedProduct);
//							        }
//							        
//							    }
//							}
//							        
//						}else {
//							finalList.add(sortedProduct);
//						}
//						
//						
//					}
//					
//				}
//				
////				if(intPaymentType == 0) {
////					paymentType = "cash";
////				}else if(intPaymentType == 1) {
////					paymentType = "cheque";
////				}else if(intPaymentType == 2) {
////					paymentType = "bank transfer";
////				}
////				
//		        
//		        return ResponseEntity.ok(finalList);
//
//			}
			
			//-----------------------------------------------------------------------------------
			
			//here we will call without master bre
			else {
				
				    System.out.println("Inside the else of product controller");
				            
				            //Old code ////////////////////////////////////////////////////////////////////
				            UserInfo userInfo;
							Optional<UserInfo> optionalUserInfo = userinfoRepository.findByMobileNumber(mobile);
							if(optionalUserInfo.isEmpty()) {
								return null;
							}
							
							userInfo = optionalUserInfo.get();
							String profession = null;
							if (userInfo.getEmploymentType() == 1) {
//					        	ondcFormDataDTO.setEmploymentType("Salaried");
								profession = "salaried";
							} else if (userInfo.getEmploymentType() == 2) {
								profession = "Self Employment";
							} else if (userInfo.getEmploymentType() == 3) {
								profession = "Self Employment";// as ondc form has only two fields salaried and
																						// self employed otherwise here would be
																						// Business
							}
							

//							String paymentType = null;
							int paymentType = userInfo.getPaymentType();
							float income = userInfo.getMonthlyIncome();
							String pincode = String.valueOf(userInfo.getResidentialPincode());
							String company = userInfo.getCompanyName();
							
							List<Product> products = productRepository.findByStatus(0);
							
							List<Product> finalList = new ArrayList();
							
							for(Product product : products) {
								String partner = product.getProductName();
								Product sortedProduct = productService.productInfoList(mobile, partner, profession, paymentType, income, pincode);
								if(sortedProduct!=null) {
									
									if(sortedProduct.getProductName().equalsIgnoreCase("bfl")) {
										ResponseEntity<Map<String, Object>> bajajResponse = checkSubscriberBajaj(mobile,(int) income, company);
										if(bajajResponse.getStatusCode().value() == 200) {
											
											Map<String, Object> responseBody = bajajResponse.getBody();
										    if (responseBody != null) {
										        Boolean responseStatus = (Boolean) responseBody.get("status");
										        String responseReason = (String) responseBody.get("reason");
										        
										        if (Boolean.FALSE.equals(responseStatus)) {
										            System.out.println("Error: " + responseStatus);
										        } else {
										        	finalList.add(sortedProduct);
										        }
										        
										    }
										}
										        
									}else {
										finalList.add(sortedProduct);
									}
									
									
								}else if(product.getProductName().equalsIgnoreCase("CreditHaat")) {
									finalList.add(product);
								}
								
							}
							
//							if(intPaymentType == 0) {
//								paymentType = "cash";
//							}else if(intPaymentType == 1) {
//								paymentType = "cheque";
//							}else if(intPaymentType == 2) {
//								paymentType = "bank transfer";
//							}
//							
					        
					        return ResponseEntity.ok(finalList);
				            //////////////////////////////////////////////////////////////////////////////////
				            
				        
				    
					
				
			}
			
//			return null;
//			if(response.getStatusCode() == 200) {
//				
//			}
			
			Optional<Product> chproduct2 = productRepository.findByProductName("CreditHaat");
            if(chproduct2.isPresent()) {
           	 List<Product> finalList2 = new ArrayList();
	             finalList2.add(chproduct2.get());
	             return ResponseEntity.ok(finalList2);
            }
            
            return null;
			
			
		}catch(Exception e) {
			e.printStackTrace();
			Optional<Product> chproduct2 = productRepository.findByProductName("CreditHaat");
            if(chproduct2.isPresent()) {
           	 List<Product> finalList2 = new ArrayList();
	             finalList2.add(chproduct2.get());
	             return ResponseEntity.ok(finalList2);
            }
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		
		
    }
	
 	//******************************************* REST endpoint to test BRE ONDC logic
    private Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> map = new HashMap();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, json.opt(key));
        }
        return map;
    }

    @PostMapping("/check-bre-ondc")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<Map<String, Object>> checkBREONDC(@RequestParam String phone) {
        try {
            JSONObject response = processCreditReport_Master_BRE_ONDC(phone);
            Map<String, Object> map = jsonToMap(response);
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("status", false);
            err.put("reason", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @PostMapping("/check-bre-ondc-json")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<Map<String, Object>> checkBREONDCJson(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", false);
            err.put("reason", "Phone number is required");
            return ResponseEntity.badRequest().body(err);
        }
        JSONObject response = processCreditReport_Master_BRE_ONDC(phone);
        Map<String, Object> map = jsonToMap(response);
        return ResponseEntity.ok(map);
    }

//    // ***************************************** BRE logic for ONDC ************************************************
//    public JSONObject processCreditReport_Master_BRE_ONDC(String phone) {
//        Set<String> subscriberNames = new HashSet();
//        int activeCreditCards=0;
//        try {
//            UserBureauData experian = null;
//            try {
////                experian = (UserBureauData) dao.get(ExperianData.class, "applyPhone='" + phone + "' order by createTime desc");
//                Optional<UserBureauData> optionalExperian = userBureauDataRepository.findLatestByPhone(phone);
//                if(optionalExperian.isEmpty() || StringUtil.nullOrEmpty(optionalExperian.get().getResponseContent()) ) {
//                	return createResponse_with_ONDC(false, "No Experian data found for phone: " + phone, "", Collections.emptySet(), activeCreditCards);
//                }
//                
//                experian = optionalExperian.get();
//            } catch (Exception e) {
//                e.printStackTrace();
//                return createResponse_with_ONDC(false, "Database error: " + e.getMessage(), "", Collections.emptySet(), activeCreditCards);
//            }
//
////            if (experian == null || StringUtil.nullOrEmpty(experian.getResponseContent())) {
////                return createResponse_with_ONDC(false, "No Experian data found for phone: " + phone, "", Collections.emptySet());
////            }
//
//            JSONObject jsonData = new JSONObject(experian.getResponseContent());
//            JSONObject inProfile = jsonData.optJSONObject("INProfileResponse");
//            if (inProfile == null) {
//                return createResponse_with_ONDC(false, "INProfileResponse is null", "", Collections.emptySet(), activeCreditCards);
//            }
//
////            // Score Check
////            int score = 0;
////            JSONObject scoreObj = inProfile.optJSONObject("SCORE");
////            if (scoreObj != null) score = scoreObj.optInt("BureauScore", 0);
////            if (score < 700) {
////                return createResponse_with_ONDC(false, "Score below 700", "Score: " + score, Collections.emptySet());
////            }
//
//            // Report Date Check
//            JSONObject header = inProfile.optJSONObject("Header");
//            int reportDateInt = header != null ? header.optInt("ReportDate", 0) : 0;
//            if (reportDateInt == 0) return createResponse_with_ONDC(false, "Report date is null", "", Collections.emptySet(), activeCreditCards);
//            LocalDate buroReportDate = LocalDate.parse(String.format("%08d", reportDateInt), DateTimeFormatter.BASIC_ISO_DATE);
//            LocalDate buroReportMinusOneMonth = buroReportDate.minusMonths(1);
//
//            // Account Details
//            JSONObject caisAccountResponse = inProfile.optJSONObject("CAIS_Account");
//            Object caisAccountDetailsObject = caisAccountResponse != null ? caisAccountResponse.opt("CAIS_Account_DETAILS") : null;
//
//            JSONArray accountDetailsArray = caisAccountDetailsObject == null
//                    ? new JSONArray()
//                    : caisAccountDetailsObject instanceof JSONArray
//                        ? (JSONArray) caisAccountDetailsObject
//                        : new JSONArray().put(caisAccountDetailsObject);
//
//            int liveLoans = 0;
//          //  int lowTicketPL = 0;
//            int unsecuredGrowth6M = 0;
//
//            Set<Integer> unsecuredPLTypes = new HashSet<>(Arrays.asList(5,6,8,9,15,37,45,47,69));
//            // Step 1: Count active credit cards first
//            for (int i = 0; i < accountDetailsArray.length(); i++) {
//                JSONObject account = accountDetailsArray.optJSONObject(i);
//                if (account == null) continue;
//
//                int accountType = account.optInt("Account_Type", -1);
//                String dateClosed = account.optString("Date_Closed", "");
//                if (accountType == 10 && (dateClosed == null || dateClosed.isEmpty())) {
//                    activeCreditCards++;
//                }
//            }
//
//            // Step 2: Apply all other rules
//
//            for (int i = 0; i < accountDetailsArray.length(); i++) {
//                JSONObject account = accountDetailsArray.optJSONObject(i);
//                if (account == null) continue;
//                int accountStatus = account.optInt("Account_Status", -1);
//                int accountType = account.optInt("Account_Type", -1);
//                // Date Reported & Vintage Check
//                int dateReportedInt = account.optInt("Date_Reported", 0);
//                int openDateInt = account.optInt("Open_Date", 0);
//                if (dateReportedInt == 0 || openDateInt == 0) continue;
//
//                LocalDate openDate = LocalDate.parse(String.format("%08d", openDateInt), DateTimeFormatter.BASIC_ISO_DATE);
//                long vintageMonths = ChronoUnit.MONTHS.between(openDate, buroReportMinusOneMonth);
//                if (vintageMonths < 12) continue;
//
//                // Amount Past Due
//                if (account.optInt("Amount_Past_Due", 0) > 0) continue;
//
//                // Written-off status
//                Set<Integer> allowedWrittenOff = new HashSet<>(Arrays.asList(0, 99, 11, 12));
//                if (!allowedWrittenOff.contains(account.optInt("Written_off_Settled_Status", 0))) continue;
//
//                // Suit Filed / Wilful Default
//                String suit = account.optString("SuitFiled_WilfulDefault", "");
//                if (!"00".equalsIgnoreCase(suit) && !suit.isEmpty()) continue;
//
//                // DPD Checks
//                Object historyValue = account.opt("CAIS_Account_History");
//                JSONObject historyResult = processAccountHistory_Master_BRE_with_ONDC(historyValue, buroReportMinusOneMonth, activeCreditCards);
//                if (!historyResult.optBoolean("status", true)) return historyResult;
//
//                // Count live loans
////                int accountStatus = account.optInt("Account_Status", -1);
////                if (Arrays.asList(0,11,71,78,80,82,83,84,21,22,23,24,25).contains(accountStatus)) {
////                    liveLoans++;
////                }
//                
//                // if account active and accountType are credit cards (10,31,35,60) then we are not calculating the cc actives counts in loan count
//                if (Arrays.asList(0,11,71,78,80,82,83,84,21,22,23,24,25).contains(accountStatus)
//                        && !Arrays.asList(10,31,35,36).contains(accountType)) {
//                    liveLoans++;
//                }
//
//                // Low-ticket unsecured PL <= 25k
////                int loanAmount = account.optInt("Highest_Credit_or_Original_Loan_Amount", 0);
////                if (unsecuredPLTypes.contains(account.optInt("Account_Type", 0)) && loanAmount <= 25000) {
////                    lowTicketPL++;
////                }
//
//                // Unsecured growth in last 6 months
//                if (ChronoUnit.MONTHS.between(openDate, buroReportMinusOneMonth) <= 6 &&
//                    account.optInt("Secured_Flag",1) == 0 &&
//                    unsecuredPLTypes.contains(account.optInt("Account_Type",0))) {
//                    unsecuredGrowth6M++;
//                }
//            }
//
//            // Final Rule Checks
//            if (liveLoans > 5)
//                return createResponse_with_ONDC(false, "Live loans > 5", "Count: " + liveLoans, Collections.emptySet(),activeCreditCards);
////            if (lowTicketPL > 3)
////                return createResponse_with_ONDC(false, "Too many low ticket PLs", "Count: " + lowTicketPL, Collections.emptySet());
//            if (unsecuredGrowth6M > 3)
//                return createResponse_with_ONDC(false, "Unsecured growth > 3 in 6 months", "Count: " + unsecuredGrowth6M, Collections.emptySet(), activeCreditCards);
//
//            // Enquiry counts
//            JSONObject summary = inProfile.optJSONObject("TotalCAPS_Summary");
//            if (summary != null) {
//                if (summary.optInt("TotalCAPSLast30Days",0) >= 5) {
//                    return createResponse_with_ONDC(false, "More than 5 enquiries in last 1 month", "", Collections.emptySet(), activeCreditCards);
//                }
//                if (summary.optInt("TotalCAPSLast90Days",0) >= 10) {
//                    return createResponse_with_ONDC(false, "More than 10 enquiries in last 3 months", "", Collections.emptySet(), activeCreditCards);
//                }
//            }
//
//            return createResponse_with_ONDC(true, "All conditions passed", "", subscriberNames, activeCreditCards);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return createResponse_with_ONDC(false, "Exception: " + ex.getMessage(), "", Collections.emptySet(), activeCreditCards);
//        }
//    }
//
//    // ***************************************** DPD Check ************************************************
//    public JSONObject processAccountHistory_Master_BRE_with_ONDC(Object historyValue, LocalDate buro_report_minus_one_month, int activeCreditCards) {
//        try {
//            if (historyValue == null)
//                return createResponse_with_ONDC(true, "No history data", "", Collections.emptySet(), activeCreditCards);
//
//            JSONArray historyArray = historyValue instanceof JSONArray
//                    ? (JSONArray) historyValue
//                    : new JSONArray().put(historyValue instanceof JSONObject ? historyValue : new JSONObject());
//
//            int totalDPD12Months = 0;
//
//            for (int j = 0; j < historyArray.length(); j++) {
//                JSONObject caisAccountHistory = historyArray.optJSONObject(j);
//                if (caisAccountHistory == null) continue;
//
//                int daysPastDue = caisAccountHistory.optInt("Days_Past_Due",0);
//                int month = caisAccountHistory.optInt("Month",0);
//                int year = caisAccountHistory.optInt("Year",0);
//                if (month == 0 || year == 0) continue;
//
//                String formattedDate = String.format("%04d-%02d", year, month);
//                LocalDate startDate = LocalDate.parse(formattedDate + "-01");
//                long monthsDiff = ChronoUnit.MONTHS.between(startDate, buro_report_minus_one_month);
//
//                if (monthsDiff <= 12) totalDPD12Months += daysPastDue;
//
//                if (monthsDiff <= 6 && daysPastDue > 0) {
//                    return createResponse_with_ONDC(false, "Fail: 0+ DPD in last 6 months",
//                            "Days past due: " + daysPastDue + " in " + monthsDiff + " months", Collections.emptySet(), activeCreditCards);
//                }
//                if (monthsDiff <= 2 && daysPastDue > 0) {
//                    return createResponse_with_ONDC(false, "Fail: 0+ DPD in last 2 months",
//                            "Days past due: " + daysPastDue + " in " + monthsDiff + " months", Collections.emptySet(), activeCreditCards);
//                }
//            }
//
//            if (totalDPD12Months > 60) {
//                return createResponse_with_ONDC(false, "Fail: cumulative 60+ DPD in last 12 months",
//                        "Total DPD in last 12 months: " + totalDPD12Months, Collections.emptySet(), activeCreditCards);
//            }
//
//            return createResponse_with_ONDC(true, "Days past due checks passed", "", Collections.emptySet(), activeCreditCards);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return createResponse_with_ONDC(false, "History data error: " + e.getMessage(), "", Collections.emptySet(), activeCreditCards);
//        }
//    }
//
//    // ***************************************** Helper method ************************************************
//    public JSONObject createResponse_with_ONDC(boolean status, String reason, String response, Set<String> subscriber_name, int activeCreditCards) {
//        JSONObject json = new JSONObject();
//        try {
//            json.put("status", status);
//            json.put("reason", reason);
//            json.put("response", response);        
//            // Add Credit Card details in response
//            json.put("creditCard", activeCreditCards > 0 ? "1" : "0");//if yes then 1 else if 0 then no
//            json.put("creditCardCount", activeCreditCards);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return json;
//    }
//
//    // ***************************************** Optional Global Exception Handler *****************************************
//    @RestControllerAdvice
//    public class GlobalExceptionHandler {
//
//        @ExceptionHandler(Exception.class)
//        public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
//            ex.printStackTrace();
//            Map<String, Object> err = new HashMap<>();
//            err.put("status", false);
//            err.put("reason", "Unhandled exception: " + ex.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
//        }
//    }


    
    //==============================code by yogita=================================================
    
 
    // ***************************************** BRE logic for ONDC ************************************************
    public JSONObject processCreditReport_Master_BRE_ONDC(String phone) {
        Set<String> subscriberNames = new HashSet<>();
        int activeCreditCards = 0;
        try {
            UserBureauData experian = null;
            try {
//                experian = (UserBureauData) dao.get(ExperianData.class, "applyPhone='" + phone + "' order by createTime desc");
                Optional<UserBureauData> optionalExperian = userBureauDataRepository.findLatestByPhone(phone);
                if(optionalExperian.isEmpty() || StringUtil.nullOrEmpty(optionalExperian.get().getResponseContent()) ) {
                	return createResponse_with_ONDC(false, "No Experian data found for phone: " + phone, "", Collections.emptySet(), activeCreditCards);
                }
                
                experian = optionalExperian.get();
            } catch (Exception e) {
                e.printStackTrace();
                return createResponse_with_ONDC(false, "Database error: " + e.getMessage(), "", Collections.emptySet(), activeCreditCards);
            }

//            if (experian == null || StringUtil.nullOrEmpty(experian.getResponseContent())) {
//                return createResponse_with_ONDC(false, "No Experian data found for phone: " + phone, "", Collections.emptySet());
//            }

            JSONObject jsonData = new JSONObject(experian.getResponseContent());
            JSONObject inProfile = jsonData.optJSONObject("INProfileResponse");
            if (inProfile == null) {
                return createResponse_with_ONDC(false, "INProfileResponse is null", "", Collections.emptySet(), activeCreditCards);
            }
 
            // Report Date Check
            JSONObject header = inProfile.optJSONObject("Header");
            int reportDateInt = header != null ? header.optInt("ReportDate", 0) : 0;
            if (reportDateInt == 0) return createResponse_with_ONDC(false, "Report date is null", "", Collections.emptySet(), activeCreditCards);
            LocalDate buroReportDate = LocalDate.parse(String.format("%08d", reportDateInt), DateTimeFormatter.BASIC_ISO_DATE);
            LocalDate buroReportMinusOneMonth = buroReportDate.minusMonths(1);

            // Account Details
            JSONObject caisAccountResponse = inProfile.optJSONObject("CAIS_Account");
            Object caisAccountDetailsObject = caisAccountResponse != null ? caisAccountResponse.opt("CAIS_Account_DETAILS") : null;

            JSONArray accountDetailsArray = caisAccountDetailsObject == null
                    ? new JSONArray()
                    : caisAccountDetailsObject instanceof JSONArray
                        ? (JSONArray) caisAccountDetailsObject
                        : new JSONArray().put(caisAccountDetailsObject);

            int liveLoans = 0;
            int unsecuredGrowth6M = 0;

            Set<Integer> unsecuredPLTypes = new HashSet<>(Arrays.asList(5,6,8,9,15,37,45,47,69));

            // Step 1: Count active credit cards
            for (int i = 0; i < accountDetailsArray.length(); i++) {
                JSONObject account = accountDetailsArray.optJSONObject(i);
                if (account == null) continue;

                int accountType = account.optInt("Account_Type", -1);
                String dateClosed = account.optString("Date_Closed", "");
                if (accountType == 10 && (dateClosed == null || dateClosed.isEmpty())) {
                    activeCreditCards++;
                }
            }

            // ---------------- VINTAGE CHECK ----------------
            boolean hasVintage12OrMore = false;
            for (int i = 0; i < accountDetailsArray.length(); i++) {
                JSONObject account = accountDetailsArray.optJSONObject(i);
                if (account == null) continue;

                int openDateInt = account.optInt("Open_Date", 0);
                if (openDateInt == 0) continue;

                LocalDate openDate = LocalDate.parse(String.format("%08d", openDateInt), DateTimeFormatter.BASIC_ISO_DATE);
                long vintageMonths = ChronoUnit.MONTHS.between(openDate, buroReportMinusOneMonth);

                if (vintageMonths >= 12) {
                    hasVintage12OrMore = true;
                    break;
                }
            }

            // Apply reject logic
            if (accountDetailsArray.length() == 1) {
                JSONObject singleAccount = accountDetailsArray.optJSONObject(0);
                if (singleAccount != null) {
                    int openDateInt = singleAccount.optInt("Open_Date", 0);
                    if (openDateInt > 0) {
                        LocalDate openDate = LocalDate.parse(String.format("%08d", openDateInt), DateTimeFormatter.BASIC_ISO_DATE);
                        long vintageMonths = ChronoUnit.MONTHS.between(openDate, buroReportMinusOneMonth);
                        if (vintageMonths < 12) {
                            return createResponse_with_ONDC(false, "Vintage < 12 months for single account", "Vintage: " + vintageMonths + " months", Collections.emptySet(), activeCreditCards);
                        }
                    }
                }
            } else if (accountDetailsArray.length() > 1 && !hasVintage12OrMore) {
                return createResponse_with_ONDC(false, "No account with vintage ≥ 12 months", "", Collections.emptySet(), activeCreditCards);
            }
            //---------------- END VINTAGE CHECK ----------------

            // Step 2: Apply all other rules
            for (int i = 0; i < accountDetailsArray.length(); i++) {
                JSONObject account = accountDetailsArray.optJSONObject(i);
                if (account == null) continue;

                int accountStatus = account.optInt("Account_Status", -1);
                int accountType = account.optInt("Account_Type", -1);

                int dateReportedInt = account.optInt("Date_Reported", 0);
                int openDateInt = account.optInt("Open_Date", 0);
                if (dateReportedInt == 0 || openDateInt == 0) continue;

                LocalDate openDate = LocalDate.parse(String.format("%08d", openDateInt), DateTimeFormatter.BASIC_ISO_DATE);
                long vintageMonths = ChronoUnit.MONTHS.between(openDate, buroReportMinusOneMonth);

                // Amount Past Due
                if (account.optInt("Amount_Past_Due", 0) > 5000) {
                    return createResponse_with_ONDC(false, "Fail in amount past due", "Amount past due is " + account.optInt("Amount_Past_Due", 0), Collections.emptySet(), activeCreditCards);
                }

                // Written-off status
                Set<Integer> allowedWrittenOff = new HashSet<>(Arrays.asList(0, 99, 11, 12));
                if (!allowedWrittenOff.contains(account.optInt("Written_off_Settled_Status", 0))) {
                    return createResponse_with_ONDC(false, "Fail in Written_off_Settled_Status value", "Value: " + account.optInt("Written_off_Settled_Status", 0), Collections.emptySet(), activeCreditCards);
                }

                //Suit Filed / Wilful Default
                String suit = account.optString("SuitFiled_WilfulDefault", "");
                if (!"00".equalsIgnoreCase(suit) && !suit.isEmpty()) {
                    return createResponse_with_ONDC(false, "Fail in SuitFiled_WilfulDefault value", "Value: " + suit, Collections.emptySet(), activeCreditCards);
                }

                //DPD Checks
                Object historyValue = account.opt("CAIS_Account_History");
                JSONObject historyResult = processAccountHistory_Master_BRE_with_ONDC(historyValue, buroReportMinusOneMonth, activeCreditCards);
                if (!historyResult.optBoolean("status", true)) return historyResult;

                //Live loans (excluding CC)
                if (Arrays.asList(0,11,71,78,80,82,83,84,21,22,23,24,25).contains(accountStatus)
                        && !Arrays.asList(10,31,35,36).contains(accountType)) {
                    liveLoans++;
                }

                //Unsecured growth in last 6 months
                if (ChronoUnit.MONTHS.between(openDate, buroReportMinusOneMonth) <= 6 &&
                    account.optInt("Secured_Flag",1) == 0 &&
                    unsecuredPLTypes.contains(account.optInt("Account_Type",0))) {
                    unsecuredGrowth6M++;
                }
            }

            //Final Rule Checks
            if (liveLoans > 10)
                return createResponse_with_ONDC(false, "Live loans > 10", "Count: " + liveLoans, Collections.emptySet(), activeCreditCards);
            if (unsecuredGrowth6M > 3)
                return createResponse_with_ONDC(false, "Unsecured growth > 3 in 6 months", "Count: " + unsecuredGrowth6M, Collections.emptySet(), activeCreditCards);

            //Enquiry counts
            JSONObject summary = inProfile.optJSONObject("TotalCAPS_Summary");
            if (summary != null) {
                if (summary.optInt("TotalCAPSLast30Days",0) >= 5) {
                    return createResponse_with_ONDC(false, "More than 5 enquiries in last 1 month", "", Collections.emptySet(), activeCreditCards);
                }
                if (summary.optInt("TotalCAPSLast90Days",0) >= 10) {
                    return createResponse_with_ONDC(false, "More than 10 enquiries in last 3 months", "", Collections.emptySet(), activeCreditCards);
                }
            }

            return createResponse_with_ONDC(true, "All conditions passed", "", subscriberNames, activeCreditCards);

        } catch (Exception ex) {
            ex.printStackTrace();
            return createResponse_with_ONDC(false, "Exception: " + ex.getMessage(), "", Collections.emptySet(), activeCreditCards);
        }
    }

    // ***************************************** DPD Check ************************************************
    public JSONObject processAccountHistory_Master_BRE_with_ONDC(Object historyValue, LocalDate buro_report_minus_one_month, int activeCreditCards) {
        try {
            if (historyValue == null)
                return createResponse_with_ONDC(true, "No history data", "", Collections.emptySet(), activeCreditCards);

            JSONArray historyArray = historyValue instanceof JSONArray
                    ? (JSONArray) historyValue
                    : new JSONArray().put(historyValue instanceof JSONObject ? historyValue : new JSONObject());

            int totalDPD12Months = 0;

            for (int j = 0; j < historyArray.length(); j++) {
                JSONObject caisAccountHistory = historyArray.optJSONObject(j);
                if (caisAccountHistory == null) continue;

                int daysPastDue = caisAccountHistory.optInt("Days_Past_Due",0);
                int month = caisAccountHistory.optInt("Month",0);
                int year = caisAccountHistory.optInt("Year",0);
                if (month == 0 || year == 0) continue;

                String formattedDate = String.format("%04d-%02d", year, month);
                LocalDate startDate = LocalDate.parse(formattedDate + "-01");
                long monthsDiff = ChronoUnit.MONTHS.between(startDate, buro_report_minus_one_month);

                if (monthsDiff <= 12) totalDPD12Months += daysPastDue;

                if (monthsDiff <= 6 && daysPastDue > 30) {
                    return createResponse_with_ONDC(false, "Fail: 30+ DPD in last 6 months",
                            "Days past due: " + daysPastDue + " in " + monthsDiff + " months", Collections.emptySet(), activeCreditCards);
                }
                if (monthsDiff <= 2 && daysPastDue > 0) {
                    return createResponse_with_ONDC(false, "Fail: 0+ DPD in last 2 months",
                            "Days past due: " + daysPastDue + " in " + monthsDiff + " months", Collections.emptySet(), activeCreditCards);
                }
            }

            if (totalDPD12Months > 60) {
                return createResponse_with_ONDC(false, "Fail: cumulative 60+ DPD in last 12 months",
                        "Total DPD in last 12 months: " + totalDPD12Months, Collections.emptySet(), activeCreditCards);
            }

            return createResponse_with_ONDC(true, "Days past due checks passed", "", Collections.emptySet(), activeCreditCards);

        } catch (Exception e) {
            e.printStackTrace();
            return createResponse_with_ONDC(false, "History data error: " + e.getMessage(), "", Collections.emptySet(), activeCreditCards);
        }
    }

//    // ***************************************** Helper method ************************************************
//    public JSONObject createResponse_with_ONDC(boolean status, String reason, String response, Set<String> subscriber_name, int activeCreditCards) {
//        JSONObject json = new JSONObject();
//        try {
//            json.put("status", status);
//            json.put("reason", reason);
//            json.put("response", response);
//            json.put("creditCard", activeCreditCards > 0 ? "1" : "0");
//            json.put("creditCardCount", activeCreditCards);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return json;
//    }
    
    // ***************************************** Helper method ************************************************
    public JSONObject createResponse_with_ONDC(boolean status, String reason, String response, Set<String> subscriber_name, int activeCreditCards) {
        JSONObject json = new JSONObject();
        try {
            json.put("status", status);
            json.put("reason", reason);
            json.put("response", response);        
            // Add Credit Card details in response
            json.put("creditCard", activeCreditCards > 0 ? "1" : "0");//if yes then 1 else if 0 then no
            json.put("creditCardCount", activeCreditCards);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return json;
    }

    // ***************************************** Optional Global Exception Handler *****************************************
    @RestControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
            ex.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("status", false);
            err.put("reason", "Unhandled exception: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
//	  // IMPORTANT: You need to implement this method or it will cause errors
//	  private JSONObject processAccountHistory_Master_BRE_with_ONDC(JSONObject history, LocalDate buroReportMinusOneMonth) {
//	      // For now, return success to avoid errors
//	      // You should implement the actual DPD check logic here
//	      JSONObject result = new JSONObject();
//	      try {
//	          result.put("status", true);
//	          result.put("reason", "DPD checks passed");
//	      } catch (Exception e) {
//	          result.put("status", false);
//	          result.put("reason", "DPD check error: " + e.getMessage());
//	      }
//	      return result;
//	  } 
    
  //==================================function for bajaj product==================================================
    @PostMapping("/check-subscriber-bajaj")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<Map<String, Object>> checkSubscriberBajaj(
            @RequestParam String phone,
            @RequestParam int income,
            @RequestParam String companyName) {

        Map<String, Object> result = new HashMap<>();
        try {
            // Fetch latest Experian data
            UserBureauData experian = null;
            
            Optional<UserBureauData> optionalExperian = userBureauDataRepository.findLatestByPhone(phone);
            
            if(optionalExperian.isEmpty() || StringUtil.nullOrEmpty(optionalExperian.get().getResponseContent()) ) {
            	result.put("status", false);
                result.put("reason", "No Experian data found for phone: " + phone);
                return ResponseEntity.ok(result);
            }
            
            experian = optionalExperian.get();

//            if (experian == null || StringUtil.nullOrEmpty(experian.getResponseContent())) {
//                result.put("status", false);
//                result.put("reason", "No Experian data found for phone: " + phone);
//                return ResponseEntity.ok(result);
//            }

            JSONObject jsonData = new JSONObject(experian.getResponseContent());
            JSONObject inProfile = jsonData.optJSONObject("INProfileResponse");
            if (inProfile == null) {
                result.put("status", false);
                result.put("reason", "INProfileResponse is null");
                return ResponseEntity.ok(result);
            }

            // Extract score from Experian data
            JSONObject scoreObj = inProfile.optJSONObject("SCORE");
            int score = scoreObj != null ? scoreObj.optInt("BureauScore", 0) : 0;

            // Fetch subscriber names from accounts
            Set<String> bajajSubscribers = new HashSet<>();
            JSONObject caisAccountResponse = inProfile.optJSONObject("CAIS_Account");
            Object accountObj = caisAccountResponse != null ? caisAccountResponse.opt("CAIS_Account_DETAILS") : null;

            JSONArray accountArray = accountObj == null
                    ? new JSONArray()
                    : accountObj instanceof JSONArray
                        ? (JSONArray) accountObj
                        : new JSONArray().put(accountObj);

            for (int i = 0; i < accountArray.length(); i++) {
                JSONObject account = accountArray.optJSONObject(i);
                if (account == null) continue;
                String subscriberName = account.optString("Subscriber_Name", "");
                if (subscriberName.toLowerCase().startsWith("bajaj")) {
                    bajajSubscribers.add(subscriberName);
                }
            }

            // If Bajaj subscriber exists
            if (!bajajSubscribers.isEmpty()) {
                result.put("status", true);
                result.put("reason", "Bajaj subscriber found");
                result.put("bajajSubscribers", bajajSubscribers);
                result.put("score", score);
                return ResponseEntity.ok(result);
            }

            // ---------- If no Bajaj subscriber, check income/company category ----------
            // Fetch company details from table
//            LnTCompanyName companyEntity = (LnTCompanyName) dao.get(
//                    LnTCompanyName.class,
//                    "companyName='" + companyName + "'"
//            );
            
            CompanyMaster companyEntity;
            
            Optional<CompanyMaster> optionalCompanyEntity = companyMasterRepository.findByCompanyName(companyName);
            
            String category = null;
            if(optionalCompanyEntity.isPresent()) {
            	
            	companyEntity = optionalCompanyEntity.get();
            	category = companyEntity.getCategory();
            }
            
            

//            String category = companyEntity != null ? companyEntity.getCategory() : null;

            if (score >= 750 && income >= 75000 && "CAT A".equalsIgnoreCase(category)) {
                result.put("status", true);
                result.put("reason", "Conditions passed (no Bajaj subscriber)");
            } else {
                result.put("status", false);
                result.put("reason", "Conditions failed (no Bajaj subscriber)");
            }

            result.put("score", score);
            result.put("income", income);
            result.put("company", companyName);
            result.put("category", category);

        } catch (Exception ex) {
            ex.printStackTrace();
            result.put("status", false);
            result.put("reason", "Exception: " + ex.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
