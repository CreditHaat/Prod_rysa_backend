package com.lsp.web.entity;

import java.time.LocalDateTime;

import com.lsp.web.genericentity.BaseEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "t_MIS", indexes = {
        @Index(name = "idx_mobile_number", columnList = "mobileNumber"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
public class MIS extends BaseEntity {

    @Column(length = 15, nullable = false)
    private String mobileNumber;

    // Proper relation with UserInfo
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo user;  // FK to UserInfo table
    
    @Column(name = "custom_user_id", length = 100)
    private String customUserId;

    @Column(name = "transaction_id", length = 200)
    private String transactionId;

    
    @Column(length = 100)
    private String clickId;
    
    @Column(name = "registertime")
    private LocalDateTime registerTime;
    
    @PrePersist
    public void prePersist() {
        if (this.registerTime == null) {
            this.registerTime = super.getCreateTime(); // mirror createTime from BaseEntity
        }
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }


    public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getCustomUserId() {
		return customUserId;
	}
	public void setCustomUserId(String customUserId) {
		this.customUserId = customUserId;
	}
	// Flags now stored as String (e.g. "Yes"/"No")
    @Column(length = 50)
    private String journeyFlag;

    @Column(length = 50)
    private String breFlag;

    @Column(length = 50)
    private String cancelFlag;

    
	@Column(length = 50)
    private String productFlag;
    
    @Column(length = 50)
    private String primeFlag;


    public String getPrimeFlag() {
		return primeFlag;
	}
	public void setPrimeFlag(String primeFlag) {
		this.primeFlag = primeFlag;
	}
	// Lenders / Partners (String instead of Boolean)
    @Column(length = 50)
    private String pahal;

    @Column(length = 50)
    private String kissht;

    @Column(length = 50)
    private String BFL;

    @Column(length = 50)
    private String ABCL;

    @Column(length = 50)
    private String aspireFin;

    @Column(length = 50)
    private String status;
    
    @Column(length = 10)
    private String offerFlag;


    // Getters & Setters

    public String getMobileNumber() {
        return mobileNumber;
    }
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public UserInfo getUser() {
        return user;
    }
    public String getOfferFlag() {
		return offerFlag;
	}
	public void setOfferFlag(String offerFlag) {
		this.offerFlag = offerFlag;
	}
	public void setUser(UserInfo user) {
        this.user = user;
    }

    public String getClickId() {
        return clickId;
    }
    public void setClickId(String clickId) {
        this.clickId = clickId;
    }

    public String getJourneyFlag() {
        return journeyFlag;
    }
    public void setJourneyFlag(String journeyFlag) {
        this.journeyFlag = journeyFlag;
    }

    public String getBreFlag() {
        return breFlag;
    }
    public void setBreFlag(String breFlag) {
        this.breFlag = breFlag;
    }

    public String getCancelFlag() {
		return cancelFlag;
	}

	public void setCancelFlag(String cancelFlag) {
		this.cancelFlag = cancelFlag;
	}

	public String getProductFlag() {
        return productFlag;
    }
    public void setProductFlag(String productFlag) {
        this.productFlag = productFlag;
    }
    
 
    public String getPahal() {
        return pahal;
    }
    public void setPahal(String pahal) {
        this.pahal = pahal;
    }

    public String getKissht() {
        return kissht;
    }
    public void setKissht(String kissht) {
        this.kissht = kissht;
    }

    public String getBFL() {
        return BFL;
    }
    public void setBFL(String BFL) {
        this.BFL = BFL;
    }

    public String getABCL() {
        return ABCL;
    }
    public void setABCL(String ABCL) {
        this.ABCL = ABCL;
    }

    public String getAspireFin() {
        return aspireFin;
    }
    public void setAspireFin(String aspireFin) {
        this.aspireFin = aspireFin;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    
    
}
