package com.lsp.web.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lsp.web.entity.MIS;

@Repository
public interface MISRepository extends JpaRepository<MIS, Long> {
	
	public Optional<MIS> findByMobileNumber(String mobileNumber);
	
	public Optional<MIS> findByTransactionId(String transactionId);

	
	List<MIS> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
	
	List<MIS> findAllByMobileNumberOrderByCreateTimeDesc(String mobileNumber);
	
	// Find latest MIS for a mobile that is not canceled
	Optional<MIS> findTopByMobileNumberAndCancelFlagNotOrderByCreateTimeDesc(String mobileNumber, String cancelFlag);
	
	Optional<MIS> findTopByMobileNumberOrderByCreateTimeDesc(String mobileNumber);



}
