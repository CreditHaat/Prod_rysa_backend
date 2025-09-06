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
import com.lsp.web.entity.JourneyLog;
import com.lsp.web.entity.Logger;
import com.lsp.web.entity.UserInfo;
import com.lsp.web.Exception.InvalidInputException;
import com.lsp.web.Exception.OtpValidationException;
import com.lsp.web.repository.JourneyLogRepository;
import com.lsp.web.repository.LoggerRepository;
import com.lsp.web.repository.UserInfoRepository;

@Service
public class UserInfoService {

	@Autowired
	private UserInfoRepository userInfoRepository;

	@Autowired
	private UserInfoMapper userInfoMapper;

	@Autowired
	private UserBureauDataService userBureauDataService;
	
	@Autowired
	private JourneyLogRepository journeyLogRepository;
	
	@Autowired
	private LoggerRepository loggerRepository;

	// *****************new code for otp generation******************************/

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

			dto.setMobileNumber(input.optString("mobileNumber", input.optString("Mobilenumber", null)));

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



	public Map<?, ?> verifyOtp(JSONObject input) {
		// external api call
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("https://loan.credithaat.com/d2cforinternal/otpvalidate");

		httpPost.setHeader("token", "Y3JlZGl0aGFhdHRlc3RzZXJ2ZXI="); // Replace with actual token
		httpPost.setHeader("Content-Type", "application/json");

		String jsonResponse = null;

		try {
			StringEntity entity = new StringEntity(input.toString());
			httpPost.setEntity(entity);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			jsonResponse = EntityUtils.toString(httpResponse.getEntity());
		} catch (Exception e) {
			
			String mobileNumber = input.optString("Mobilenumber", null);
			saveJourneyLogAndLogger(mobileNumber, "verifyOtp", 1, input, e.getMessage());

		}

		if (jsonResponse == null || jsonResponse.trim().isEmpty() || jsonResponse.trim().equalsIgnoreCase("null")) {
			Map<String, Object> data = new HashMap<>();
			data.put("score", null);
			data.put("active_account_count", 0);
			data.put("code", 2);
			data.put("message", "otp verified successfully but experian not found");

			String mobileNumber = input.optString("Mobilenumber", null);
			Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);

			if (optionalUser.isPresent()) {
				UserInfo user = optionalUser.get();
				user.setCreditProfile("1000");
				userInfoRepository.save(user);
			}
			
			saveJourneyLogAndLogger(mobileNumber, "verifyOtp", 1, input, data);

			return data; // ⬅️ return early if response is null/empty
		}

		try {
			JSONObject responseJson = new JSONObject(jsonResponse);
			if ("OTP validation failed, OTP is not match".equalsIgnoreCase(responseJson.optString("errorString"))) {
				Map<String, Object> data = new HashMap<>();
				data.put("code", -1);
				data.put("message", responseJson.getString("errorString"));
				String stgOneHitId = input.optString("stgOneHitId", null);
				String stgTwoHitId = input.optString("stgTwoHitId", null);
				if(!stgOneHitId.equalsIgnoreCase("000000") && !stgTwoHitId.equals("000000")) {
					data.put("isExperianOtp", "true");
				}
				
				String mobileNumber = input.optString("Mobilenumber", null);
				saveJourneyLogAndLogger(mobileNumber, "verifyOtp", 1, input, data);
				
				return data;
			}
			
		} catch (Exception e) {
			
			
			String mobileNumber = input.optString("Mobilenumber", null);
			saveJourneyLogAndLogger(mobileNumber, "verifyOtp", 1, input, e.getMessage());
			
			Map<String, Object> data = new HashMap<>();
			data.put("code", -1);
			data.put("message", "otp not verified");
			
			return data;
			
			

		}
		
		try {
			if (!jsonResponse.trim().startsWith("{")) {
				throw new Exception("Invalid response format. Not a JSON object: " + jsonResponse);
			}
		}catch(Exception e) {
			String mobileNumber = input.optString("Mobilenumber", null);
			saveJourneyLogAndLogger(mobileNumber, "verifyOtp", 1, input, e.getMessage());
			
		}
		

