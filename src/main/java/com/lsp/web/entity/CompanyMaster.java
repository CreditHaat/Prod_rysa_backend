package com.lsp.web.entity;

import com.lsp.web.genericentity.BaseEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "t_company_master")
public class CompanyMaster extends BaseEntity{
	
    @Column(name = "company_name", nullable = false, unique = true)
    private String companyName;
    
    @Column(name = "alias")
    private String alias;
    
    @Column(name = "category")
    private String category;
    public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	// Constructors
    public CompanyMaster() {}

    public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getAlias() {
		return alias;
	}
	

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public CompanyMaster(String companyName) {
        this.companyName = companyName;
    }

}