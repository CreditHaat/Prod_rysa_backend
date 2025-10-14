package com.lsp.web.repository;

import com.lsp.web.entity.UserEngagementLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEngagementLogRepository extends JpaRepository<UserEngagementLog, Long> {

    // find all logs by mobile number
    List<UserEngagementLog> findByMobileNumber(String mobileNumber);

    // find the latest log by mobile number
    UserEngagementLog findTopByMobileNumberOrderByLastAttributionTimeDesc(String mobileNumber);

    // find all logs by userId (foreign key UserInfo)
    List<UserEngagementLog> findByUser_Id(Long userInfoId);

    // check if user has logs after a specific time
    List<UserEngagementLog> findByMobileNumberAndLastAttributionTimeAfter(String mobileNumber, java.time.LocalDateTime time);
}