		// Parse JSON response
		try {
			JSONObject json = new JSONObject(jsonResponse);
			JSONObject inProfileResponse = json.optJSONObject("INProfileResponse");
			// JSONObject errorString = json.optJSONObject("errorString");

			if (inProfileResponse == null) {
				Map<String, Object> data = new HashMap<>();
				data.put("score", null);
				data.put("active_account_count", 0);
				// data.put("finalScore", finalScore);
				data.put("code", 2);// code 2 is for otp verified succesfully but experian not found
				data.put("message", "otp verified succesfully but experian not found");
				String mobileNumber = input.optString("Mobilenumber", null);
				Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);

				
				
				if (optionalUser.isPresent()) {
					UserInfo user = optionalUser.get();
//			                user.setFinalScore((int)finalScore);  // ✅ Make sure this setter exists in your UserInfo entity
					user.setCreditProfile("1000");
					userInfoRepository.save(user);
				}
				
				saveJourneyLogAndLogger(mobileNumber, "verifyOtp", 1, input, data);
				
				return data;

			}
			
			
		}catch (Exception e) {
			
			Map<String, Object> data = new HashMap<>();
			data.put("code", 2);// code 2 is for otp verified succesfully but experian not found
			data.put("message", "otp verified but got some exception while checking INProfileResponse");
			
			String mobileNumber = input.optString("Mobilenumber", null);
			saveJourneyLogAndLogger(mobileNumber, "verifyOtp", 1, input, e.getMessage());
			
			return data;
		} 
		
		
		try {
			//if inprofile is not null then we will get the values from inprofile
			JSONObject json = new JSONObject(jsonResponse);
			JSONObject inProfileResponse = json.optJSONObject("INProfileResponse");
			String mobileNumber = input.optString("Mobilenumber", null);
			Map<String, Object> data = processVerifyOtpBureauData(mobileNumber, inProfileResponse);
			if(data==null) {
				data = new HashMap<>();
			}
			data.put("code", 1);// code 1 is for otp verified succesfully
			data.put("message", "otp verified succesfully");
			
			return data;
			
		}catch(Exception e) {
			
			Map<String, Object> data = new HashMap<>();
			data.put("code", 1);// code 2 is for otp verified succesfully but experian not found
			data.put("message", "otp verified but got some exception while getting INProfileResponse");
			
			String mobileNumber = input.optString("Mobilenumber", null);
			saveJourneyLogAndLogger(mobileNumber, "verifyOtp", 1, input, e.getMessage());
			
			return data;
			
		}
		
