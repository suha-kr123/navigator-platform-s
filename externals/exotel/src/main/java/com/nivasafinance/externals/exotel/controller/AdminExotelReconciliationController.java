package com.nivasafinance.externals.exotel.controller;

import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.externals.exotel.dto.ExotelReconciliationSummary;
import com.nivasafinance.externals.exotel.service.ExotelReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Manual Exotel call-log reconciliation for a calendar date range (backfill / debugging).
 */
@RestController
@RequestMapping(ApiConstants.V1 + "/admin/exotel/reconciliation")
@RequiredArgsConstructor
public class AdminExotelReconciliationController {

    private final ExotelReconciliationService exotelReconciliationService;

    @PostMapping("/run")
    @RequireRole({"ADMIN"})
    public ResponseEntity<ExotelReconciliationSummary> run(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            return ResponseEntity.ok(exotelReconciliationService.reconcileDateRange(start, end));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage() != null ? e.getMessage() : "Invalid date range");
        }
    }
}
