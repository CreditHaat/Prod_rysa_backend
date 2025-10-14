package com.lsp.web.controller;

//import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
//import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsp.web.Exception.LoanNotFoundException;
import com.lsp.web.ONDCService.ConfirmService;
import com.lsp.web.ONDCService.DisbursedLoanService;
import com.lsp.web.ONDCService.FinvuService;
import com.lsp.web.ONDCService.InitService;
import com.lsp.web.ONDCService.LoanInstallmentsService;
import com.lsp.web.ONDCService.SearchService;
import com.lsp.web.ONDCService.SelectService;
import com.lsp.web.ONDCService.StatusService;
import com.lsp.web.ONDCService.UpdateService;
import com.lsp.web.dto.ConfirmRequestDTO;
import com.lsp.web.dto.DisbursedLoanDTO2;
import com.lsp.web.dto.ForeclosureUpdateRequestDTO;
import com.lsp.web.dto.FormLogRequest;
import com.lsp.web.dto.InitRequestDTO;
import com.lsp.web.dto.MissedPaymentUpdateRequestDTO;
import com.lsp.web.dto.ONDCFormDataDTO;
import com.lsp.web.dto.SearchRequestDTO;
import com.lsp.web.dto.SelectRequestDTO;
import com.lsp.web.dto.StatusRequestDTO;
import com.lsp.web.dto.UpdateFulfillmentRequestDTO;
import com.lsp.web.dto.UpdatePaymentRequestDTO;
import com.lsp.web.dto.UpdateRequestDTO;
import com.lsp.web.entity.Callback;
import com.lsp.web.entity.JourneyLog;
import com.lsp.web.entity.Master_City_State;
import com.lsp.web.entity.Product;
import com.lsp.web.entity.UserBureauData;
import com.lsp.web.entity.UserInfo;
import com.lsp.web.repository.CallbackRepository;
import com.lsp.web.repository.JourneyLogRepository;
import com.lsp.web.repository.MasterCityStateRepository;
import com.lsp.web.repository.ProductRepository;
import com.lsp.web.repository.UserBureauDataRepository;
import com.lsp.web.repository.UserInfoRepository;

import ondc.onboarding.utility.Routes;
import ondc.onboarding.utility.Utils;
///////////////////////

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin("*")
public class ONDCController extends Utils {

	@Autowired
	private SearchService searchService;
	@Autowired
	private SelectService selectService;
	@Autowired
	private InitService initService;
	@Autowired
	private ConfirmService confirmService;
	@Autowired
	private UpdateService updateService;
	@Autowired
	private StatusService statusService;

	@Autowired
	private DisbursedLoanService loanService; // Service where saveLoanFromOnConfirm is defined

	@Autowired
	private LoanInstallmentsService loanInstallmentService;
	
	@Autowired
	private MasterCityStateRepository masterCityStateRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private UserInfoRepository userInfoRepository;
	
	@Autowired
	private FinvuService finvuService;
	
	@Autowired
	private CallbackRepository callbackRepository;
	
	@Autowired
	private JourneyLogRepository journeyLogRepository;
	
	@Autowired
	private UserBureauDataRepository userBureauDataRepository;

	@GetMapping("/createTransactionId")
	public ResponseEntity<?> createId() {
		try {
			String transactionId = UUID.randomUUID().toString();
			return ResponseEntity.ok(transactionId);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to create transactionId", "details", e.getMessage()));
		}
	}

	// here we are taking the mobileNumber just to get UserInfo and save it in
	// journeyLog its not related to ONDC's search api
	@PostMapping("/search")
//	@RequestBody
	public ResponseEntity<?> triggerSearch(@RequestBody SearchRequestDTO searchRequestDTO) {
		return searchService.search(searchRequestDTO.getTransactionId(), searchRequestDTO.getMobileNumber(),
				searchRequestDTO.getStage());
	}