//		Map<String, Object> data = new HashMap<>();
//		data.put("score", score);
//		data.put("pan", pan);
//		data.put("dob", dob);
//		data.put("email", email);
//		data.put("gender", gender);
//		data.put("pincode", pincode);

	}
	
	public Map<String, Object> processVerifyOtpBureauData(String mobileNumber, JSONObject inProfileResponse){
		
		String score = null;
//		String pan = null;
//		String dob = null;
//		String email = null;
//		String gender = null;
//		String pincode = null;
		
		try {
			
			// Extract score
						JSONObject scoreJson = inProfileResponse.optJSONObject("SCORE");
						score = scoreJson != null ? scoreJson.optString("BureauScore", null) : null;
						
						//here we will write a code to save the bureauData before any processing--------------
						
						Optional<UserInfo> userInfoOpt = userInfoRepository.findByMobileNumber(mobileNumber);
						
						try {
							if (userInfoOpt.isPresent() && inProfileResponse != null) {
								UserInfo user = userInfoOpt.get();
								if (score != null && !score.trim().isEmpty()) {
									user.setCreditProfile(score);
								} else {
									// fallback to 1000 if score is missing
									user.setCreditProfile("1000");
								}
								userInfoRepository.save(user);
								//first we will set data into user table

								// Wrap the JSON in {"INProfileResponse": {...}}
								JSONObject wrappedResponse = new JSONObject();
								wrappedResponse.put("INProfileResponse", inProfileResponse);

								//here we are setting the bureau data at first beofre doing any process on bureau data to avoid any exception
								userBureauDataService.saveOrUpdateBureauData(user, score, wrappedResponse.toString());
							
							
							}
						}catch(Exception e) {
							saveJourneyLogAndLogger(mobileNumber, "processVerifyOtpBureauData", 1, "error while saving BureauData data before processing fields", e.getMessage());
						}
						
						
						
						//-------------------------------------------------------------------------------
						
						// ===== Step 1: Try from Current_Applicant_Details first =====
//						JSONObject applicantDetails = inProfileResponse.optJSONObject("Current_Application")
//								.optJSONObject("Current_Application_Details").optJSONObject("Current_Applicant_Details");
//
//						JSONObject applicantAddress = inProfileResponse.optJSONObject("Current_Application")
//								.optJSONObject("Current_Application_Details").optJSONObject("Current_Applicant_Address_Details");

//						pan = applicantDetails != null ? applicantDetails.optString("IncomeTaxPan", "") : "";
//						dob = applicantDetails != null ? applicantDetails.optString("Date_Of_Birth_Applicant", "") : "";
//						email = applicantDetails != null ? applicantDetails.optString("EMailId", "") : "";
//						gender = applicantDetails != null ? applicantDetails.optString("Gender_Code", "") : "";
//						pincode = applicantAddress != null ? applicantAddress.optString("PINCode", "") : "";
						
						// ===== Step 2: Fallback to CAIS_Account_DETAILS if missing =====
//						if (pan.isEmpty() || dob.isEmpty() || email.isEmpty() || gender.isEmpty() || pincode.isEmpty()) {
//						if (dob.isEmpty() || gender.isEmpty()) {	
//						JSONObject caisAccount = inProfileResponse.optJSONObject("CAIS_Account");
//							if (caisAccount != null) {
//								JSONArray accountDetails = caisAccount.optJSONArray("CAIS_Account_DETAILS");
//								if (accountDetails != null && accountDetails.length() > 0) {
//									JSONObject firstAcc = accountDetails.optJSONObject(0);
//									if (firstAcc != null) {
//										JSONObject holderDetails = firstAcc.optJSONObject("CAIS_Holder_Details");
//										if (holderDetails != null) {
//											if (dob.isEmpty())
//												dob = holderDetails.optString("Date_of_birth", "");
////											if (email.isEmpty())
////												email = holderDetails.optString("EMailId", "");
//											if (gender.isEmpty())
//												gender = holderDetails.optString("Gender_Code", "");
//										}

//										JSONArray addressArray = firstAcc.optJSONArray("CAIS_Holder_Address_Details");
//										if (addressArray != null && addressArray.length() > 0) {
//											JSONObject firstAddress = addressArray.optJSONObject(0);
//											if (pincode.isEmpty()) {
//												pincode = firstAddress.optString("ZIP_Postal_Code_non_normalized", "");
//											}
//										}
//									}
//								}
//							}
//						}
//						score = scoreJson != null ? scoreJson.optString("BureauScore", null) : null;

			//================================================this code is used to save  bereauresponse in table====== 

//						String mobileNumber = input.optString("Mobilenumber", null);
//						Optional<UserInfo> userInfoOpt = userInfoRepository.findByMobileNumber(mobileNumber);//comment by tejas

						if (userInfoOpt.isPresent() && inProfileResponse != null) {
							UserInfo user = userInfoOpt.get();

							// Wrap the JSON in {"INProfileResponse": {...}}
							//commented by tejas
//							JSONObject wrappedResponse = new JSONObject();
//							wrappedResponse.put("INProfileResponse", inProfileResponse);
//
//							userBureauDataService.saveOrUpdateBureauData(user, score, wrappedResponse.toString());
							//comment by tejas end here

//					                if (score != null && !score.trim().isEmpty()) {
//					                    user.setCreditProfile(score);
//					                    userInfoRepository.save(user); 
//					                }
//							if (score != null && !score.trim().isEmpty()) {
//								user.setCreditProfile(score);
//							} else {
//								// fallback to 1000 if score is missing
//								user.setCreditProfile("1000");
//							}

							// ✅ Save additional fields if present
//							if (email != null && !email.trim().isEmpty()) {
//								user.setEmail(email);
//							}
//							if (dob != null && !dob.trim().isEmpty()) {
//								try {
//									// Parse "19700101" into LocalDate
//									DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//									LocalDate parsedDob = LocalDate.parse(dob, inputFormatter);
//
//									// If your UserInfo entity dob is String → save formatted String
//									DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//									user.setDob(parsedDob.format(outputFormatter));
//
//									// If your UserInfo entity dob is LocalDate → just set parsedDob
//									// user.setDob(parsedDob);
//								} catch (Exception e) {
////									System.out.println("Invalid DOB format: " + dob);
//								}
//							}

							// ✅ Gender (convert String → Integer)
//							if (gender != null && !gender.trim().isEmpty()) {
//								try {
//									user.setGender(Integer.parseInt(gender));
//								} catch (NumberFormatException e) {
////									System.out.println("Invalid gender value: " + gender);
//								}
//							}

							// ✅ Pincode (convert String → Integer)
//							if (pincode != null && !pincode.trim().isEmpty()) {
//								try {
//									user.setResidentialPincode(Integer.parseInt(pincode));
//								} catch (NumberFormatException e) {
////									System.out.println("Invalid pincode value: " + pincode);
//								}
//							}

							// ✅ Persist updates
							userInfoRepository.save(user);
						}

						Map<String, Object> data = new HashMap<>();
						data.put("score", score);
//						data.put("pan", pan);
//						data.put("dob", dob);
//						data.put("email", email);
//						data.put("gender", gender);
//						data.put("pincode", pincode);

//						data.put("code", 1);// code 1 is for otp verified succesfully
//						data.put("message", "otp verified succesfully");

						return data;
						
						
			
		}catch(Exception e) {
			saveJourneyLogAndLogger(mobileNumber, "processVerifyOtpBureauData", 1, inProfileResponse, e.getMessage());
			Map<String, Object> eMap = new HashMap<>();
			eMap.put("score", score);
//			eMap.put("pan", pan);
//			eMap.put("dob", dob);
//			eMap.put("email", email);
//			eMap.put("gender", gender);
//			eMap.put("pincode", pincode);
			return eMap;
		}
	}
	
	public void saveJourneyLogAndLogger(String mobileNumber,String requestId,int stage, Object requestPayload,Object responsePayload) {
		try {
			JourneyLog journeyLog = new JourneyLog();
			journeyLog.setPlatformId("A");
			journeyLog.setStage(stage);
			journeyLog.setRequestId(requestId);
			
			if(mobileNumber!=null) {
				Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);
				if(optionalUser.isPresent()) {
					journeyLog.setUser(optionalUser.get());
				}
			}
			
			
			journeyLogRepository.save(journeyLog);
			
			Logger logger = new Logger();
			logger.setJourneyLog(journeyLog);
//          logger.setUrl(gatewayUrl);// this url doesnt refers the value of api url it holds the url if we get from response of that api
			logger.setRequestPayload(String.valueOf(requestPayload));
			logger.setResponsePayload(String.valueOf(responsePayload));

			loggerRepository.save(logger);
			
		}catch(Exception e) {
			
		}
	}

