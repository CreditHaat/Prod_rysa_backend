package com.lsp.web.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lsp.web.entity.*;

@Repository
public interface UserBureauDataRepository extends JpaRepository<UserBureauData, Long> {
    Optional<UserBureauData> findByUserId(Long userId);
    Optional<UserBureauData> findByMobileNumber(String mobileNumber);
    
// // Using Spring Data JPA method naming
//    Optional<UserBureauData> findTopByMobileNumberOrderByCreateTimeDesc();

    // OR, using JPQL explicitly
    @Query("SELECT u FROM UserBureauData u WHERE u.mobileNumber = :phone ORDER BY u.createTime DESC")
    Optional<UserBureauData> findLatestByPhone(String phone);
}