package com.lsp.web.ONDCService;

import com.lsp.web.repository.CompanyMasterRepository;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class CompanyMasterService {

    private final CompanyMasterRepository repository;

    public CompanyMasterService(CompanyMasterRepository repository) {
        this.repository = repository;
    }

    public List<String> getCompanyNames(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findCompanyNamesByQuery(query.trim().toLowerCase());
    }
}