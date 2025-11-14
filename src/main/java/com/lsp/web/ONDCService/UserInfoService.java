package com.lsp.web.ONDCService;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.lsp.web.entity.MIS;
import com.lsp.web.entity.UserEngagementLog;
import com.lsp.web.entity.UserInfo;
import com.lsp.web.Exception.InvalidInputException;
import com.lsp.web.Exception.OtpValidationException;
import com.lsp.web.repository.JourneyLogRepository;
import com.lsp.web.repository.LoggerRepository;
import com.lsp.web.repository.MISRepository;
import com.lsp.web.repository.MasterCityStateRepository;
import com.lsp.web.repository.UserEngagementLogRepository;
import com.lsp.web.repository.UserInfoRepository;
import com.lsp.web.util.StringUtil;


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

	@Autowired
	private MasterCityStateRepository masterCityStateRepository;
	
	@Autowired 
	private MISRepository misRepository;
	
	@Autowired
	private UserEngagementLogRepository userEngagementLogRepository;



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
				if (!stgOneHitId.equalsIgnoreCase("000000") && !stgTwoHitId.equals("000000")) {
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
		} catch (Exception e) {
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

		} catch (Exception e) {

			Map<String, Object> data = new HashMap<>();
			data.put("code", 2);// code 2 is for otp verified succesfully but experian not found
			data.put("message", "otp verified but got some exception while checking INProfileResponse");

			String mobileNumber = input.optString("Mobilenumber", null);
			saveJourneyLogAndLogger(mobileNumber, "verifyOtp", 1, input, e.getMessage());

			return data;
		}

		try {
			// if inprofile is not null then we will get the values from inprofile
			JSONObject json = new JSONObject(jsonResponse);
			JSONObject inProfileResponse = json.optJSONObject("INProfileResponse");
			String mobileNumber = input.optString("Mobilenumber", null);
			Map<String, Object> data = processVerifyOtpBureauData(mobileNumber, inProfileResponse);
			if (data == null) {
				data = new HashMap<>();
			}
			data.put("code", 1);// code 1 is for otp verified succesfully
			data.put("message", "otp verified succesfully");

			return data;

		} catch (Exception e) {

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

	public Map<String, Object> processVerifyOtpBureauData(String mobileNumber, JSONObject inProfileResponse) {

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

			// here we will write a code to save the bureauData before any
			// processing--------------

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
					// first we will set data into user table

					// Wrap the JSON in {"INProfileResponse": {...}}
					JSONObject wrappedResponse = new JSONObject();
					wrappedResponse.put("INProfileResponse", inProfileResponse);

					// here we are setting the bureau data at first beofre doing any process on
					// bureau data to avoid any exception
					userBureauDataService.saveOrUpdateBureauData(user, score, wrappedResponse.toString());

				}
			} catch (Exception e) {
				saveJourneyLogAndLogger(mobileNumber, "processVerifyOtpBureauData", 1,
						"error while saving BureauData data before processing fields", e.getMessage());
			}

			// -------------------------------------------------------------------------------

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

			// ================================================this code is used to save
			// bereauresponse in table======

