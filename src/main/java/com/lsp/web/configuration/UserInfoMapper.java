package com.lsp.web.configuration;

import org.springframework.stereotype.Component;

import com.lsp.web.dto.*;
import com.lsp.web.entity.*;

@Component
public class UserInfoMapper {
	
	// To Convert UserInfo Entity to DTO
	public UserInfoDto toUserInfoDto(UserInfo userInfo) {
	
		UserInfoDto dto = new UserInfoDto();
        dto.setFirstName(userInfo.getFirstName());
        dto.setFatherName(userInfo.getFatherName());
        dto.setLastName(userInfo.getLastName());
        dto.setMobileNumber(userInfo.getMobileNumber());
        dto.setEmail(userInfo.getEmail());
        dto.setPan(userInfo.getPan());
        dto.setLoanAmount(userInfo.getLoanAmount());
//        dto.setAgent(userInfo.getAgent());
//        dto.setAgentId(userInfo.getAgentId());
//        dto.setCreditProfile(userInfo.getCreditProfile());
        dto.setDob(userInfo.getDob());
        dto.setAddress(userInfo.getAddress());
        dto.setResidentialPincode(userInfo.getResidentialPincode());
        dto.setGender(userInfo.getGender());
        dto.setMaritalStatus(userInfo.getMaritalStatus());
        dto.setMonthlyIncome(userInfo.getMonthlyIncome());
        dto.setPaymentType(userInfo.getPaymentType());
        dto.setEmploymentType(userInfo.getEmploymentType());
        dto.setCompanyName(userInfo.getCompanyName());
//		userInfoDto.setRegisterTime(userInfo.getRegisterTime());
		return dto;
	}
	
	// To Convert DTO to UserInfo Entity
	 public UserInfo toUserInfo(UserInfoDto dto) {
	        if (dto == null) {
	            return null;
	        }

	        UserInfo entity = new UserInfo();
	        entity.setFirstName(dto.getFirstName());
	        entity.setFatherName(dto.getFatherName());
	        entity.setLastName(dto.getLastName());
	        entity.setMobileNumber(dto.getMobileNumber());
	        entity.setEmail(dto.getEmail());
	        entity.setPan(dto.getPan());
	        entity.setLoanAmount(dto.getLoanAmount());
//	        entity.setAgentId(dto.getAgentId());
//	        entity.setAgent(dto.getAgent());
//	        entity.setCreditProfile(dto.getCreditProfile());
	        entity.setDob(dto.getDob());
	        entity.setAddress(dto.getAddress());
	        entity.setResidentialPincode(dto.getResidentialPincode());
	        entity.setGender(dto.getGender());
	        entity.setMaritalStatus(dto.getMaritalStatus());
	        entity.setMonthlyIncome(dto.getMonthlyIncome());
	        entity.setPaymentType(dto.getPaymentType());
	        entity.setEmploymentType(dto.getEmploymentType());
	        entity.setCompanyName(dto.getCompanyName());
	
	
	        return entity;
	    }
	}

