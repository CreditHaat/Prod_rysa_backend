package com.lsp.web.controller;

//import java.util.Map;
//import java.util.Objects;
//
////import org.apache.http.HttpStatus;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.lsp.web.util.ApiResponse;
//import com.lsp.web.util.StringUtil;
//import com.lsp.web.entity.UserInfo;
//import com.lsp.web.repository.UserInfoRepository;
//import com.lsp.web.ONDCService.UserInfoService;
//import com.lsp.web.dto.UserInfoDto;

import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.function.Supplier;

//import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lsp.web.util.ApiResponse;
import com.lsp.web.util.StringUtil;
import com.lsp.web.entity.UserInfo;
import com.lsp.web.repository.UserInfoRepository;
import com.lsp.web.ONDCService.UserInfoService;
import com.lsp.web.dto.*;


@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "*")
public class UserInfoController {

	@Autowired
	private UserInfoService userInfoService;

//	@PostMapping("/send")
//	public ResponseEntity<ApiResponse> sendOtp(@RequestBody Map<String, Object> payload) {
//	    JSONObject input = new JSONObject(payload);
//	    
//	    // Call the service
//	    ApiResponse response = userInfoService.processOtp(input);
//
//	    // Send ApiResponse to frontend with stgOneHitId and stgTwoHitId
//	    return ResponseEntity.status(response.getCode()).body(response);
//	}
	@CrossOrigin(origins = "*")
	@PostMapping("/send")
	public ResponseEntity<ApiResponse> sendOtp(@RequestBody Map<String, Object> payload) {
	    JSONObject input = new JSONObject(payload);
	    
	    // Step 1: Save or update the user
//	    try {
//	        userInfoService.saveUser(input);  // This will handle new or existing users
//	    } catch (Exception e) {
//	        ApiResponse error = new ApiResponse();
//	        error.setCode(500);
//	        error.setMsg("User saving failed: " + e.getMessage());
//	        return ResponseEntity.status(500).body(error);
//	    }

	    // Step 2: Call OTP process
	    ApiResponse response = userInfoService.processOtp(input);

	    // Step 3: Return OTP response
	    return ResponseEntity.status(response.getCode()).body(response);
	}
	
	
	@CrossOrigin(origins = "*")
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, Object> payload) {
    	// Extract values safely using Objects.toString (null-safe)
        String mobile = Objects.toString(payload.get("Mobilenumber"), null);
        String otp = Objects.toString(payload.get("OTP"), null);
        String stgOneHitId = Objects.toString(payload.get("stgOneHitId"), null);
        String stgTwoHitId = Objects.toString(payload.get("stgTwoHitId"), null);
        String otpGenerationStatus = Objects.toString(payload.get("otpGenerationStatusfromexperian"), null);
        String agent_id = Objects.toString(payload.get("agent_id"), null);
        String agent = Objects.toString(payload.get("agent"), null);
//        String agent_id = "1246569";
//        String agent = "BTI";
        
        // Validate using StringUtil
        if (StringUtil.nullOrEmpty(mobile) || StringUtil.nullOrEmpty(otp) || StringUtil.nullOrEmpty(stgOneHitId)  || StringUtil.nullOrEmpty(stgTwoHitId) || StringUtil.nullOrEmpty(otpGenerationStatus) ||StringUtil.nullOrEmpty(agent_id) || StringUtil.nullOrEmpty(agent)) {
        	throw new IllegalArgumentException("Mobile, OTP, Both Stage IDs and OTP Generation Status are required");
        }
        
        JSONObject input = new JSONObject(payload);
        try {
        	Map<?, ?> response = userInfoService.verifyOtp(input);
            return ResponseEntity.ok(response);
        }catch(Exception e) {
        	return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        
//        return ResponseEntity.status(response.hashCode()).body(response);
    }
    


        @Autowired
        private UserInfoRepository userInfoRepository;

        // ✅ Add this method below
        @CrossOrigin(origins = "*")
        @GetMapping("/getmobile")
        public ResponseEntity<?> getUser(@RequestParam String Mobilenumber) {
            UserInfo user = userInfoRepository.findByMobileNumber(Mobilenumber.trim())
                .orElseThrow(() -> new RuntimeException("User not found for mobile: " + Mobilenumber));
            
            return ResponseEntity.ok(user);
        
    }
        
        //code by yogita
        
        @CrossOrigin(origins = "*")
        @PostMapping("/page1")
        public ResponseEntity<Map<String, Object>> savePage1(
            @RequestBody UserInfoDto dto,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String dsa,
            @RequestParam(required = false) String sub_dsa,

            @RequestParam(required = false) String channel
        ) {
            
            if (dto.getAgent() == null && source != null) {
                dto.setAgent(source);
            }
            
            if(dto.getAgentId() == null && dsa != null) {
                dto.setAgentId(Integer.parseInt(dsa));
            }
          if (dto.getSubAgent() == null && sub_dsa != null) {
          dto.setSubAgent(sub_dsa);
      }
            
            if (dto.getChannel() == null && channel != null) {
                dto.setChannel(channel);
            }
            
            // Campaign already comes from frontend with complete query string
            // No need to process it here
            
            return wrapResponse(() -> userInfoService.saveOrUpdatePage1(dto));
        }
//        @PostMapping("/page1")
//        public ResponseEntity<Map<String, Object>> savePage1(
//                @RequestBody UserInfoDto dto,
//                @RequestParam(required = false) String source,
//                @RequestParam(required = false) String dsa,
//
//                @RequestParam(required = false) String sub_dsa,
//                @RequestParam(required = false) String campaign) {
//
//            // if frontend provides that thin in query pararm then we will set here 
//            if (dto.getAgent() == null && source != null) {
//                dto.setAgent(source);
//            }
//            if(dto.getAgentId()==null && dsa != null) {
//            	dto.setAgentId(Integer.parseInt(dsa));            
//            }
//            if (dto.getSubAgent() == null && sub_dsa != null) {
//                dto.setSubAgent(sub_dsa);
//            }
//            if (dto.getCampaign() == null && campaign != null) {
//                dto.setCampaign(campaign);
//            }
//
//            return wrapResponse(() -> userInfoService.saveOrUpdatePage1(dto));
//        }

        @CrossOrigin(origins = "*")
    	@PostMapping("/sendOtpArysefin")
    	public ResponseEntity<ApiResponse> aryseOtp(@RequestBody Map<String, Object> payload) {
    	    JSONObject input = new JSONObject(payload);

    	    // Step 2: Call OTP process
    	    ApiResponse response = userInfoService.aryseOtp(input);

    	    // Step 3: Return OTP response
    	    return ResponseEntity.status(response.getCode()).body(response);
    	}
    	
//        @PostMapping("/verifyArysefinOtp")
//        public ResponseEntity<?> verifyOtpasysefin(@RequestBody Map<String, Object> payload) {
//        	// Extract values safely using Objects.toString (null-safe)
//            String mobile = Objects.toString(payload.get("Mobilenumber"), null);
//            String otp = Objects.toString(payload.get("OTP"), null);           
//            // Validate using StringUtil
//            JSONObject input = new JSONObject(payload);
//            try {
//            	Map<?, ?> response = userInfoService.verifyOtp(input);
//                return ResponseEntity.ok(response);
//            }catch(Exception e) {
//            	return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//            }
//            
////            return ResponseEntity.status(response.hashCode()).body(response);
//        }
//        
        
    	@CrossOrigin(origins = "*")
    	@PostMapping("/verifyArysefinOtp")
    	public ResponseEntity<ApiResponse> aryseVerifyOtp(@RequestBody Map<String, Object> payload) {
    	    // Extract values safely
    	    String mobile = Objects.toString(payload.get("Mobilenumber"), "");
    	    String otp = Objects.toString(payload.get("OTP"), "");  // Note: Using "OTP" as per your controller

    	    if (mobile.isEmpty() || otp.isEmpty()) {
    	        ApiResponse error = new ApiResponse();
    	        error.setCode(400);
    	        error.setMsg("Mobilenumber and OTP are required");
    	        return ResponseEntity.badRequest().body(error);
    	    }

    	    try {
    	        JSONObject input = new JSONObject();
    	        input.put("Mobilenumber", mobile);
    	        input.put("otp", otp);

    	        ApiResponse response = userInfoService.aryseVerifyOtp(input);
    	        
    	        // Return appropriate HTTP status based on the response code
    	        if (response.getCode() == 0) {
    	            return ResponseEntity.status(HttpStatus.OK).body(response);
    	        } else {
    	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    	        }

    	    } catch (Exception e) {
    	        ApiResponse error = new ApiResponse();
    	        error.setCode(500);
    	        error.setMsg("Error verifying OTP: " + e.getMessage());
    	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    	    }
    	}
        
        // Page 1
//        @PostMapping("/page1")
//        public ResponseEntity<Map<String, Object>> savePage1(@RequestBody UserInfoDto dto) {
//            return wrapResponse(() -> userInfoService.saveOrUpdatePage1(dto));
//        }

        // Page 2 with validation
    	@CrossOrigin(origins = "*")
        @PostMapping("/page2")
        public ResponseEntity<Map<String, Object>> savePage2(@RequestBody UserInfoDto dto) {
            return wrapResponse(() -> userInfoService.saveOrUpdatePage2(dto));
        }

        // Page 3
        @CrossOrigin(origins = "*")
        @PostMapping("/page3")
        public ResponseEntity<Map<String, Object>> savePage3(@RequestBody UserInfoDto dto) {
            return wrapResponse(() -> userInfoService.saveOrUpdatePage3(dto));
        }

        // Page 4
        @CrossOrigin(origins = "*")
        @PostMapping("/page4")
        public ResponseEntity<Map<String, Object>> savePage4(@RequestBody UserInfoDto dto) {
            return wrapResponse(() -> userInfoService.saveOrUpdatePage4(dto));
        }

        //  Wrapper method to return JSON always
        private ResponseEntity<Map<String, Object>> wrapResponse(Supplier<UserInfoDto> supplier) {
            Map<String, Object> response = new HashMap<>();
            try {
                UserInfoDto dto = supplier.get();
                response.put("status", "APPROVED");
                response.put("data", dto);
            } catch (RuntimeException e) {
                response.put("status", "REJECTED");
                response.put("reason", e.getMessage());
            }
            return ResponseEntity.ok(response);
        }
        
        @CrossOrigin(origins = "*")
        @PostMapping("/sendJourneyOTP")
        public ResponseEntity<ApiResponse> sendOtpJourney(@RequestBody Map<String, Object> payload) {
            try {
                // Map payload to DTO
                UserInfoDto dto = new UserInfoDto();
                dto.setMobileNumber(Objects.toString(payload.get("Mobilenumber"), ""));
                dto.setFirstName(Objects.toString(payload.get("firstname"), ""));
                dto.setLastName(Objects.toString(payload.get("lastname"), ""));
                dto.setEmail(Objects.toString(payload.get("email"), ""));
                dto.setPan(Objects.toString(payload.get("pan"), ""));

                // Call the API
                JSONObject apiResponse = userInfoService.callExperianAryseFinApi(dto);

                // Convert JSONObject to ApiResponse
                ApiResponse response = new ApiResponse();
                response.setCode(200); // or parse from apiResponse if it has status
                response.setMsg(apiResponse.toString());

                return ResponseEntity.status(response.getCode()).body(response);

            } catch (Exception e) {
                ApiResponse error = new ApiResponse();
                error.setCode(500);
                error.setMsg("OTP sending failed: " + e.getMessage());
                return ResponseEntity.status(500).body(error);
            }
        }

	
	
}	
	