//	public Map<?, ?> verifyOtp1(JSONObject input) {
//		try {
//			// Prepare external API call
//			HttpClient httpClient = HttpClients.createDefault();
//			HttpPost httpPost = new HttpPost("https://loan.credithaat.com/d2cforinternal/otpvalidate");
//
////		           HttpPost httpPost = new HttpPost("http://localhost/d2cforinternal/otpvalidate");
//
//			httpPost.setHeader("token", "Y3JlZGl0aGFhdHRlc3RzZXJ2ZXI="); // Replace with actual token
//			httpPost.setHeader("Content-Type", "application/json");
//
//			StringEntity entity = new StringEntity(input.toString());
//			httpPost.setEntity(entity);
//
//			HttpResponse httpResponse = httpClient.execute(httpPost);
//			String jsonResponse = EntityUtils.toString(httpResponse.getEntity());
//
//			// System.out.println("Raw JSON Response from Experian API: " + jsonResponse);
//
//			if (jsonResponse == null || jsonResponse.trim().isEmpty() || jsonResponse.trim().equalsIgnoreCase("null")) {
//				Map<String, Object> data = new HashMap<>();
//				data.put("score", null);
//				data.put("active_account_count", 0);
//				data.put("code", 2);
//				data.put("message", "otp verified successfully but experian not found");
//				long finalScore = 20;
//				data.put("finalScore", finalScore);
//
//				String mobileNumber = input.optString("Mobilenumber", null);
//				Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);
//
//				if (optionalUser.isPresent()) {
//					UserInfo user = optionalUser.get();
//					user.setCreditProfile("1000");
//					userInfoRepository.save(user);
//				}
//
//				return data; // ⬅️ return early if response is null/empty
//			}
//
//			JSONObject responseJson = new JSONObject(jsonResponse);
//			if ("OTP validation failed, OTP is not match".equalsIgnoreCase(responseJson.optString("errorString"))) {
//				Map<String, Object> data = new HashMap<>();
//				data.put("code", -1);
//				data.put("message", responseJson.getString("errorString"));
//				return data;
//			}
//
//			// Handle null or empty response
//			if (jsonResponse == null || jsonResponse.trim().isEmpty() || jsonResponse.trim().equalsIgnoreCase("null")) {
//				Map<String, Object> data = new HashMap<>();
//				data.put("score", null);
//				data.put("active_account_count", 0);
//				data.put("code", 2);
//				data.put("message", "otp verified succesfully but experian not found");
//				long finalScore = 20;
//				data.put("finalScore", finalScore);
//
//				String mobileNumber = input.optString("Mobilenumber", null);
//				Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);
//
//				if (optionalUser.isPresent()) {
//					UserInfo user = optionalUser.get();
//					user.setCreditProfile("1000");
////			                user.setFinalScore((int)finalScore);  // ✅ Make sure this setter exists in your UserInfo entity
//					userInfoRepository.save(user);
//				}
//				return data;
//
//			}
//
//			if (!jsonResponse.trim().startsWith("{")) {
//				throw new Exception("Invalid response format. Not a JSON object: " + jsonResponse);
//			}
//
//			// Parse JSON response
//			JSONObject json = new JSONObject(jsonResponse);
//
//			// Step 1: Check if errorString is missing or null → means OTP is invalid
////		         String errorString = json.optString("errorString", null);
////		         if (errorString == null || errorString.trim().isEmpty()) {
////		             Map<String, Object> data = new HashMap<>();
////		             data.put("code", -1);
////		             data.put("message", "Invalid OTP");
////		             return data;
////		         }
//			JSONObject inProfileResponse = json.optJSONObject("INProfileResponse");
//			// JSONObject errorString = json.optJSONObject("errorString");
//
//			if (inProfileResponse == null) {
//				Map<String, Object> data = new HashMap<>();
//				data.put("score", null);
//				data.put("active_account_count", 0);
//				// data.put("finalScore", finalScore);
//				data.put("code", 2);// code 2 is for otp verified succesfully but experian not found
//				data.put("message", "otp verified succesfully but experian not found");
//				long finalScore = 20;
//				data.put("finalScore", finalScore);
//				String mobileNumber = input.optString("Mobilenumber", null);
//				Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);
//
//				if (optionalUser.isPresent()) {
//					UserInfo user = optionalUser.get();
////			                user.setFinalScore((int)finalScore);  // ✅ Make sure this setter exists in your UserInfo entity
//					user.setCreditProfile("1000");
//					userInfoRepository.save(user);
//				}
//				return data;
//
//			}
//
//			// Extract score
//			JSONObject scoreJson = inProfileResponse.optJSONObject("SCORE");
//			// ===== Step 1: Try from Current_Applicant_Details first =====
//			JSONObject applicantDetails = inProfileResponse.optJSONObject("Current_Application")
//					.optJSONObject("Current_Application_Details").optJSONObject("Current_Applicant_Details");
//
//			JSONObject applicantAddress = inProfileResponse.optJSONObject("Current_Application")
//					.optJSONObject("Current_Application_Details").optJSONObject("Current_Applicant_Address_Details");
//
//			String pan = applicantDetails != null ? applicantDetails.optString("IncomeTaxPan", "") : "";
//			String dob = applicantDetails != null ? applicantDetails.optString("Date_Of_Birth_Applicant", "") : "";
//			String email = applicantDetails != null ? applicantDetails.optString("EMailId", "") : "";
//			String gender = applicantDetails != null ? applicantDetails.optString("Gender_Code", "") : "";
//			String pincode = applicantAddress != null ? applicantAddress.optString("PINCode", "") : "";
//
//			// ===== Step 2: Fallback to CAIS_Account_DETAILS if missing =====
//			if (pan.isEmpty() || dob.isEmpty() || email.isEmpty() || gender.isEmpty() || pincode.isEmpty()) {
//				JSONObject caisAccount = inProfileResponse.optJSONObject("CAIS_Account");
//				if (caisAccount != null) {
//					JSONArray accountDetails = caisAccount.optJSONArray("CAIS_Account_DETAILS");
//					if (accountDetails != null && accountDetails.length() > 0) {
//						JSONObject firstAcc = accountDetails.optJSONObject(0);
//						if (firstAcc != null) {
//							JSONObject holderDetails = firstAcc.optJSONObject("CAIS_Holder_Details");
//							if (holderDetails != null) {
//								if (pan.isEmpty())
//									pan = holderDetails.optString("Income_TAX_PAN", "");
//								if (dob.isEmpty())
//									dob = holderDetails.optString("Date_of_birth", "");
//								if (email.isEmpty())
//									email = holderDetails.optString("EMailId", "");
//								if (gender.isEmpty())
//									gender = holderDetails.optString("Gender_Code", "");
//							}
//
//							JSONArray addressArray = firstAcc.optJSONArray("CAIS_Holder_Address_Details");
//							if (addressArray != null && addressArray.length() > 0) {
//								JSONObject firstAddress = addressArray.optJSONObject(0);
//								if (pincode.isEmpty()) {
//									pincode = firstAddress.optString("ZIP_Postal_Code_non_normalized", "");
//								}
//							}
//						}
//					}
//				}
//			}
//			String score = scoreJson != null ? scoreJson.optString("BureauScore", null) : null;
//
////================================================this code is used to save  bereauresponse in table====== 
//
//			String mobileNumber = input.optString("Mobilenumber", null);
//			Optional<UserInfo> userInfoOpt = userInfoRepository.findByMobileNumber(mobileNumber);
//
//			if (userInfoOpt.isPresent() && inProfileResponse != null) {
//				UserInfo user = userInfoOpt.get();
//
//				// Wrap the JSON in {"INProfileResponse": {...}}
//				JSONObject wrappedResponse = new JSONObject();
//				wrappedResponse.put("INProfileResponse", inProfileResponse);
//
//				userBureauDataService.saveOrUpdateBureauData(user, score, wrappedResponse.toString());
//
////		                if (score != null && !score.trim().isEmpty()) {
////		                    user.setCreditProfile(score);
////		                    userInfoRepository.save(user); 
////		                }
//				if (score != null && !score.trim().isEmpty()) {
//					user.setCreditProfile(score);
//				} else {
//					// fallback to 1000 if score is missing
//					user.setCreditProfile("1000");
//				}
//
//				// ✅ Save additional fields if present
//				if (email != null && !email.trim().isEmpty()) {
//					user.setEmail(email);
//				}
//				if (dob != null && !dob.trim().isEmpty()) {
//					try {
//						// Parse "19700101" into LocalDate
//						DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//						LocalDate parsedDob = LocalDate.parse(dob, inputFormatter);
//
//						// If your UserInfo entity dob is String → save formatted String
//						DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//						user.setDob(parsedDob.format(outputFormatter));
//
//						// If your UserInfo entity dob is LocalDate → just set parsedDob
//						// user.setDob(parsedDob);
//					} catch (Exception e) {
//						System.out.println("Invalid DOB format: " + dob);
//					}
//				}
//
//				// ✅ Gender (convert String → Integer)
//				if (gender != null && !gender.trim().isEmpty()) {
//					try {
//						user.setGender(Integer.parseInt(gender));
//					} catch (NumberFormatException e) {
//						System.out.println("Invalid gender value: " + gender);
//					}
//				}
//
//				// ✅ Pincode (convert String → Integer)
//				if (pincode != null && !pincode.trim().isEmpty()) {
//					try {
//						user.setResidentialPincode(Integer.parseInt(pincode));
//					} catch (NumberFormatException e) {
//						System.out.println("Invalid pincode value: " + pincode);
//					}
//				}
//
//				// ✅ Persist updates
//				userInfoRepository.save(user);
//			}
//
//			Map<String, Object> data = new HashMap<>();
//			data.put("score", score);
//			data.put("pan", pan);
//			data.put("dob", dob);
//			data.put("email", email);
//			data.put("gender", gender);
//			data.put("pincode", pincode);
//
//			data.put("code", 1);// code 1 is for otp verified succesfully
//			data.put("message", "otp verified succesfully");
//
//			return data;
//
//		}
//
//		catch (Exception e) {
//			e.printStackTrace();
//			Map<String, Object> data = new HashMap<>();
//			data.put("code", -1);// code -1 stands for OTP failed
//			data.put("message", "OTP verification failed: " + e.getMessage());
//
//			return data;
//
//		}
//	}

}
