package com.nivasafinance.services.voice.provider.exotel.data;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExotelCallRequest {

    private String from;
    private String to;
    private String callerId;
    @Builder.Default
    private boolean record = true;
    private String customField;
    private String statusCallback;
    @Builder.Default
    private List<String> statusCallbackEvents = new ArrayList<>();
}
