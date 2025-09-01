package com.lsp.web.dto;

import java.util.Date;

public class UserInfoDto {

	private String firstName;
    private String fatherName;
    private String lastName;
	private String mobileNumber;
	private String email;
	private String pan;
	private Float loanAmount;
	private Integer agentId;
	private String agent;
    private String creditProfile;
	private String dob;
    private String address;
//    private String pincode;
//    private String gender;
//    private String maritalStatus;
//    private String salary;
//    private String paymentType;
    private Integer residentialPincode; // matches entity
    private Integer gender;             // matches entity
    private Integer maritalStatus;      // matches entity
    private Float monthlyIncome;        // maps to salary in DTO
    private Integer paymentType;        // matches entity
    private Integer employmentType; 
    private String currentResidence;
    private String residenceType;
    private String monthlyExpense;
    private String companyName;
    private String idProof;
    private String incomeProof;
    private String employmentProof;
    private Date registerTime;
    private Integer priorityTwoScore;
    private Integer priorityThreeScore;
	private String repaymentScore;
	private Integer finalScore;

	public UserInfoDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserInfoDto(String firstName, String fatherName, String lastName, String mobileNumber, String email, String pan, Float loanAmount, Integer agentId,
			String agent, String creditProfile, String dob, String address, Integer residentialPincode, Integer gender,
			Integer maritalStatus, Float monthlyIncome, Integer paymentType, String currentResidence, String residenceType,
			String monthlyExpense, String companyName, String idProof, String incomeProof, String employmentProof, Date registerTime,Integer priorityTwoScore,
     Integer priorityThreeScore,
     String repaymentScore,Integer finalScore) {
		super();
		this.firstName = firstName;
        this.fatherName = fatherName;
        this.lastName = lastName;
		this.mobileNumber = mobileNumber;
		this.email = email;
		this.pan = pan;
		this.loanAmount = loanAmount;
		this.agentId = agentId;
		this.agent = agent;
		this.creditProfile = creditProfile;
		this.dob = dob;
		this.address = address;
		this.residentialPincode = residentialPincode;
		this.gender = gender;
		this.maritalStatus = maritalStatus;
		this.monthlyIncome = monthlyIncome;
		this.paymentType = paymentType;
		this.currentResidence = currentResidence;
		this.residenceType = residenceType;
		this.monthlyExpense = monthlyExpense;
		this.companyName = companyName;
		this.idProof = idProof;
		this.incomeProof = incomeProof;
		this.employmentProof = employmentProof;
		this.registerTime = registerTime;
		this.priorityTwoScore=priorityTwoScore;
		this.priorityThreeScore=priorityThreeScore;
		this.repaymentScore=repaymentScore;
		this.finalScore=finalScore;
	}


	public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public Float getLoanAmount() {
		return loanAmount;
	}

	public void setLoanAmount(Float loanAmount) {
		this.loanAmount = loanAmount;
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

	public Integer getPriorityTwoScore() {
		return priorityTwoScore;
	}

	public void setPriorityTwoScore(Integer priorityTwoScore) {
		this.priorityTwoScore = priorityTwoScore;
	}

	public Integer getPriorityThreeScore() {
		return priorityThreeScore;
	}

	public void setPriorityThreeScore(Integer priorityThreeScore) {
		this.priorityThreeScore = priorityThreeScore;
	}

	public String getRepaymentScore() {
		return repaymentScore;
	}

	public void setRepaymentScore(String repaymentScore) {
		this.repaymentScore = repaymentScore;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

//	public String getPincode() {
//		return pincode;
//	}

//	public void setPincode(String pincode) {
//		this.pincode = pincode;
//	}
//
//	public String getGender() {
//		return gender;
//	}
//
//	public void setGender(String gender) {
//		this.gender = gender;
//	}

	public Date getRegisterTime() {
		return registerTime;
	}

	public void setRegisterTime(Date registerTime) {
		this.registerTime = registerTime;
	}

	public String getCreditProfile() {
		return creditProfile;
	}

	public void setCreditProfile(String creditProfile) {
		this.creditProfile = creditProfile;
	}

//	public String getMaritalStatus() {
//		return maritalStatus;
//	}
//
//	public void setMaritalStatus(String maritalStatus) {
//		this.maritalStatus = maritalStatus;
//	}
//
//	public String getSalary() {
//		return salary;
//	}
//
//	public void setSalary(String salary) {
//		this.salary = salary;
//	}
//
//	public String getPaymentType() {
//		return paymentType;
//	}
//
//	public void setPaymentType(String paymentType) {
//		this.paymentType = paymentType;
//	}

	public String getCurrentResidence() {
		return currentResidence;
	}

	public void setCurrentResidence(String currentResidence) {
		this.currentResidence = currentResidence;
	}

	public String getResidenceType() {
		return residenceType;
	}

	public void setResidenceType(String residenceType) {
		this.residenceType = residenceType;
	}

	public String getMonthlyExpense() {
		return monthlyExpense;
	}

	public void setMonthlyExpense(String monthlyExpense) {
		this.monthlyExpense = monthlyExpense;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getIdProof() {
		return idProof;
	}

	public void setIdProof(String idProof) {
		this.idProof = idProof;
	}

	public String getIncomeProof() {
		return incomeProof;
	}

	public void setIncomeProof(String incomeProof) {
		this.incomeProof = incomeProof;
	}

	public String getEmploymentProof() {
		return employmentProof;
	}

	public void setEmploymentProof(String employmentProof) {
		this.employmentProof = employmentProof;
	}

	public Integer getFinalScore() {
		return finalScore;
	}

	public void setFinalScore(Integer finalScore) {
		this.finalScore = finalScore;
	}

	public Integer getResidentialPincode() {
		return residentialPincode;
	}

	public void setResidentialPincode(Integer residentialPincode) {
		this.residentialPincode = residentialPincode;
	}

	public Integer getGender() {
		return gender;
	}

	public void setGender(Integer gender) {
		this.gender = gender;
	}

	public Integer getMaritalStatus() {
		return maritalStatus;
	}

	public void setMaritalStatus(Integer maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	public Float getMonthlyIncome() {
		return monthlyIncome;
	}

	public void setMonthlyIncome(Float monthlyIncome) {
		this.monthlyIncome = monthlyIncome;
	}

	public Integer getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(Integer paymentType) {
		this.paymentType = paymentType;
	}

	public Integer getEmploymentType() {
		return employmentType;
	}

	public void setEmploymentType(Integer employmentType) {
		this.employmentType = employmentType;
	}
	

}