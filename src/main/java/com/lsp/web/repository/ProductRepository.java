package com.lsp.web.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsp.web.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
	Optional<Product> findByProductName(String name);
	List<Product> findByStatus(int status);
//	Optional<Product> findByProductNameAndStatusAndNetpayAndSalary(String productName, int status, String netpayQuery, String salaryQuery);
//	Optional<Product> findByProductNameAndStatusAndOnlyNetpayAndOnlySalary(String productName, int status,  onlyNetpay, Integer onlySalary);

}
//public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
//	Optional<Product> findByProductName(String name);
////	Optional<Product> findByProductNameAndStatusAndNetpayAndSalary(String productName, int status, String netpayQuery, String salaryQuery);
//	Optional<Product> findByProductNameAndStatusAndOnlyNetpayAndOnlySalary(String productName, int status,  onlyNetpay, Integer onlySalary);
//
//
//}
