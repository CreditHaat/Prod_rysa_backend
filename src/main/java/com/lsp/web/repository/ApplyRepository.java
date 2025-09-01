package com.lsp.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsp.web.entity.Apply;
import com.lsp.web.entity.UserInfo;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ApplyRepository extends JpaRepository<Apply, Integer> {
    Optional<Apply> findByUser(UserInfo user);
    
    Optional<Apply> findByUserAndProductName(UserInfo user, String productName);
    
//    Optional<Apply> findByApplyPhoneAndProductNameAndStatusNotInAndApplyTimeGreaterThan(
//            String applyPhone, String productName, List<Integer> statusNotIn, Date applyTime);
    
//    Optional<Apply> findByUserAndProductNameAndStatusNotInAndApplyTimeGreaterThan(
//            UserInfo user, String productName, List<Integer> statusNotIn, Date applyTime);

}


