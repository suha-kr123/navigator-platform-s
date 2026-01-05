package com.nivasafinance.services.voice.provider.exotel.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExotelAddContactsCSVToListRequest {
    @JsonProperty("list_name")
    private String listName;
    @JsonProperty("file_name")
    private MultipartFile file;
}
