package com.lsp.web.controller;

import com.lsp.web.ONDCService.CompanyMasterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
@CrossOrigin(origins = "*")
public class CompanyMasterController {

    private final CompanyMasterService service;

    public CompanyMasterController(CompanyMasterService service) {
        this.service = service;
    }

    @GetMapping("/getCompanyNames")
    public List<String> getCompanyNames(@RequestParam(required = false) String query) {
        return service.getCompanyNames(query);
    }
}