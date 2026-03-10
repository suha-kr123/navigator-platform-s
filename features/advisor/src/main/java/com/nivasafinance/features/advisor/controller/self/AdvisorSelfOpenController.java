package com.nivasafinance.features.advisor.controller.self;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisor.dto.self.SelfSendOtpRequest;
import com.nivasafinance.features.advisor.dto.self.SelfVerifyOtpRequest;
import com.nivasafinance.features.advisor.dto.self.SelfVerifyOtpResponse;
import com.nivasafinance.features.advisor.service.self.AdvisorSelfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.OPEN_API_V1 + "/advisors")
@RequiredArgsConstructor
public class AdvisorSelfOpenController {

    private final AdvisorSelfService advisorSelfService;

    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendOtp(@RequestBody @Valid SelfSendOtpRequest request) {
        advisorSelfService.sendOtp(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<SelfVerifyOtpResponse> verifyOtp(@RequestBody @Valid SelfVerifyOtpRequest request) {
        return ResponseEntity.ok(advisorSelfService.verifyOtp(request));
    }
}