	@PostMapping("/select")
	public ResponseEntity<?> triggerSelect(@RequestBody SelectRequestDTO selectRequestDTO) {
		return selectService.select(selectRequestDTO.getTransactionId(), selectRequestDTO.getBppId(),
				selectRequestDTO.getBppUri(), selectRequestDTO.getProviderId(), selectRequestDTO.getItemId(),
				selectRequestDTO.getFormId(), selectRequestDTO.getSubmissionId(), selectRequestDTO.getStatus(),
				selectRequestDTO.getMobileNumber(), selectRequestDTO.getStage(), selectRequestDTO.getProductName(),
				selectRequestDTO.getLoanAmount(), selectRequestDTO.getVersion());
	}

	@PostMapping("/init")
	public ResponseEntity<?> callInit(@RequestBody InitRequestDTO request) {
		return initService.init(request.getTransactionId(), request.getBppId(), request.getBppUri(),
				request.getProviderId(), request.getItemId(), request.getFormId(), request.getSubmissionId(),
				request.getBankCode(), request.getAccountNumber(), request.getVpa(), request.getSettlementAmount(),
				request.getMobileNumber(), request.getStage(), request.getProductName(), request.getFormType(),
				request.getAccountname(), request.getAccountType(), request.getIFSC(), request.getVersion(),
				request.getInitAttempt(), request.getPaymentId());
	}

	@PostMapping("/confirm")
	public ResponseEntity<?> selectRequest(@RequestBody ConfirmRequestDTO request) {// to change this to
																					// ConfirmRequestDTO
		return confirmService.confirm(request.getTransactionId(), request.getBppId(), request.getBppUri(),
				request.getProviderId(), request.getItemId(), request.getFormId(), request.getSubmissionId(),
				request.getBankCode(), request.getAccountNumber(), request.getVpa(), request.getSettlementAmount(),
				request.getVersion(), request.getPaymentId(), request.getMobileNumber(), request.getStage(),
				request.getProductName());
	}

	@PostMapping("/update")
	public ResponseEntity<?> updateRequest(@RequestBody UpdateRequestDTO request) {
		return updateService.update(request.getTransactionId(), request.getBppId(), request.getBppUri(),
				request.getOrderId(), request.getFulfillmentState(), request.getVersion());
	}

	//This is to update the part payment
	@PostMapping("/update-payment")
	public ResponseEntity<?> updatePaymentRequest(@RequestBody UpdatePaymentRequestDTO request) {
		return updateService.updatePayment(request.getTransactionId(), request.getBppId(), request.getBppUri(),
				request.getOrderId(), request.getAmount(), request.getCurrency(), request.getVersion());
	}

	@PostMapping("/update-fulfillment")
	public ResponseEntity<?> updateFulfillmentRequest(@RequestBody UpdateFulfillmentRequestDTO request) {
		return updateService.updateFulfillment(request.getTransactionId(), request.getBppId(), request.getBppUri(),
				request.getOrderId(), request.getFulfillmentState(), request.getVersion());
	}

	@PostMapping("/status")
	public ResponseEntity<?> statusRequest(@RequestBody StatusRequestDTO request) {
		return statusService.status(request.getTransactionId(), request.getBppId(), request.getBppUri(),
				request.getRefId(), request.getVersion());
	}

	@PostMapping("/update-missed-emi")
	public ResponseEntity<?> updateMissedPayment(@RequestBody MissedPaymentUpdateRequestDTO request) {
		return updateService.updateMissedPayment(request.getTransactionId(), request.getBppId(), request.getBppUri(),
				request.getOrderId(), request.getPaymentLabel(), request.getVersion());
	}

	@PostMapping("/update-foreclosure")
	public ResponseEntity<?> updateForeclosure(@RequestBody ForeclosureUpdateRequestDTO request) {
		return updateService.updateForeclosurePayment(request.getTransactionId(), request.getBppId(),
				request.getBppUri(), request.getOrderId(), request.getPaymentLabel(), request.getVersion());
	}

//	 @PostMapping("/fetchPrepartPayment")
//	 public 

