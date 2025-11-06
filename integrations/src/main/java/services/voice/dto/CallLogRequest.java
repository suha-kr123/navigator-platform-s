package services.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallLogRequest {
    private String callSid;
    private String entityName;
    private Long entityId;
    private String direction;
    private String fromNumber;
    private String toNumber;
    private String status = "initiated";
    private String exophone;
}

