//package com.lsp.web.entity;
//
//    
//import com.lsp.web.genericentity.*;
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Inheritance;
//import jakarta.persistence.InheritanceType;
//import jakarta.persistence.Table;
//
//@Entity
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
//@Table(name = "t_masterpincode")
//public class Master_City_State extends BaseEntity {
//
//    private Integer pincode;
//    private String city;
//    private String state;
//    private String MeraKal;
//    private String cityId;
//
////    // Add new fields if any (like new banks or credit cards)
////    // Example:
////    private String MarriottBonvoyHDFCCard;
////    private String IDFCBank;
////    private String ANQ;
////    private String Rupicard;
////    private String YesBank;
////    private String PI_ANQ;
////    private String SBI;
////    private String ICICI_HPCL_Coral;
////    private String AxisBank;
////    private String MillenniaCreditCard;
////    private String AU_Credit_Cards;
////    private String AU_Secured_Cards;
////    private String SwiggyHDFCCard;
////    private String MoneyBackPlusCard;
////    private String DinersClubPrivilegeHDFCCard;
////    private String AyushPay;
////    private String PoonawallaFincorp;
////    private String HeroFINCORP;
//
//    // === GETTERS & SETTERS ===
//
//  
//
//    public String getCity() { return city; }
//    public Integer getPincode() {
//		return pincode;
//	}
//	public void setPincode(Integer pincode) {
//		this.pincode = pincode;
//	}
//	public void setCity(String city) { this.city = city; }
//
//    public String getState() { return state; }
//    public void setState(String state) { this.state = state; }
//
//    public String getMeraKal() { return MeraKal; }
//    public void setMeraKal(String meraKal) { MeraKal = meraKal; }
//
//    public String getCityId() { return cityId; }
//    public void setCityId(String cityId) { this.cityId = cityId; }
//
//    // Include new bank & card fields getters/setters as needed
//    // ...
//}

package com.lsp.web.entity;

    
import com.lsp.web.genericentity.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "t_masterpincode")
public class Master_City_State extends BaseEntity {

    private Integer pincode;
    private String city;
    private String state;
//    private String MeraKal;
    private String cityId;
    private String HDB;
    private String Kissht;
    private String ABCL;
    private String BFL;
    private String CreditHaat;
    private String Pahal;
	private String Aspire;

    // === GETTERS & SETTERS ===

  

    public String getCity() { return city; }
    public Integer getPincode() {
		return pincode;
	}
	public void setPincode(Integer pincode) {
		this.pincode = pincode;
	}
	public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
//
//    public String getMeraKal() { return MeraKal; }
//    public void setMeraKal(String meraKal) { MeraKal = meraKal; }

    public String getCityId() { return cityId; }
    public void setCityId(String cityId) { this.cityId = cityId; }
//	public String getHdb() {
//		return hdb;
//	}
//	public void setHdb(String hdb) {
//		this.hdb = hdb;
//	}
//	public String getKissht() {
//		return kissht;
//	}
//	public void setKissht(String kissht) {
//		this.kissht = kissht;
//	}
//	public String getAbcl() {
//		return abcl;
//	}
//	public void setAbcl(String abcl) {
//		this.abcl = abcl;
//	}
//	public String getBfl() {
//		return bfl;
//	}
//	public void setBfl(String bfl) {
//		this.bfl = bfl;
//	}
//	public String getCredithaat() {
//		return credithaat;
//	}
//	public void setCredithaat(String credithaat) {
//		this.credithaat = credithaat;
//	}
	public String getHDB() {
		return HDB;
	}
	public void setHDB(String hDB) {
		HDB = hDB;
	}
	public String getKissht() {
		return Kissht;
	}
	public void setKissht(String kissht) {
		Kissht = kissht;
	}
	public String getABCL() {
		return ABCL;
	}
	public void setABCL(String aBCL) {
		ABCL = aBCL;
	}
	public String getBFL() {
		return BFL;
	}
	public void setBFL(String bFL) {
		BFL = bFL;
	}
	public String getCreditHaat() {
		return CreditHaat;
	}
	public void setCreditHaat(String creditHaat) {
		CreditHaat = creditHaat;
	}
	public String getPahal() {
		return Pahal;
	}
	public void setPahal(String pahal) {
		Pahal = pahal;
	}
	public String getAspire() {
		return Aspire;
	}
	public void setAspire(String aspire) {
		Aspire = aspire;
	}
    
    
    

    // Include new bank & card fields getters/setters as needed
    // ...
}