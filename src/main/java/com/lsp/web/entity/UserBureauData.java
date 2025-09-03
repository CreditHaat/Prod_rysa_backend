package com.lsp.web.entity;

import java.time.LocalDateTime;

import com.lsp.web.genericentity.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table
(name="t_user_bureau_data")
public class UserBureauData extends BaseEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

//    @Column(name = "register_time")
//    private LocalDateTime registerTime;

    @Column(name = "mobile_number", length = 15, nullable = false)
    private String mobileNumber;

    @Column(name = "credit_score", length = 10)
    private String creditScore;

    @Column(name = "response_content", columnDefinition = "MEDIUMTEXT")
    private String responseContent;

    @Column(name = "user_id")
    private Long userId;

//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}

//	public LocalDateTime getRegisterTime() {
//		return registerTime;
//	}
//
//	public void setRegisterTime(LocalDateTime registerTime) {
//		this.registerTime = registerTime;
//	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getCreditScore() {
		return creditScore;
	}

	public void setCreditScore(String creditScore) {
		this.creditScore = creditScore;
	}

	public String getResponseContent() {
		return responseContent;
	}

	public void setResponseContent(String responseContent) {
		this.responseContent = responseContent;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	

}