package com.lsp.web.ONDCService;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lsp.web.entity.Apply;
import com.lsp.web.entity.Master_City_State;
import com.lsp.web.entity.Product;
import com.lsp.web.entity.UserInfo;
import com.lsp.web.repository.ApplyFailRepository;
import com.lsp.web.repository.ApplyRepository;
import com.lsp.web.repository.MasterCityStateCustomRepository;
import com.lsp.web.repository.MasterCityStateRepository;
import com.lsp.web.repository.ProductRepository;
import com.lsp.web.repository.ProductSpecification;
import com.lsp.web.repository.UserInfoRepository;
import com.lsp.web.util.StringUtil;
import org.springframework.data.jpa.domain.Specification;


@Service
public class ProductService {
	
	@Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ApplyRepository applyRepository;

    @Autowired
    private ApplyFailRepository applyFailRepository;
    @Autowired
    private MasterCityStateCustomRepository masterCityStateCustomRepository;

//    @Autowired
//    private BusinessapplyRepository businessapplyRepository;

    @Autowired
    private MasterCityStateRepository masterCityStateRepository;

    public Product productInfoList(String mobile, String partner, String profession, int paymentType,
            float income, String pincode) {

        int salary = (int) income;

        // Adjust partner name
//        partner = adjustPartnerName(partner);

//        if (isProfessionOther(profession, partner)) {
//            return null;
//        }

        // Build JPA conditions
//        String onlyNetpayQuery = buildNetpayQuery(paymentType);
//        String onlySalaryQuery = buildSalaryQuery(profession);

        // Get user info
        UserInfo user;
        Optional<UserInfo> optionalUser = userInfoRepository.findByMobileNumber(mobile);
        if(optionalUser.isEmpty()) {
        	return null;
        }
        
        user = optionalUser.get();

        // Get product info
//        Product product;
        
        Product product = productRepository.findOne(
        	    ProductSpecification.hasProductName(partner)
        	        .and(ProductSpecification.hasStatus(0))
        	        .and(ProductSpecification.onlySalaryCondition(profession))
        	        .and(ProductSpecification.onlyNetPayCondition(paymentType))
        	).orElse(null);
        
        if(product == null) {
        	return null;
        }

//        allOf(...) = AND all conditions
//
//        		anyOf(...) = OR conditions

        // Check credit profile
        if (!checkCreditProfile(user, product)) {
            return null;
        }

        // Check if user has applied recently
//        if (isUserAppliedRecently(mobile, partner, 180)) {
//            return null;
//        }
//        if (isUserAppliedRecently(user, partner, 180)) {
//            return null;
//        }

        // Check if the city is valid
        if (!isCityValid(pincode, partner)) {
            return null;
        }

        // Check agent exclusion
//        if (isAgentExcluded(agentId, product)) {
//            return null;
//        }

        // Validate salary range
        if(product.getMinSalary()!=null && product.getMaxSalary()!=null) {
        	if (salary >= product.getMinSalary() && salary <= product.getMaxSalary()) {
//              return partner;
          	return product;
          }
        }
        

        return null;
    }

//    private String adjustPartnerName(String partner) {
//        switch (partner) {
//            case "StashFin":
//                return "Stash Fin";
//            case "IIFL-PL":
//                return "IIFL";
//            case "IIFL-BL":
//                return "IIFL";
//            case "Tata Capital":
//                return "TataCapital";
//            default:
//                return partner;
//        }
//    }

    private boolean isProfessionOther(String profession, String partner) {
        return ("SmartCoin".equalsIgnoreCase(partner) || "Prefr".equalsIgnoreCase(partner))
                && "Other".equalsIgnoreCase(profession);
    }

    private String buildNetpayQuery(int paymentType) {
        return paymentType == 2 ? "onlyNetpay<2" : "onlyNetpay=0";
    }

    private String buildSalaryQuery(String profession) {
        switch (profession) {
            case "Salaried":
                return "onlySalary<>2";
            case "Self Employed":
                return "onlySalary=0";
            case "Business":
                return "onlySalary=2";
            default:
                return "onlySalary<>2";
        }
    }

    private boolean checkCreditProfile(UserInfo user, Product product) {
        try {
//            Integer score = Integer.parseInt(user.getCreditProfile());
//            if (product.getNtc() == 1 && score < product.getCredit_profile()) {
//                return false;
//            } else if (score != 1000 && score < product.getCredit_profile()) {
//                return false;
//            }
            if(product.getNtc() == 1) {
            	Integer score = Integer.parseInt(user.getCreditProfile());
    			
    			if(score<product.getCredit_profile()) {
    				return false;
    			}
            }else {
            	Integer score = Integer.parseInt(user.getCreditProfile());
    			
    			if(score==1000) {
    				//return null;
//    				return true;
    				return false;
    			}
    			else if(score<product.getCredit_profile()) {
    			// return null;
    				return false;
    			 }else{
//    				return false; 
    				 return true;
    			 }
            }
        } catch (Exception ex) {
            // Log or handle the exception if needed
        }
        return true;
    }

//    private boolean isUserAppliedRecently(UserInfo user, String partner, int days) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DAY_OF_YEAR, -days);
//        Date cutoffDate = calendar.getTime();
////        Optional<Apply> optionalApplyDate = applyRepository.findByApplyPhoneAndProductNameAndStatusNotInAndApplyTimeGreaterThan(
////                mobile, partner, Arrays.asList(4, 7), cutoffDate);
//        
//        Optional<Apply> optionalApplyDate = applyRepository.findByUserAndProductNameAndStatusNotInAndApplyTimeGreaterThan(
//                user, partner, Arrays.asList(4, 7), cutoffDate);
//        
//        if(optionalApplyDate.isEmpty()) {
//        	return false;
//        }
//        
//        return true;
//    }

    private boolean isCityValid(String pincode, String productName) {
//        Optional<Master_City_State> optionalCityState = masterCityStateRepository.findByPincodeAndProductName(Integer.parseInt(pincode), productName);
//    	String lowerProductName = productName.toLowerCase();
    	
    	
        Master_City_State cityState = masterCityStateCustomRepository.findByPincodeAndPartner(Integer.parseInt(pincode), productName);
//        if(optionalCityState.isEmpty()) {
//        	return false;
//        }
//        return true;
        return cityState != null;
    }

    private boolean isAgentExcluded(String agentId, Product product) {
        if (StringUtil.notEmpty(agentId)) {
            String[] excludedDsa = product.getExcludeDsaList();
            return excludedDsa != null && Arrays.asList(excludedDsa).contains(agentId);
        }
        return false;
    }

}
