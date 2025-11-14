package com.lsp.web.entity;

import com.lsp.web.genericentity.BaseEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "t_Agent")
public class Agent extends BaseEntity {

    @Column(name = "agent_name", nullable = false ,length = 50)
    private String agentName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "products",length = 100)
    private String products;

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getProducts() {
		return products;
	}

	public void setProducts(String products) {
		this.products = products;
	}




}
