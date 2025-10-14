package com.lsp.web.entity;
import java.util.Date;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import com.lsp.web.genericentity.BaseEntity;

import java.time.LocalDateTime;
import java.util.Base64;


@Entity
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "t_userinfo" , indexes = {@Index(name="first_index",columnList = "mobileNumber")})
public class UserInfo extends BaseEntity {
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private BankDetails bankDetails;

    @Column(name = "last_attribution_time")
    private LocalDateTime lastAttributionTime;//last attribution time column

    
//    @Column(name = "reattribution_time")
//    private LocalDateTime reattributionTime;   // reattribution time Column
//
//	public LocalDateTime getReattributionTime() {
//		return reattributionTime;
//	}
//
//	public void setReattributionTime(LocalDateTime reattributionTime) {
//		this.reattributionTime = reattributionTime;
//	}

	public LocalDateTime getLastAttributionTime() {
		return lastAttributionTime;
	}

	public void setLastAttributionTime(LocalDateTime lastAttributionTime) {
		this.lastAttributionTime = lastAttributionTime;
	}

	public String getClickId() {
		return clickId;
	}

	public void setClickId(String clickId) {
		this.clickId = clickId;
	}
	
	
	private String firstName;
	private String fatherName;
	private String lastName;
	
	@Column(length = 15)
	private String mobileNumber;
	
	@Column(name = "encrypted_mobile_number", length = 100)
	private String encryptedMobileNumber;

	
	private String dob;
	private Integer EmploymentType;
	private Integer paymentType;
	private Float monthlyIncome;
	
	@Column(length = 11)
	private String pan;
	
	private Integer gender;
	private Integer residentialPincode;
	private String email;
	private Float loanAmount;
	private String spouseName;
	private Integer maritalStatus;
	
	private String panName;
	
	@Column(length = 500)
	private String address;
	
	@Column(length = 100)
	private String companyName;
	
	@Column(length = 50)
	private String workEmail;
	private Integer workPincode;
	
	@Column(length = 100)
	private String motherName;
	
	private String YOE;
	@Column(name = "active", nullable = false)
	private Integer active;
	
//	private String creditProfile;
	
	@Column(name = "agent_id", length = 20)
	 private Integer agentId;//DSA
	    
   @Column(name = "agent", length = 30)
   private String agent; //source
   @Column(name = "click_id", length = 50)
   private String clickId; //clickid 
   
   @Column(name = "sub_agent", length = 50)//sub dsa
   private String sub_agent;

   @Column(name = "source", length = 50)
   private String source; //App Source

   @Column(name = "web_source", length = 50)
   private String webSource; //Web Source
   
   @Column(name = "credit_profile")
   private String creditProfile;
   
   @Column(name = "campaign", length = 500)
   private String campaign;  //campaign

   @Column(name = "channel", length = 50)
   private String channel; //channel

	public Integer getActive() {
	    return active;
	}

	public void setActive(Integer active) {
	    this.active = active;
	}
	public BankDetails getBankDetails() {
		return bankDetails;
	}
	public void setBankDetails(BankDetails bankDetails) {
		this.bankDetails = bankDetails;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getFatherName() {
		return fatherName;
	}
	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
		
		if (mobileNumber != null) {
	        this.encryptedMobileNumber = Base64.getEncoder().encodeToString(mobileNumber.getBytes());
	    } else {
	        this.encryptedMobileNumber = null;
	    }
	}
	public String getDob() {
		return dob;
	}
	public void setDob(String dob) {
		this.dob = dob;
	}
	public Integer getEmploymentType() {
		return EmploymentType;
	}
	public void setEmploymentType(Integer employmentTypeCode) {
		EmploymentType = employmentTypeCode;
	}
	public Integer getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(Integer paymentType) {
		this.paymentType = paymentType;
	}
	public Float getMonthlyIncome() {
		return monthlyIncome;
	}
	public void setMonthlyIncome(Float monthlyIncome) {
		this.monthlyIncome = monthlyIncome;
	}
	public String getPan() {
		return pan;
	}
	public void setPan(String pan) {
		this.pan = pan;
	}
	public Integer getGender() {
		return gender;
	}
	public void setGender(Integer gender) {
		this.gender = gender;
	}
	public Integer getResidentialPincode() {
		return residentialPincode;
	}
	public void setResidentialPincode(Integer residentialPincode) {
		this.residentialPincode = residentialPincode;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Float getLoanAmount() {
		return loanAmount;
	}
	public void setLoanAmount(Float loanAmount) {
		this.loanAmount = loanAmount;
	}
	public String getSpouseName() {
		return spouseName;
	}
	public void setSpouseName(String spouseName) {
		this.spouseName = spouseName;
	}
	public Integer getMaritalStatus() {
		return maritalStatus;
	}
	public void setMaritalStatus(Integer maritalStatus) {
		this.maritalStatus = maritalStatus;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
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
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getWorkEmail() {
		return workEmail;
	}
	public void setWorkEmail(String workEmail) {
		this.workEmail = workEmail;
	}
	public Integer getWorkPincode() {
		return workPincode;
	}
	public void setWorkPincode(Integer workPincode) {
		this.workPincode = workPincode;
	}
	public String getMotherName() {
		return motherName;
	}
	public void setMotherName(String motherName) {
		this.motherName = motherName;
	}
	public String getYOE() {
		return YOE;
	}
	public void setYOE(String yOE) {
		YOE = yOE;
	}

	public String getEncryptedMobileNumber() {
		return encryptedMobileNumber;
	}

	public void setEncryptedMobileNumber(String encryptedMobileNumber) {
		this.encryptedMobileNumber = encryptedMobileNumber;
	}

//	public String getCreditProfile() {
//		return creditProfile;
//	}
//
//	public void setCreditProfile(String creditProfile) {
//		this.creditProfile = creditProfile;
//	}

	public String getPanName() {
		return panName;
	}

	public void setPanName(String panName) {
		this.panName = panName;
	}

	public Integer getAgentId() {
		return agentId;
	}

	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getCreditProfile() {
		return creditProfile;
	}

	public void setCreditProfile(String creditProfile) {
		this.creditProfile = creditProfile;
	}

	public String getSub_agent() {
		return sub_agent;//sub source
	}

	public void setSub_agent(String sub_agent) {
		this.sub_agent = sub_agent;
	}
	
	   @Column(name = "sub_agent_id", length = 30)
	   private Integer subAgentId; // sub_dsa

	   public Integer getSubAgentId() {
		return subAgentId;
	}

	public void setSubAgentId(Integer subAgentId) {
		this.subAgentId = subAgentId;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getWebSource() {
		return webSource;
	}

	public void setWebSource(String webSource) {
		this.webSource = webSource;
	}

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	
	
    
}