package com.lsp.web.repository;

import org.springframework.data.jpa.domain.Specification;

import com.lsp.web.entity.Product;

public class ProductSpecification {
	
	//how this class works line by line
	/*
	 * root → represents the entity (Product) in the query.
	
	query → represents the full query being built.
	
	cb (CriteriaBuilder) → used to build conditions (=, >, <, AND, OR, etc).
	
	cb.equal(root.get("productName"), productName) → generates SQL like:
	 */
	
	public static Specification<Product> hasProductName(String productName) {
        return (root, query, cb) -> cb.equal(root.get("productName"), productName); //WHERE product_name = :productName
    }

    public static Specification<Product> hasStatus(int status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Product> onlySalaryCondition(String profession) {
    	
    	
//    	Depending on profession:
//
//    		"Salaried" → onlySalary <> 2
//
//    		"Self Employed" → onlySalary = 0
//
//    		"Business" → onlySalary = 2
//
//    		Otherwise → cb.conjunction() (means "always true", so it doesn’t filter anything)
//
//    		So, SQL could look like:
//
//    		AND only_salary <> 2   -- if Salaried
//    		AND only_salary = 0    -- if Self Employed
//    		AND only_salary = 2    -- if Business
    	
        return (root, query, cb) -> {
            if ("Salaried".equalsIgnoreCase(profession)) {
                return cb.notEqual(root.get("onlySalary"), 2);
            } else if ("Self Employed".equalsIgnoreCase(profession)) {
                return cb.equal(root.get("onlySalary"), 0);
            } else if ("Business".equalsIgnoreCase(profession)) {
                return cb.equal(root.get("onlySalary"), 2);
            }
            return cb.conjunction(); // no condition
        };
    }

    public static Specification<Product> onlyNetPayCondition(Integer paymentType) {
        return (root, query, cb) -> {
            if (paymentType != null && paymentType == 2) {
                return cb.lessThan(root.get("onlyNetpay"), 2);
            } else if (paymentType != null) {
                return cb.equal(root.get("onlyNetpay"), 0);
            }
            return cb.conjunction();
        };
    }
    
}