//						String mobileNumber = input.optString("Mobilenumber", null);
//						Optional<UserInfo> userInfoOpt = userInfoRepository.findByMobileNumber(mobileNumber);//comment by tejas

			if (userInfoOpt.isPresent() && inProfileResponse != null) {
				UserInfo user = userInfoOpt.get();

				// Wrap the JSON in {"INProfileResponse": {...}}
				// commented by tejas
//							JSONObject wrappedResponse = new JSONObject();
//							wrappedResponse.put("INProfileResponse", inProfileResponse);
//
//							userBureauDataService.saveOrUpdateBureauData(user, score, wrappedResponse.toString());
				// comment by tejas end here

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

		} catch (Exception e) {
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

	public void saveJourneyLogAndLogger(String mobileNumber, String requestId, int stage, Object requestPayload,
			Object responsePayload) {
		try {
			JourneyLog journeyLog = new JourneyLog();
			journeyLog.setPlatformId("A");
			journeyLog.setStage(stage);
			journeyLog.setRequestId(requestId);

			if (mobileNumber != null) {
				Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobileNumber);
				if (optionalUser.isPresent()) {
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

		} catch (Exception e) {

		}
	}

	// code by yogita//////////////////////////////////
	// ==============================================code by
	// yogita==============================
	// ================= Page 1 =================
//		public UserInfoDto saveOrUpdatePage1(UserInfoDto dto) {
//		    Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(dto.getMobileNumber().trim());
//		    UserInfo user = optionalUser.orElse(new UserInfo());
	//
//		    // Page1 fields
//		    user.setMobileNumber(dto.getMobileNumber());
	//
//		    // DSA/SubDSA/Campaign set here
//		    if (dto.getAgent() != null) user.setAgent(dto.getAgent());//agent = source
//		    if (dto.getAgentId() != null) user.setAgentId(dto.getAgentId());//agent id = dsa
//		    if (dto.getSubAgent() != null) user.setSub_agent(dto.getSubAgent());//subagent = sub dsa
//		    if (dto.getCampaign() != null) user.setCampaign(dto.getCampaign());//agter question mark will will store all url
	//
//		    userInfoRepository.save(user);
//		    return buildDto(user);
//		}
//	public UserInfoDto saveOrUpdatePage1(UserInfoDto dto) {
//
//		Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(dto.getMobileNumber().trim());
//		UserInfo user = optionalUser.orElse(new UserInfo());
//
//		// Page1 fields
//		user.setMobileNumber(dto.getMobileNumber());
//
//		// DSA/SubDSA/Campaign set here
//		if (dto.getAgent() != null)
//			user.setAgent(dto.getAgent());// agent = source
//		if (dto.getAgentId() != null)
//			user.setAgentId(dto.getAgentId());// agent id = dsa
//		if (dto.getSubAgent() != null)
//			user.setSub_agent(dto.getSubAgent());// subagent = sub dsa
//		if (dto.getCampaign() != null)
//			user.setCampaign(dto.getCampaign());// after question mark will store all url
//		if (dto.getChannel() != null)
//			user.setChannel(dto.getChannel());// channel = channel
//
//		// fix: set active for new users
////		    if (user.getId() == null) { // new record
//		user.setActive(0); // or false, depending on business logic
////		    }
//
//		userInfoRepository.save(user);
//		return buildDto(user);
//	}
	
	public UserInfoDto saveOrUpdatePage1(UserInfoDto dto) {
		
		  // ------------------ Prepare JSONObject for Reattribution ------------------
	    try {
	        JSONObject reattrDetails = new JSONObject();
	        reattrDetails.put("mobileNumber", dto.getMobileNumber());
	        reattrDetails.put("userId", ""); // Empty, reattribution will handle
	        reattrDetails.put("channel", dto.getChannel() != null ? dto.getChannel() : "");
	        reattrDetails.put("dsa", dto.getAgentId() != null ? dto.getAgentId().toString() : "");
	        reattrDetails.put("sub_source", dto.getSubAgent() != null ? dto.getSubAgent() : "");
	        reattrDetails.put("sub_dsa", dto.getSubAgent() != null ? dto.getSubAgent() : "");
	        reattrDetails.put("query", dto.getCampaign() != null ? dto.getCampaign() : "");
	        reattrDetails.put("source", dto.getAgent() != null ? dto.getAgent() : "");
	        reattrDetails.put("clickid", dto.getClickId() != null ? dto.getClickId() : "");

	        // ------------------ Call Reattribution Function ------------------
	        reattribution_func(reattrDetails);

	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }


		Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(dto.getMobileNumber().trim());
		UserInfo user = optionalUser.orElse(new UserInfo());
			
//------------------ Handle MIS ----------------------------------------------------------
		LocalDateTime now = LocalDateTime.now();
		List<MIS> misList = misRepository.findAllByMobileNumberOrderByCreateTimeDesc(dto.getMobileNumber());

		MIS mis = null;

		if (!misList.isEmpty()) {
		    MIS latestMIS = null;
		    for (MIS m : misList) {
		        if (!"cancel".equalsIgnoreCase(m.getCancelFlag())) {
		            latestMIS = m;
		            break;
		        }
		    }

		    if (latestMIS != null && latestMIS.getCreateTime() != null && 
		        latestMIS.getCreateTime().isAfter(now.minusDays(7))) {
		        mis = latestMIS; // update same MIS
		        mis.setClickId(user.getClickId());//===>important :take latest clickid from userinfo
		        mis.setAgentId(user.getAgentId());

		    } else {
		        // Expire all old MIS
		        for (MIS old : misList) old.setCancelFlag("cancel");
		        misRepository.saveAll(misList);
		        

		        // Create new MIS
		        mis = new MIS();
		        mis.setJourneyFlag("Not Completed");
		        mis.setMobileNumber(dto.getMobileNumber());
		        mis.setUser(user);
		        mis.setCustomUserId("a-" + user.getId());
		        mis.setClickId(user.getClickId());
			    mis.setAgentId(user.getAgentId());

		    }
		} else {
		    // No MIS exists → create new
		    mis = new MIS();
		    mis.setJourneyFlag("Not Completed");
		    mis.setMobileNumber(dto.getMobileNumber());
		    mis.setUser(user);
		    mis.setCustomUserId("a-" + user.getId());
	        mis.setClickId(user.getClickId());
			mis.setAgentId(user.getAgentId());


		}

		misRepository.save(mis);


	    return buildDto(user);

	}
	// =================generate
	// otp=====================================================

	// Process OTP for the user
	public ApiResponse aryseOtp(JSONObject input) {
		try {

			String url = "https://loan.credithaat.com/api/otparyse";
//		                String url = "http://localhost/d2cforinternal/otpgeneration";

			// Create request body for OTP API
			JSONObject requestBody = new JSONObject();
			requestBody.put("phone", input.getString("Mobilenumber"));

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
			// Create response
			ApiResponse response = new ApiResponse();

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

	public ApiResponse aryseVerifyOtp(JSONObject input) {
		try {
			String url = "https://loan.credithaat.com/api/otparysevalidate"; // third party verify OTP api url

			// String url = "http://localhost/api/otparysevalidate"; // third party verify
			// OTP api url

			// Extract mobile and OTP from input
			String mobile = input.getString("Mobilenumber");
			String otp = input.getString("otp");

			JSONObject requestBody = new JSONObject();
			requestBody.put("phone", mobile);
			requestBody.put("otp", otp);
			requestBody.put("userId", ""); // Empty string - API will handle it

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("token", "Y3JlZGl0aGFhdHRlc3RzZXJ2ZXI=");

			HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> jsonResponse = restTemplate.postForEntity(url, request, String.class);

			JSONObject responseJson = new JSONObject(jsonResponse.getBody());

			ApiResponse response = new ApiResponse();

			// Handle the specific response codes from third-party API
			int code = responseJson.getInt("code");
			String message = responseJson.optString("msg", "Unknown response");

			response.setCode(code);

			switch (code) {
			case 0:
				response.setMsg("OTP verified successfully");
				// You can also extract token and uid if needed
				JSONObject obj = responseJson.optJSONObject("obj");
				if (obj != null) {
					// Store token and uid for future use if needed
					String token = obj.optString("token");
					String uid = obj.optString("uid");
					// You might want to return this data or store it
				}
				break;
			case -1:
				response.setMsg("Invalid OTP");
				break;
			case -2:
				response.setMsg("OTP expired");
				break;
			default:
				response.setMsg(message);
				break;
			}

			return response;

		} catch (Exception e) {
			ApiResponse error = new ApiResponse();
			error.setCode(500);
			error.setMsg("Error verifying OTP: " + e.getMessage());
			return error;
		}
	}

	// ====================page
	// 2================================================================
	public UserInfoDto saveOrUpdatePage2(UserInfoDto dto) {
		UserInfo user = userInfoRepository.findByMobileNumber(dto.getMobileNumber().trim())
				.orElseThrow(() -> new RuntimeException("Mobile not found"));

		// Update fields
		if (dto.getResidentialPincode() != null)
			user.setResidentialPincode(dto.getResidentialPincode());
		if (dto.getAddress() != null)
			user.setAddress(dto.getAddress());
		if (dto.getEmploymentType() != null)
			user.setEmploymentType(dto.getEmploymentType());
		if (dto.getPaymentType() != null)
			user.setPaymentType(dto.getPaymentType());
		if (dto.getMonthlyIncome() != null)
			user.setMonthlyIncome(dto.getMonthlyIncome());

		// FIRST: Always save the data
		userInfoRepository.save(user);

		// THEN: Check validation and throw exception if needed
		boolean validPincode = masterCityStateRepository.findByPincode(user.getResidentialPincode()).isPresent();
		boolean validIncome = (user.getMonthlyIncome() != null && user.getMonthlyIncome() >= 20000);

		if (!validPincode || !validIncome) {
			// Data already saved above, now throw exception for rejection response
			throw new RuntimeException("Invalid pincode or income below 20k");
		}

		return buildDto(user);
	}

//		public ApiResponse aryseVerifyOtp(JSONObject input) {
//		    try {
//		        String url = "http://localhost/api/otparysevalidate"; // third party verify OTP api url
	//
//		        JSONObject requestBody = new JSONObject();
//		        requestBody.put("phone", input.getString("Mobilenumber"));
//		        requestBody.put("otp", input.getString("otp"));
	//
//		        HttpHeaders headers = new HttpHeaders();
//		        headers.setContentType(MediaType.APPLICATION_JSON);
//		        headers.set("token", "Y3JlZGl0aGFhdHRlc3RzZXJ2ZXI=");
	//
//		        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
	//
//		        RestTemplate restTemplate = new RestTemplate();
//		        ResponseEntity<String> jsonResponse = restTemplate.postForEntity(url, request, String.class);
	//
//		        JSONObject responseJson = new JSONObject(jsonResponse.getBody());
	//
//		        ApiResponse response = new ApiResponse();
//		        // ✅ use new style
//		        response.setCode(jsonResponse.getStatusCode().value());
//		        response.setMsg(responseJson.optString("message", "OTP verification status"));
//		        return response;
	//
//		    } catch (Exception e) {
//		        ApiResponse error = new ApiResponse();
//		        error.setCode(500);
//		        error.setMsg("Error verifying OTP: " + e.getMessage());
//		        return error;
//		    }
//		}
	//

	// ================= Page 2
	// ===============================================================================

//	    public UserInfoDto saveOrUpdatePage2(UserInfoDto dto) {
//	        UserInfo user = userInfoRepository.findByMobileNumber(dto.getMobileNumber().trim())
//	                .orElseThrow(() -> new RuntimeException("Mobile not found"));
	//
//	        if (dto.getResidentialPincode() != null) user.setResidentialPincode(dto.getResidentialPincode());
//	        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
//	        if (dto.getEmploymentType() != null) user.setEmploymentType(dto.getEmploymentType());
//	        if (dto.getPaymentType() != null) user.setPaymentType(dto.getPaymentType());
//	        if (dto.getMonthlyIncome() != null) user.setMonthlyIncome(dto.getMonthlyIncome());
	//
//	        //  Validation logic
//	        boolean validPincode = masterCityStateRepository
//	                .findByPincode(user.getResidentialPincode())
//	                .isPresent();
	//
//	        boolean validIncome = (user.getMonthlyIncome() != null && user.getMonthlyIncome() >= 20000);
	//
//	        if (!validPincode || !validIncome) {
//	            throw new RuntimeException("Rejected: Invalid pincode or income below 20k");
//	        }
	//
//	        userInfoRepository.save(user);
//	        return buildDto(user);
//	    }

	// ================= Page 3
	// ===========================================================
	public UserInfoDto saveOrUpdatePage3(UserInfoDto dto) {
		UserInfo user = userInfoRepository.findByMobileNumber(dto.getMobileNumber().trim())
				.orElseThrow(() -> new RuntimeException("Mobile not found"));

		// Full Name from frontend
		String fullName = dto.getFirstName();
		if (fullName != null && !fullName.trim().isEmpty()) {
			fullName = fullName.trim();

			// ✅ Save full name directly in panName
			user.setPanName(fullName);

			// ✅ Split full name into first, last, and father name
			String[] parts = fullName.split("\\s+");
			user.setFirstName(parts[0]); // first word as first name

			if (parts.length == 2) {
				user.setLastName(parts[1]);
				user.setFatherName(null);
			} else if (parts.length > 2) {
				user.setLastName(parts[parts.length - 1]);

				StringBuilder middle = new StringBuilder();
				for (int i = 1; i < parts.length - 1; i++) {
					middle.append(parts[i]);
					if (i < parts.length - 2)
						middle.append(" ");
				}
				user.setFatherName(middle.toString());
			}
		}

		// Other optional details
		if (dto.getPan() != null)
			user.setPan(dto.getPan());
		if (dto.getEmail() != null)
			user.setEmail(dto.getEmail());
		if (dto.getGender() != null)
			user.setGender(dto.getGender());
		// converted date here dd-mm-yyyy to yyyy-mm-dd format
		if (dto.getDob() != null && !dto.getDob().isEmpty()) {
			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			LocalDate parsedDate = LocalDate.parse(dto.getDob(), inputFormatter);
			String formattedDate = parsedDate.format(outputFormatter);

			user.setDob(formattedDate); // assuming dob is String in DB
		}

		// Save to DB
		userInfoRepository.save(user);
		return buildDto(user);
	}

	// ================= Page 4 =========================================
	public UserInfoDto saveOrUpdatePage4(UserInfoDto dto) {
		System.out.println("Incoming Page4 request for mobile: " + dto.getMobileNumber());
		System.out.println("CompanyName: " + dto.getCompanyName());
		System.out.println("WorkEmail: " + dto.getWorkEmail());
		System.out.println("WorkPincode: " + dto.getWorkPincode());

		UserInfo user = userInfoRepository.findByMobileNumber(dto.getMobileNumber().trim())
				.orElseThrow(() -> new RuntimeException("Mobile not found"));

		if (dto.getCompanyName() != null)
			user.setCompanyName(dto.getCompanyName());
		if (dto.getWorkEmail() != null)
			user.setWorkEmail(dto.getWorkEmail());
		if (dto.getWorkPincode() != null)
			user.setWorkPincode(dto.getWorkPincode());
		if (dto.getFatherName() != null && !dto.getFatherName().trim().isEmpty()) {
	        String fatherInput = dto.getFatherName().trim();
	        String[] fatherParts = fatherInput.split("\\s+");
	        user.setFatherName(fatherParts[0]);
	    }
		if (dto.getMaritalStatus() != null)
			user.setMaritalStatus(dto.getMaritalStatus());
		if (dto.getMaritalStatus() != null && dto.getMaritalStatus() == 1 && dto.getSpouseName() != null) {
		    user.setSpouseName(dto.getSpouseName());
		} else {
		    user.setSpouseName(null);
		}

		userInfoRepository.save(user);
		//// ===================== Update MIS =====================
//      MIS mis = misRepository.findByMobileNumber(dto.getMobileNumber())
//              .orElseThrow(() -> new RuntimeException("MIS entry not found"));
//
//      // Set journeyFlag as Completed after page4
//      mis.setJourneyFlag("Completed");
//      
//   // ----------------- Set Prime Flag -----------------
//      Float salary = user.getMonthlyIncome();             // Page2
//      Integer profession = user.getEmploymentType();      // 1 = salaried
//      String creditProfileStr = user.getCreditProfile();  // Page3 score (String)
//   // Initialize upfront
//      String primeFlag = null; 
//
//      if (creditProfileStr != null) {
//          int creditProfile = Integer.parseInt(creditProfileStr);
//
//          if (creditProfile == 1000) {
//              primeFlag = "NTC";
//          } else if (creditProfile >= 720 && salary >= 35000 && profession == 1) {
//              primeFlag = "YES";
//          } else {
//              primeFlag = "NO";
//          }
//      }
//
//      // Save primeFlag in MIS
//      mis.setPrimeFlag(primeFlag); // Safe, always initialized

//
//      misRepository.save(mis);
//=====================================================================================
	    // ------------------ MIS Handling ------------------
		List<MIS> misList = misRepository.findAllByMobileNumberOrderByCreateTimeDesc(dto.getMobileNumber());
		MIS mis = null;

		for (MIS m : misList) {
		    if (!"cancel".equalsIgnoreCase(m.getCancelFlag())) {
		        mis = m;
		        break;
		    }
		}

		if (mis == null) {
		    throw new RuntimeException("No active MIS found for this user");
		}

		// Update Page 4 flags
		mis.setJourneyFlag("Completed");

		Float salary = user.getMonthlyIncome();
		Integer profession = user.getEmploymentType();
		String creditProfileStr = user.getCreditProfile();

		String primeFlag = null;
		if (creditProfileStr != null) {
		    int creditProfile = Integer.parseInt(creditProfileStr);
		    if (creditProfile == 1000) {
		        primeFlag = "NTC";
		    } else if (creditProfile >= 720 && salary >= 35000 && profession == 1) {
		        primeFlag = "YES";
		    } else {
		        primeFlag = "NO";
		    }
		}

		mis.setPrimeFlag(primeFlag);
		misRepository.save(mis);
		
	    return buildDto(user);
	}

		
//		return buildDto(user);
//	}
	
	

//	    public UserInfoDto saveOrUpdatePage4(UserInfoDto dto) {
//	        UserInfo user = userInfoRepository.findByMobileNumber(dto.getMobileNumber().trim())
//	                .orElseThrow(() -> new RuntimeException("Mobile not found"));
	//
//	        if (dto.getCompanyName() != null) user.setCompanyName(dto.getCompanyName());
//	        if (dto.getWorkEmail() != null) user.setWorkEmail(dto.getWorkEmail());
//	        if (dto.getWorkPincode() != null) user.setWorkPincode(dto.getWorkPincode());
	//
//	        userInfoRepository.save(user);
//	        return buildDto(user);
//	    }

	
	

//	    public UserInfoDto saveOrUpdatePage4(UserInfoDto dto) {
//	        UserInfo user = userInfoRepository.findByMobileNumber(dto.getMobileNumber().trim())
//	                .orElseThrow(() -> new RuntimeException("Mobile not found"));
	//
//	        if (dto.getCompanyName() != null) user.setCompanyName(dto.getCompanyName());
//	        if (dto.getWorkEmail() != null) user.setWorkEmail(dto.getWorkEmail());
//	        if (dto.getWorkPincode() != null) user.setWorkPincode(dto.getWorkPincode());
	//
//	        userInfoRepository.save(user);
//	        return buildDto(user);
//	    }

	// ================= Helper =================
	private UserInfoDto buildDto(UserInfo user) {
		UserInfoDto dto = new UserInfoDto();
		dto.setMobileNumber(user.getMobileNumber());
		dto.setPanName(user.getPanName());
		dto.setFirstName(user.getFirstName());
		dto.setFatherName(user.getFatherName());
		dto.setLastName(user.getLastName());
		dto.setEmail(user.getEmail());
		dto.setPan(user.getPan());
		dto.setAddress(user.getAddress());
		dto.setResidentialPincode(user.getResidentialPincode());
		dto.setEmploymentType(user.getEmploymentType());
		dto.setPaymentType(user.getPaymentType());
		dto.setMonthlyIncome(user.getMonthlyIncome());
		dto.setCompanyName(user.getCompanyName());
		dto.setWorkEmail(user.getWorkEmail());
		dto.setWorkPincode(user.getWorkPincode());
		dto.setGender(user.getGender());
		dto.setDob(user.getDob());

		dto.setAgent(user.getAgent());
		dto.setAgentId(user.getAgentId());
		dto.setSubAgent(user.getSub_agent());
		dto.setCampaign(user.getCampaign());
		return dto;
	}

	public JSONObject callExperianAryseFinApi(UserInfoDto dto) {
		try {
			String url = "https://loan.credithaat.com/d2c/bearuefetcharyse"; // AryseFin API
//	            String url = "http://localhost/d2c/bearuefetcharyse"; // AryseFin API

			// Build request payload
			JSONObject requestBody = new JSONObject();
			requestBody.put("Mobilenumber", dto.getMobileNumber());
			requestBody.put("Firstname", dto.getFirstName());
			requestBody.put("Lastname", dto.getLastName() != null ? dto.getLastName() : "");
			requestBody.put("Email", dto.getEmail() != null ? dto.getEmail() : "");
			requestBody.put("PAN", dto.getPan() != null ? dto.getPan() : "");
//	            requestBody.put("agent_id", 1246569);
//	            requestBody.put("agent", "BTI");
			requestBody.put("agent_id", 357046965);
			requestBody.put("agent", "arysefinlead");

			// Set headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("token", "Y3JlZGl0aGFhdHRlc3RzZXJ2ZXI=");

			HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

			// Make API call
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

			JSONObject respJson = new JSONObject(response.getBody());

			// ✅ CASE 1: Bureau data found
			if (respJson.has("INProfileResponse")) {
				JSONObject inProfileResponse = respJson.getJSONObject("INProfileResponse");

				// extract score
				JSONObject scoreJson = inProfileResponse.optJSONObject("SCORE");
				String score = scoreJson != null ? scoreJson.optString("BureauScore", null) : null;

				// fetch userinfo from db
				Optional<UserInfo> userInfoOpt = userInfoRepository.findByMobileNumber(dto.getMobileNumber());
				if (userInfoOpt.isPresent()) {
					UserInfo user = userInfoOpt.get();

					if (score != null && !score.trim().isEmpty()) {
						user.setCreditProfile(score);
					} else {
						user.setCreditProfile("1000"); // fallback if score missing
					}
					userInfoRepository.save(user);

					// Wrap response
					JSONObject wrappedResponse = new JSONObject();
					wrappedResponse.put("INProfileResponse", inProfileResponse);

					// save bureau data
					userBureauDataService.saveOrUpdateBureauData(user, score, wrappedResponse.toString());
				}
			}
			// ❌ CASE 2: Bureau data not found
//	            else if ("consumer record not found".equalsIgnoreCase(respJson.optString("errorString"))) {
			else {
				Optional<UserInfo> userInfoOpt = userInfoRepository.findByMobileNumber(dto.getMobileNumber());
				if (userInfoOpt.isPresent()) {
					UserInfo user = userInfoOpt.get();
					user.setCreditProfile("1000");
					userInfoRepository.save(user);
				}
			}

			return respJson;

		} catch (Exception e) {
			e.printStackTrace();
			try {
				JSONObject error = new JSONObject();
				error.put("error", "Experian API call failed: " + e.getMessage());
				return error;
			} catch (org.json.JSONException jsonEx) {
				jsonEx.printStackTrace();
				return null;
			}
		}
	}

	//////////////////////////////////////////////////
	
	  //====================Reattribution logic ====================
    
    public void reattribution_func(JSONObject reattribution_details) {
    	
   	 String mobileNumber = "";
   	 String userId = "";
   	 String channel = "";
   	 String dsa = "";
   	 String sub_source = "";
   	 String sub_dsa = "";
   	 String query = "";
   	 String source = "";
   	 String clickId="";
   	 
   	 mobileNumber = reattribution_details.optString("mobileNumber");
   	 userId = reattribution_details.optString("userId");
   	 channel = reattribution_details.optString("channel");
   	 dsa = reattribution_details.optString("dsa");
   	 sub_source = reattribution_details.optString("sub_source");
   	 sub_dsa = reattribution_details.optString("sub_dsa");
   	 query = reattribution_details.optString("query");
   	 source = reattribution_details.optString("source");
   	 clickId = reattribution_details.optString("clickid");

   	
   	boolean usernull = false;

   // boolean isNewUser = false;

    Optional<UserInfo> userOpt = userInfoRepository.findByMobileNumber(mobileNumber);
    UserInfo user = userOpt.orElse(null);

//   	UserInfoDto user = (UserInfoDto) dto.get(UserInfoDto.class, "mobilenumber='" + mobileNumber + "'");

    
    if(user==null) {
   		user = new UserInfo();
   		user.setMobileNumber(mobileNumber);
   		user.setRegisterTime(LocalDateTime.now());       		
   		user.setLastAttributionTime(LocalDateTime.now());
   		user.setActive(0);
   		//user.setRegTime(new Date());
   		//user.setLast_attribution_time(new Date());
   		
   		try {
   			//user.setAgentUserId(Integer.parseInt(dsa));
   			user.setAgentId(Integer.parseInt(dsa));
				user.setAgent(source);
				user.setClickId(clickId);  // if you want to store it in UserInfo

				//user.setSubagentUserId(Integer.parseInt(sub_dsa));
				user.setSubAgentId(Integer.parseInt(sub_dsa));

		
				user.setSub_agent(sub_source);
   		}catch(Exception ex) {}
   		
   		usernull = true;
   		//user.setChannel("Credithaat");
   		user.setChannel("AryseFin");
   	   	//user.setChannel0("Cred Care");
   		user.setClickId(clickId);
   	   	user.setWebSource(source);
   	   	user.setCampaign(query);
   	}
   	
//   	try {
//   	if(StringUtil.notEmpty(userId)) {
//   	user.setUserId(userId);
//   	}
//   	}catch(Exception ex) {}
//   	
   	
   	if(user!=null && usernull==false) {
   		
   		LocalDateTime last_attributio_time_check = null;
   		
   		try {
   			last_attributio_time_check = user.getLastAttributionTime();
   		}catch(Exception ex) {}
   		
   		
   		if(last_attributio_time_check!=null) {
   		LocalDateTime last_attribution_time = user.getLastAttributionTime();
   		//Date currentDate = new Date();
   		LocalDateTime currentDate = LocalDateTime.now();
//   		long differenceInMilliseconds = ChronoUnit.DAYS.between(last_attribution_time, currentDate);
//   		
//   		//long differenceInMilliseconds = currentDate.getTime() - last_attribution_time.getTime();
//           
//           // Convert milliseconds to days
//           long differenceInDays = differenceInMilliseconds / (1000 * 60 * 60 * 24);
//     
   		long differenceInDays = ChronoUnit.DAYS.between(last_attribution_time, currentDate);

           
           if(differenceInDays <= 60) {
        	   UserEngagementLog user_engagement_log = new UserEngagementLog();
				user_engagement_log.setMobileNumber(mobileNumber);
				user_engagement_log.setUser(user);
				user_engagement_log.setAgent(user.getAgent());
				user_engagement_log.setLastAttributionTime(last_attributio_time_check);
				user_engagement_log.setSub_agent(source);
				user_engagement_log.setClickId(clickId);
				try {
	   				//user_engagement_log.setSub_agent(sub_source);
	   				user_engagement_log.setAgentId(Integer.parseInt(dsa));
					
				//user_engagement_log.setAgentUserId(Integer.parseInt(dsa));
				}catch(Exception ex) {}
				user_engagement_log.setCampaign(query);
				user_engagement_log.setRegisterTime(LocalDateTime.now());

//				user_engagement_log.setRegTime(new Date());
				user_engagement_log.setWebSource(source);
				user_engagement_log.setClickId(clickId);

				//UserEngagementLogRepository.saveOrUpdate(user_engagement_log);
	   			userEngagementLogRepository.save(user_engagement_log);

           }
           else if(differenceInDays > 60) {
   			if (StringUtil.notEmpty(dsa)) {
//   				Agent agent = (Agent) dao.get(Agent.class, "crmUser='" + dsa + "'");
//   				if (agent != null && agent.getCrmUser() != null) {
   					user.setAgentId(Integer.parseInt(dsa));
   					user.setAgent(source);
   					user.setClickId(clickId);//seted click id also after 60 days it will be change in userinfo also
   	    			user.setLastAttributionTime(LocalDateTime.now());
//   				}
   			  }
   			
   			if (StringUtil.notEmpty(sub_dsa)) {
//					SubAgent subagent = (SubAgent) dao.get(SubAgent.class, "crmUser='" + sub_dsa + "'");
//					if (subagent != null && subagent.getCrmUser() != null) {
						user.setSubAgentId(Integer.parseInt(sub_dsa));
						user.setSub_agent(sub_source);
//					}
				  }
   			
   			UserEngagementLog user_engagement_log = new UserEngagementLog();
				user_engagement_log.setMobileNumber(mobileNumber);
				user_engagement_log.setUser(user);
				user_engagement_log.setLastAttributionTime(LocalDateTime.now());
				user_engagement_log.setAgent(source);
				user_engagement_log.setClickId(clickId);

				try {
				user_engagement_log.setAgentId(Integer.parseInt(dsa));
				}catch(Exception ex) {}
				user_engagement_log.setCampaign(query);
				user_engagement_log.setRegisterTime(LocalDateTime.now());
				user_engagement_log.setWebSource(source);
				user_engagement_log.setClickId(clickId);

//				dao.saveOrUpdate(user_engagement_log);
	   			userEngagementLogRepository.save(user_engagement_log);

				
				user.setLastAttributionTime(LocalDateTime.now());
           }
   		}else {
   			try {
//   		        Calendar calendar = Calendar.getInstance();
//   		        calendar.setTime(new Date());
//   		        calendar.add(Calendar.DAY_OF_YEAR,-45);
//   		        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//   		        UserInfo agentchecktime = (UserInfo)dao.get(UserInfo.class,"mobilenumber='"+mobileNumber+"' and createTime>'"+format.format(calendar.getTime())+"'");
//   				
   		  // New code using LocalDateTime
   		     LocalDateTime date60DaysAgo = LocalDateTime.now().minusDays(60);
   		     Optional<UserInfo> agentCheckTimeOpt = userInfoRepository.findByMobileNumberAndCreateTimeAfter(
   		             mobileNumber, date60DaysAgo);
   		     UserInfo agentCheckTime = agentCheckTimeOpt.orElse(null);
   		        
   		        
   			if(StringUtil.nullOrEmpty(user.getAgent()) || agentCheckTime==null) {
   			if (StringUtil.notEmpty(dsa)) {
//   				Agent agent = (Agent) dao.get(Agent.class, "crmUser='" + dsa + "'");
//   				if (agent != null && agent.getCrmUser() != null) {
   					user.setAgentId(Integer.parseInt(dsa));
   					user.setAgent(source);
//   				}
   			  }
   			}
   			
   			try {
   			if (StringUtil.notEmpty(sub_dsa)) {
//   					SubAgent subagent = (SubAgent) dao.get(SubAgent.class, "crmUser='" + sub_dsa + "'");
//   					if (subagent != null && subagent.getCrmUser() != null) {
   						user.setSubAgentId(Integer.parseInt(sub_dsa));
   						user.setSub_agent(sub_source);
//   					}
   			   }
   			}catch(Exception ex) {}
   			
   			}catch(Exception ex){}
   		}
   	}
   	
		//dao.saveOrUpdate(user);
   	userInfoRepository.save(user);
		
		try {
			if(usernull==true) {
				UserEngagementLog user_engagement_log = new UserEngagementLog();
			user_engagement_log.setMobileNumber(mobileNumber);
			user_engagement_log.setUser(user);
			user_engagement_log.setLastAttributionTime(LocalDateTime.now());
			user_engagement_log.setAgent(source);
			user_engagement_log.setClickId(clickId);

			try {
			user_engagement_log.setAgentId(Integer.parseInt(dsa));
			}catch(Exception ex) {}
			user_engagement_log.setCampaign(query);
			user_engagement_log.setRegisterTime(LocalDateTime.now());
			user_engagement_log.setWebSource(source);
			user_engagement_log.setClickId(clickId);

//			dao.saveOrUpdate(user_engagement_log);
			userEngagementLogRepository.save(user_engagement_log);
			}
		}catch(Exception ex) {}
   }
	
	public JSONObject callDigitapAryseFinApi(UserInfoDto dto) {
	    try {
	        // ✅ Use your local Credithaat API endpoint
//	        String url = "http://localhost/uan";
	    	String url = "https://loan.credithaat.com/uan";

	        // ✅ Build full URL with query parameters
	        String fullUrl = url + "?pan=" + dto.getPan() + "&mobile=" + dto.getMobileNumber();

	        // ✅ Use RestTemplate GET call
	        RestTemplate restTemplate = new RestTemplate();
	        ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);

	        // ✅ Parse response into JSON
	        JSONObject respJson = new JSONObject(response.getBody());

	        return respJson;

	    } catch (Exception e) {
	        e.printStackTrace();
	        try {
	            JSONObject error = new JSONObject();
	            error.put("error", "Digitap API call failed: " + e.getMessage());
	            return error;
	        } catch (org.json.JSONException jsonEx) {
	            jsonEx.printStackTrace();
	            return null;
	        }
	    }
	}
	
	public JSONObject callDigitapPanAryseFinApi(UserInfoDto dto) {
	    try {
	        // ✅ Use your local Credithaat API endpoint
//	        String url = "http://localhost/pan";
	        String url = "https://loan.credithaat.com/pan";

	        // ✅ Build full URL with query parameters
//	        String fullUrl = url + "?pan=" + dto.getPan();
	        String fullUrl = url + "?pan=" + dto.getPan() + "&mobile=" + dto.getMobileNumber();

	        // ✅ Use RestTemplate GET call
	        RestTemplate restTemplate = new RestTemplate();
	        ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);

	        // ✅ Parse response into JSON
	        JSONObject respJson = new JSONObject(response.getBody());

	        return respJson;

	    } catch (Exception e) {
	        e.printStackTrace();
	        try {
	            JSONObject error = new JSONObject();
	            error.put("error", "Digitap API call failed: " + e.getMessage());
	            return error;
	        } catch (org.json.JSONException jsonEx) {
	            jsonEx.printStackTrace();
	            return null;
	        }
	    }
	}


}