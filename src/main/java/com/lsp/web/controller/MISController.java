package com.lsp.web.controller;

import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lsp.web.ONDCService.MISService;
@RestController
@RequestMapping("/api/mis")
public class MISController {

    @Autowired
    private MISService misService;

    @CrossOrigin("*")
    @PostMapping("/saveTransactionId")
    public ResponseEntity<?> saveTransactionId(@RequestParam String mobileNumber,
                                               @RequestParam String transactionId
                                               ) {
        boolean success = misService.saveTransactionId(mobileNumber, transactionId);
        if (success) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Transaction ID saved successfully"));
        } else {
        	return ResponseEntity.status(HttpStatus.SC_NOT_FOUND)
        	        .body(Map.of("status", "failed", "message", "No MIS record found for this mobile number"));
        }
    }
    
    @CrossOrigin("*")
    @PostMapping("/updateBeforeTransaction")
    public ResponseEntity<?> updateStatusBeforeTransactionId(@RequestParam String mobileNumber, @RequestParam String status){
    	try {
    		misService.updateStatusBeforeTransactionId(mobileNumber, status);
    		return ResponseEntity.ok("done");
    	}catch(Exception e) {
    		e.printStackTrace();
    		return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    	}
    }
    
    @CrossOrigin("*")
    @PostMapping("/updateAfterTransaction")
    public ResponseEntity<?> updateStatusAfterTransactionId(@RequestParam String transactionId, @RequestParam String status){
    	try {
    		misService.updateStatusAfterTransactionId(transactionId, status);
    		return ResponseEntity.ok("done");
    	}catch(Exception e) {
    		e.printStackTrace();
    		return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    	}
    }
}