package com.lsp.web.repository;

import com.lsp.web.entity.CompanyMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CompanyMasterRepository extends JpaRepository<CompanyMaster, Long> {

    @Query("SELECT c.companyName FROM CompanyMaster c " +
           "WHERE LOWER(c.companyName) LIKE LOWER(CONCAT(:query, '%')) " +
           "   OR LOWER(c.alias) LIKE LOWER(CONCAT(:query, '%')) " +
           "ORDER BY c.companyName ASC")
    List<String> findCompanyNamesByQuery(@Param("query") String query);
    
    Optional<CompanyMaster> findByCompanyName(String companyName);
    
}