	@PostMapping("/save-loan")
	public ResponseEntity<?> testSaveLoan(@RequestBody String requestBody) {
		try {
			loanService.saveLoanFromOnConfirm(requestBody);
			return ResponseEntity.ok(Map.of("status", "success", "message", "Loan data saved from on_confirm payload"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
	}

//	    @PostMapping("/fetchloans")
//	    public ResponseEntity<?> fetchLoans(@RequestBody String mobileNumber){
//	    	try {
//	    		return loanService.fetchLoans(mobileNumber);
//	    	}catch(Exception e) {
//	    		e.printStackTrace();
//	    	}
//	    	return null;
//	    }
//	@PostMapping("/fetchloans")
//	public ResponseEntity<?> fetchLoans(@RequestBody Map<String, String> request) {
//		String mobileNumber = request.get("mobileNumber");
//		return loanService.fetchLoans(mobileNumber);
//	}
	@PostMapping("/fetchloans")
	public ResponseEntity<?> fetchLoans(@RequestBody Map<String, String> request) {
	    String mobileNumber = request.get("mobileNumber");
	    return loanService.fetchLoans(mobileNumber);
	   
	}

	@PostMapping("/fetchInstallmentByEndDate")
	public ResponseEntity<?> fetchInstallmentByEndDate(@RequestBody Map<String, String> request) {
		try {
			String loanNumber = request.get("loanNumber");
			return ResponseEntity.ok(loanInstallmentService.getUpcomingInstallment(loanNumber));
		}catch(LoanNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
		catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
		
	}
	
	@PostMapping("/save-prepartpayment")
	public ResponseEntity<?> savePartPayment(@RequestBody String requestBody) {
		try {
			loanService.updateLoanFromOnUpdate(requestBody);
			return ResponseEntity.ok(Map.of("status", "success", "message", "Loan data saved from on_confirm payload"));
		}
		catch(LoanNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
	}
	
//	@PostMapping("/fetchInstallmentsByLoan")
//	public ResponseEntity<?> fetchInstallmentsByLoan(@RequestParam(name="loanId") String loanId) {
//		try {
////			String loanNumber = request.get("loanNumber");
//			return ResponseEntity.ok(loanInstallmentService.fetchInstallmentsByLoan(loanId));
//		}catch(LoanNotFoundException e) {
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//		}
//		catch(Exception e) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body(Map.of("status", "error", "message", e.getMessage()));
//		}
//		
//	}
	
	@PostMapping("/fetchInstallmentsByLoan")
	public ResponseEntity<?> fetchInstallmentsByLoan(@RequestBody Map<String, String> request) {
	    try {
	        String loanId = request.get("loanId");  // fetch from JSON body
	        return ResponseEntity.ok(loanInstallmentService.fetchInstallmentsByLoan(loanId));
	    } catch (LoanNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("status", "error", "message", e.getMessage()));
	    }
	}

	@PostMapping("/fetchMissedEmiByLoan")
	public ResponseEntity<?> fetchMissedEmiByLoan(@RequestBody Map<String, String> request) {
	    try {
	        String loanId = request.get("loanId");  // fetch from JSON body
	        return ResponseEntity.ok(loanInstallmentService.fetchMissedEmiByLoan(loanId));
	    } catch (LoanNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("status", "error", "message", e.getMessage()));
	    }
	}
	
//	@PostMapping("/networkobservability")
//	public ResponseEntity<?> networkObservability(@RequestBody Map<> )
	
	@PostMapping("/fetchByPincode")
	public ResponseEntity<?> fetchLendersByPincode(@RequestParam(name = "pincode") String pincode){
		
		try {
			Optional<Master_City_State> optionalMasterCityState = masterCityStateRepository.findByPincode(Integer.parseInt(pincode));
			if(optionalMasterCityState.isEmpty()) {
				return null;
			}
			
			Master_City_State masterCityState = optionalMasterCityState.get();
			
			List<Product> products = productRepository.findAll();
			List<Product> finalproductList = new ArrayList<>();
			for(Product product : products) {
//				tejas
				if(product.getProductName().equalsIgnoreCase("Kissht")) {
					
					if(masterCityState.getKissht()!=null && masterCityState.getKissht().equalsIgnoreCase("y")) {
						finalproductList.add(product);
					}
				}else if(product.getProductName().equalsIgnoreCase("ABCL")) {
					if(masterCityState.getABCL()!=null && masterCityState.getABCL().equalsIgnoreCase("y")) {
						finalproductList.add(product);
					}
				}else if(product.getProductName().equalsIgnoreCase("BFL")) {
					if(masterCityState.getBFL()!=null && masterCityState.getBFL().equalsIgnoreCase("y")) {
						finalproductList.add(product);
					}
				}
			}
			
			return ResponseEntity.ok(finalproductList);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@PostMapping("WriteFormLogs")
	public ResponseEntity<?> writeFormLogs(@RequestBody FormLogRequest formLogRequest){
		try {
			
			String mobilenumber = formLogRequest.getOndcFormDataDTO().getContactNumber();
			String ondcFormDTOJSON = null;
			if(formLogRequest.getOndcFormDataDTO()!=null) {
				ObjectMapper mapper = new ObjectMapper(); // ✅ create instance
	            ondcFormDTOJSON = mapper.writeValueAsString(formLogRequest.getOndcFormDataDTO());
			}
			String responseJSON = null;
			if(formLogRequest.getResponse()!=null) {
				ObjectMapper mapper = new ObjectMapper();
				responseJSON =mapper.writeValueAsString(formLogRequest.getResponse());
			}
			
			String gatewayUrl = formLogRequest.getGatewayUrl();
			String transactionId = formLogRequest.getTransactionId();
			String formSubmissionStatus = formLogRequest.getFormSubmissionStatus();
			String productName = formLogRequest.getProductName();
			searchService.writeFormLogs(mobilenumber, ondcFormDTOJSON, responseJSON, gatewayUrl, transactionId, formSubmissionStatus, productName);
			return ResponseEntity.ok("DONE"); 
		}catch(Exception e) {
			 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                .body(Map.of("status", "error", "message", e.getMessage()));
		}
	}
	
	@PostMapping("/getUserDetails")
	public ResponseEntity<?> getUserDetails(@RequestParam(name="mobileNumber") String mobileNumber){
		try {
			Optional<UserInfo> userInfo = userInfoRepository.findByMobileNumber(mobileNumber);
			if(userInfo.isEmpty()) {
				return null;
			}
			
			Map<String, Object> map = new HashMap<>();
			map.put("workPincode", userInfo.get().getWorkPincode());
			map.put("maritalStatus", userInfo.get().getMaritalStatus());
			map.put("paymentType", userInfo.get().getPaymentType());
			map.put("firstNameFromPan", userInfo.get().getFirstName());
			map.put("lastNameFromPan", userInfo.get().getLastName());
			map.put("panName", userInfo.get().getPanName());
			map.put("dob",userInfo.get().getDob());

			map.put("gender",userInfo.get().getGender() == 1 ? "male" : userInfo.get().getGender() == 2 ? "female" : "other");
			map.put("pan", userInfo.get().getPan());
			map.put("mobileNumber", userInfo.get().getMobileNumber());
			map.put("email", userInfo.get().getEmail());
//            ondcFormDataDTO.setOfficialemail(userInfo.getWorkEmail());
			map.put("workEmail", userInfo.get().getWorkEmail());
			
			if (userInfo.get().getEmploymentType() == 1) {
//            	ondcFormDataDTO.setEmploymentType("Salaried");
				map.put("profession", "salaried");
			} else if (userInfo.get().getEmploymentType() == 2) {
				map.put("profession", "Self Employment");
			} else if (userInfo.get().getEmploymentType() == 3) {
				map.put("profession", "Self Employment");// as ondc form has only two fields salaried and
																		// self employed otherwise here would be
																		// Business
			}
			
			int inc = 0;
			if (userInfo.get().getMonthlyIncome() != null) {
			    try {
			        // Use BigDecimal to handle both integer and decimal values
			        BigDecimal income = new BigDecimal(userInfo.get().getMonthlyIncome().toString());
			        inc = income.intValue(); // safely converts (truncates decimal part if present)
			    } catch (NumberFormatException e) {
			        // log and keep inc = 0 if parsing fails
			        System.err.println("Invalid income format: " + userInfo.get().getMonthlyIncome());
			    }
			}
			
			map.put("income", String.valueOf(inc));
			
			map.put("company", userInfo.get().getCompanyName());
			map.put("addressline1", userInfo.get().getAddress());
			
			map.put("pincode", userInfo.get().getResidentialPincode() != null ? userInfo.get().getResidentialPincode().toString() : null);
			map.put("creditProfile", userInfo.get().getCreditProfile());
			
			
			return ResponseEntity.ok(map);
		}catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
	
	@PostMapping("/getUserInfo")
	public ResponseEntity<?> getUserInfo(@RequestParam(name="mobileNumber") String mobileNumber){
		try {
			Optional<UserInfo> userInfo = userInfoRepository.findByMobileNumber(mobileNumber);
			if(userInfo.isEmpty()) {
				return null;
			}
			
//			Map<String, Object> map = new HashMap<>();
//			map.put("workPincode", userInfo.get().getWorkPincode());
//			map.put("maritalStatus", userInfo.get().getMaritalStatus());
//			map.put("paymentType", userInfo.get().getPaymentType());
			
			return ResponseEntity.ok(userInfo.get());
		}catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
	
	@PostMapping("/getProductByStatus")
	public ResponseEntity<?> getProductByStatus(@RequestParam(name="status") int status ){
		try {
			List<Product> products = productRepository.findByStatus(status);
			
			return ResponseEntity.ok(products);
//			Map<String, Object> map = new HashMap<>();
//			map.put("workPincode", userInfo.get().getWorkPincode());
//			map.put("maritalStatus", userInfo.get().getMaritalStatus());
//			map.put("paymentType", userInfo.get().getPaymentType());
			
		}catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
	
	//srcref is consent handler id
	@PostMapping("/finvuRedirect")
	public ResponseEntity<?> finvuRedirect(@RequestParam(name="transactionId") String txnid,@RequestParam(name="srcref") String srcref, @RequestParam(name="mobileNumber") String mobileNumber, @RequestParam(name="redirectUrl") String redirecturl){
		try {
			String redirectionlinkforaa = finvuService.linkparam(txnid, srcref, mobileNumber, redirecturl);
			return ResponseEntity.ok(redirectionlinkforaa);
		}catch(Exception e) {
			return null;
		}
	}
	
//	@PostMapping("/setFrontendContext")
//	public ResponseEntity<?> frontendContext(@RequestParam(name="transactionId") String transactionId, @RequestParam("bppId") String bppId){
//		try {
////			List<Callback> callbacks = callbackRepository.findByuID(transactionId);
////			Optional<Callback> callbacks = callbackRepository.findByuIdLatest(transactionId);
////			"pahal.lenderbridge.uat.ignosis.ai"
//			Optional<Callback> callbacks = callbackRepository.findByTransactionIdAndBppId(transactionId, bppId);
//			return ResponseEntity.ok(callbacks);
//			
//		}catch(Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
	
	@PostMapping("/setFrontendContext")
	public ResponseEntity<?> frontendContext(@RequestParam(name="transactionId") String transactionId, @RequestParam("bppId") String bppId){
		try {
//			List<Callback> callbacks = callbackRepository.findByuID(transactionId);
//			Optional<Callback> callbacks = callbackRepository.findByuIdLatest(transactionId);
//			"pahal.lenderbridge.uat.ignosis.ai"
			Optional<Callback> callbacks = callbackRepository.findByTransactionIdAndBppId(transactionId, bppId);
			if(callbacks.isEmpty())
			{
				return null;
			}else {
				if(callbacks.get().getApi().equalsIgnoreCase("/on_status")) {
					
					//here we will get the submission id from the on_status
					//code to get submission id
					
					//here we will find again from db the record before on_status
					Optional<Callback> callbacks2 = callbackRepository.findLatestExcludingAction(transactionId, bppId, "on_status");//here on_status is the action that we will be skipping
					if(callbacks2.isEmpty()) {
						return null;
					}else {
						Callback returnCallback = callbacks2.get();
						Map<String,Object> map = new HashMap();
						map.put("content1", callbacks2);
						map.put("content2", callbacks);
						return ResponseEntity.ok(map);
					}
				}
			}
			
			return ResponseEntity.ok(callbacks);
			
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@PostMapping("/getFirstPageData")
	public ResponseEntity<?> fetchFirstPageData(@RequestParam(name="mobileNumber") String mobileNumber){
		try {
			Optional<UserInfo> userInfo = userInfoRepository.findByMobileNumber(mobileNumber);
			if(userInfo.isEmpty()) {
				return null;
			}
			
			//if user is present ----->
			Optional<JourneyLog> journeyLog = journeyLogRepository.findByUser(userInfo.get().getId());
			if(journeyLog.isEmpty()) {
				return null;
			}
			
//			List<Callback> callbacks = callbackRepository.findByuID(journeyLog.get().getUId());
			
			List<String> callbacks = callbackRepository.findContentByUid(journeyLog.get().getUId());
			
			ObjectMapper objectMapper = new ObjectMapper();
			List<JsonNode> jsonCallback = new ArrayList();
			for(String c : callbacks) {
				
				jsonCallback.add(objectMapper.readTree(c));
			}
			
			return ResponseEntity.ok(jsonCallback);
			
		}catch(Exception e) {
			
		}
		return null;
	}
	
	@PostMapping("/getBureauData")
	public ResponseEntity<?> fetchBureauData(@RequestParam(name="mobileNumber") String mobileNumber){
		try {
			Optional<UserBureauData> userBureauData = userBureauDataRepository.findLatestByPhone(mobileNumber);
			return ResponseEntity.ok(userBureauData);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	@PostMapping("/findJourneyLog")
	
	@PostMapping("/saveStage")
	public ResponseEntity<?> saveStage(@RequestParam(name="mobileNumber") String mobileNumber, @RequestParam(name="transactionId") String transactionId, @RequestParam(name="gatewayUrl") String gatewayUrl, @RequestParam(name="platformId") String platformId, @RequestParam(name="stage") int stage){
		try {
			Optional<UserInfo> userInfo = userInfoRepository.findByMobileNumber(mobileNumber);
			if(userInfo.isEmpty()) {
//				return null;
				return ResponseEntity.ok("null");
			}
			
			JourneyLog journeyLog = new JourneyLog();
			journeyLog.setPlatformId(platformId);
			journeyLog.setUser(userInfo.get());
			journeyLog.setRequestId(gatewayUrl);
			journeyLog.setUId(transactionId);
			journeyLog.setStage(stage);
			
			journeyLogRepository.save(journeyLog);
			
			return ResponseEntity.ok("ok");
		}catch(Exception e) {
			e.printStackTrace();
//			return null;
			return ResponseEntity.ok("exception occured");
		}
	}
	
	@PostMapping("/getONDCFormDataDTO")
	public ResponseEntity<?> getONDCFormDataDTO(@RequestParam(name="mobileNumber") String mobileNumber){
		try {
			
			Optional<UserInfo> optionlUserInfo = userInfoRepository.findByMobileNumber(mobileNumber);
			if(optionlUserInfo.isEmpty()) {
				return null;
			}
			
			UserInfo userInfo = optionlUserInfo.get();
			
			String dobRaw = userInfo.getDob(); // The original DOB value
			LocalDate dobDate = null;

			// Try parsing the date with possible known formats
			DateTimeFormatter[] knownFormats = new DateTimeFormatter[] { DateTimeFormatter.ofPattern("yyyy-MM-dd"), // common
																													// API
																													// format
					DateTimeFormatter.ofPattern("dd/MM/yyyy"), // slash format
					DateTimeFormatter.ofPattern("MM-dd-yyyy"), // US format
					DateTimeFormatter.ofPattern("dd-MM-yyyy") // Desired format (already correct)
			};

			for (DateTimeFormatter format : knownFormats) {
				try {
					dobDate = LocalDate.parse(dobRaw, format);
					break; // successfully parsed
				} catch (DateTimeParseException e) {
					// try next
				}
			}

			if (dobDate == null) {
				throw new RuntimeException("Unrecognized date format: " + dobRaw);
			}

			// Now format it to dd-MM-yyyy
//            String dobFormatted = dobDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			String dobFormatted = dobDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//            String dobFormatted = dobDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
//            ondcFormDataDTO.setDob(dobFormatted);

			// ----------------------------------------------------------------------------------------------------

			// FormDataDTO
			ONDCFormDataDTO ondcFormDataDTO = new ONDCFormDataDTO();

//			ondcFormDataDTO.setPanName(userInfo.getFirstName() + " " + userInfo.getLastName());
//			ondcFormDataDTO.setPanName(userInfo.getFirstName() + " "+userInfo.getFatherName()+" " + userInfo.getLastName());
			ondcFormDataDTO.setPanName(userInfo.getPanName());
			
			String panName = userInfo.getPanName();

//			ondcFormDataDTO.setFirstName(userInfo.getFirstName());
//			ondcFormDataDTO.setLastName(userInfo.getLastName());
			
			if (panName != null && !panName.trim().isEmpty()) {
			    String[] parts = panName.trim().split("\\s+", 2); // split into 2 parts
			    String firstNameFromPan = parts[0]; // first word
			    String lastNameFromPan = parts.length > 1 ? parts[1] : ""; // rest of the name if available

			    ondcFormDataDTO.setFirstName(firstNameFromPan);
			    ondcFormDataDTO.setLastName(lastNameFromPan);
			    ondcFormDataDTO.setPanName(panName);
			}
			
//            ondcFormDataDTO.setDob(userInfo.getDob());
			ondcFormDataDTO.setDob(dobFormatted);

			ondcFormDataDTO
					.setGender(userInfo.getGender() == 1 ? "male" : userInfo.getGender() == 2 ? "female" : "other");
			ondcFormDataDTO.setPan(userInfo.getPan());
			ondcFormDataDTO.setContactNumber(userInfo.getMobileNumber());
			ondcFormDataDTO.setEmail(userInfo.getEmail());
//            ondcFormDataDTO.setOfficialemail(userInfo.getWorkEmail());
			ondcFormDataDTO.setOfficialEmail(userInfo.getWorkEmail());

			if (userInfo.getEmploymentType() == 1) {
//            	ondcFormDataDTO.setEmploymentType("Salaried");
				ondcFormDataDTO.setEmploymentType("salaried");
			} else if (userInfo.getEmploymentType() == 2) {
				ondcFormDataDTO.setEmploymentType("Self Employment");
			} else if (userInfo.getEmploymentType() == 3) {
				ondcFormDataDTO.setEmploymentType("Self Employment");// as ondc form has only two fields salaried and
																		// self employed otherwise here would be
																		// Business
			}

//			ondcFormDataDTO.setEndUse("consumerDurablePurchase"); // if static
			ondcFormDataDTO.setEndUse("other");
//            ondcFormDataDTO.setIncome(userInfo.getMonthlyIncome() != null ? userInfo.getMonthlyIncome().toString() : null);

			int inc = 0;
			if (userInfo.getMonthlyIncome() != null) {
			    try {
			        // Use BigDecimal to handle both integer and decimal values
			        BigDecimal income = new BigDecimal(userInfo.getMonthlyIncome().toString());
			        inc = income.intValue(); // safely converts (truncates decimal part if present)
			    } catch (NumberFormatException e) {
			        // log and keep inc = 0 if parsing fails
			        System.err.println("Invalid income format: " + userInfo.getMonthlyIncome());
			    }
			}
			
			ondcFormDataDTO.setIncome(String.valueOf(inc));
			
			//			ondcFormDataDTO.setIncome("100000");
			ondcFormDataDTO.setCompanyName(userInfo.getCompanyName());
			ondcFormDataDTO.setUdyamNumber(null); // static or from another source
			ondcFormDataDTO.setAddressL1(userInfo.getAddress());
			ondcFormDataDTO.setAddressL2(""); // Optional field
//			ondcFormDataDTO.setCity("Pune"); // Static or derive if available
//			ondcFormDataDTO.setState("Maharashtra"); // Same
			ondcFormDataDTO.setCity("NA");
			ondcFormDataDTO.setState("NA");
			ondcFormDataDTO.setPincode(
					userInfo.getResidentialPincode() != null ? userInfo.getResidentialPincode().toString() : null);
			ondcFormDataDTO.setAa_id(userInfo.getMobileNumber() + "@finvu");
//            ondcFormDataDTO.setAa_id("")
			ondcFormDataDTO.setBureauConsent("on");
			
			return ResponseEntity.ok(ondcFormDataDTO);
			
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

}