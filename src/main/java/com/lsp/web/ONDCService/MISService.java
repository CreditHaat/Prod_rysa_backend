package com.lsp.web.ONDCService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lsp.web.entity.MIS;
import com.lsp.web.repository.MISRepository;

@Service
public class MISService {

    @Autowired
    private MISRepository misRepository;

    public boolean saveTransactionId(String mobileNumber, String transactionId) {
        LocalDateTime now = LocalDateTime.now();

        // Fetch all MIS for the mobile number, ordered by createTime desc
        List<MIS> misList = misRepository.findAllByMobileNumberOrderByCreateTimeDesc(mobileNumber);
        MIS activeMIS = null;

        // Find the latest active MIS within 7 days (cancelFlag != "cancel")
        for (MIS m : misList) {
            if (!"cancel".equalsIgnoreCase(m.getCancelFlag()) && 
                m.getCreateTime() != null && 
                m.getCreateTime().isAfter(now.minusDays(7))) {
                activeMIS = m;
                break;
            }
        }

        if (activeMIS == null) {
            // No active MIS found within 7 days
            return false;
        }

        // Update the transactionId
        activeMIS.setTransactionId(transactionId);

        // Save the MIS
        misRepository.saveAndFlush(activeMIS);
        return true;
    }

	public String updateStatusBeforeTransactionId(String mobileNumber, String status) {
		
		Optional<MIS> optionalMis = misRepository.findTopByMobileNumberOrderByCreateTimeDesc(mobileNumber);
		if(optionalMis.isEmpty()) {
			return null;
		}
		
		MIS mis = optionalMis.get();
		mis.setStatus(status);
		misRepository.save(mis);
		
		return "OK";
		
	}

	public String updateStatusAfterTransactionId(String transactionId, String status) {
		
		Optional<MIS> optionaMis = misRepository.findByTransactionId(transactionId);
		if(optionaMis.isEmpty()) {
			return null;
		}
		
		MIS mis = optionaMis.get();
		mis.setStatus(status);
		misRepository.save(mis);
		
		return "OK";
		
	}
}



//package com.lsp.web.ONDCService;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.lsp.web.entity.MIS;
//import com.lsp.web.repository.MISRepository;
//
//@Service
//public class MISService {
//    @Autowired
//    private MISRepository misRepository;
//
//    public boolean saveTransactionId(String mobileNumber, String transactionId) {
//        Optional<MIS> optionalMIS = misRepository.findByMobileNumber(mobileNumber);
//
//        if (optionalMIS.isPresent()) {
//            MIS mis = optionalMIS.get();
//
//            // Save transaction ID into transaction_ID column
//            mis.setTransactionId(transactionId);
//            //mis.setOfferFlag(String.valueOf(offerFlag)); // Convert int to String
//
//            misRepository.save(mis);
//            return true;
//        }
//        return false; //MIS not found
//    }
//}
