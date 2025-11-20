package com.nivasafinance.services.voice.provider.exotel.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "TwilioResponse")
public class ExotelCallResponse {

    @XmlElement(name = "Call")
    private Call call;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Call {
        @XmlElement(name = "Sid")
        private String sid;

        @XmlElement(name = "Status")
        private String status;

        @XmlElement(name = "StartTime")
        private String startTime;

        @XmlElement(name = "EndTime")
        private String endTime;

        @XmlElement(name = "Duration")
        private String duration;
    }
}
