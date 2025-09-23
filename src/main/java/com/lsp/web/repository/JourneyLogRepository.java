//package com.lsp.web.repository;
//
//import com.lsp.web.entity.JourneyLog;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface JourneyLogRepository extends JpaRepository<JourneyLog, Integer> {
//
//    // Add any custom finders if you need them later:
//    // List<JourneyLog> findByApplyRecordId(Long applyRecordId);
//     Optional<JourneyLog> findByUId(String uid);
//     
//     List<JourneyLog> findFirstByUId(String uId);
//     
//     @Query("SELECT j FROM JourneyLog j WHERE j.UId = :UId AND j.stage = :stage ORDER BY j.createTime DESC")
//     List<JourneyLog> findByUIdAndStage(@Param("UId") String UId, @Param("stage") Integer stage);
//
//     List<JourneyLog> findByUIdOrderByCreateTimeDesc(String uId);
//
//}
//

package com.lsp.web.repository;

import com.lsp.web.entity.JourneyLog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JourneyLogRepository extends JpaRepository<JourneyLog, Integer> {

    // Add any custom finders if you need them later:
    // List<JourneyLog> findByApplyRecordId(Long applyRecordId);
     Optional<JourneyLog> findByUId(String uid);
     
     List<JourneyLog> findFirstByUId(String uId);
     
     @Query("SELECT j FROM JourneyLog j WHERE j.UId = :UId AND j.stage = :stage ORDER BY j.createTime DESC")
     List<JourneyLog> findByUIdAndStage(@Param("UId") String UId, @Param("stage") Integer stage);

     List<JourneyLog> findByUIdOrderByCreateTimeDesc(String uId);
     
//     SELECT * FROM t_journey_log WHERE user_id=1 AND request_id = 'https://prod.gateway.ondc.org/search' order by createtime DESC LIMIT 1;
//     @Query("select j from JourneyLog j where j.user.id = :userId AND j.requestId = 'https://prod.gateway.ondc.org/search' ORDER BY createTime DESC LIMIT 1") //here we can't user this limit we will need to user pageable to instead of limit
//     Optional<JourneyLog> findByUser(@Param("userId") Long userId);
     
     @Query(
    		  value = "SELECT * FROM t_journey_log j WHERE j.user_id = :userId AND j.request_id = 'https://prod.gateway.ondc.org/search' ORDER BY j.createtime DESC LIMIT 1",
    		  nativeQuery = true
    		)
    		Optional<JourneyLog> findByUser(@Param("userId") Long userId);
     
}


