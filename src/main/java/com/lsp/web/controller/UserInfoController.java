package com.lsp.web.controller;

import java.util.Map;
import java.util.Objects;

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

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
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
        @GetMapping("/getmobile")
        public ResponseEntity<?> getUser(@RequestParam String Mobilenumber) {
            UserInfo user = userInfoRepository.findByMobileNumber(Mobilenumber.trim())
                .orElseThrow(() -> new RuntimeException("User not found for mobile: " + Mobilenumber));
            
            return ResponseEntity.ok(user);
        
    }

	
	
}	
	
