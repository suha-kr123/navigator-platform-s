package com.nivasafinance.services.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.InputStream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoiceCreateListRequest {
    private String name;
    private InputStream file;
}
