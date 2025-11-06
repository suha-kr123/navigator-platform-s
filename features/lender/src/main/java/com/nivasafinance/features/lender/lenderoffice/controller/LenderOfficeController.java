package com.nivasafinance.features.lender.lenderoffice.controller;

import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lender/{lenderKey}/office")
public class LenderOfficeController {

    private final LenderOfficeReadService lenderOfficeReadService;

    @Autowired
    public LenderOfficeController(LenderOfficeReadService lenderOfficeReadService) {
        this.lenderOfficeReadService = lenderOfficeReadService;
    }

    @GetMapping
    public List<LenderOfficeReponseData> getOfficesByLender(
            @PathVariable String lenderKey,
            @RequestParam(defaultValue = "ACTIVE") LenderOfficeStatus status) {
        return lenderOfficeReadService.getByLenderKeyAndStatus(lenderKey, status);
    }
}

