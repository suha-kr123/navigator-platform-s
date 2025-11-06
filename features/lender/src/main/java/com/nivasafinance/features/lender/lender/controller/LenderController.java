package com.nivasafinance.features.lender.lender.controller;

import com.nivasafinance.features.lender.lender.dto.LenderWithOfficesResponse;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import com.nivasafinance.features.lender.lender.service.LenderReadService;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/lender")
public class LenderController {

    private final LenderReadService lenderReadService;
    private final LenderOfficeReadService lenderOfficeReadService;

    @Autowired
    public LenderController(LenderReadService lenderReadService, LenderOfficeReadService lenderOfficeReadService) {
        this.lenderReadService = lenderReadService;
        this.lenderOfficeReadService = lenderOfficeReadService;
    }

    @GetMapping
    public List<LenderWithOfficesResponse> getAllLendersWithOffices() {
        return lenderReadService.getAllByStatus(LenderStatus.ACTIVE).stream()
                .map(lender -> {
                    List<com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData> offices =
                            lenderOfficeReadService.getByLenderKeyAndStatus(lender.getKey(), LenderOfficeStatus.ACTIVE);
                    return new LenderWithOfficesResponse(
                            lender.getName(),
                            lender.getKey(),
                            offices
                    );
                })
                .collect(Collectors.toList());
    }
}

