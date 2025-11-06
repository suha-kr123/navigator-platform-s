package framework.core.data;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationResponse {
    private IntegrationResponseStatus responseStatus;
    private HttpStatus httpStatus;
    private String responseBody;
    private String errorMessage;

    public boolean isSuccess() {
        return IntegrationResponseStatus.SUCCESS == responseStatus;
    }

    public static IntegrationResponse successResponse(HttpStatus httpStatus, String responseBody) {
        return new IntegrationResponse(IntegrationResponseStatus.SUCCESS, httpStatus, responseBody, null);
    }

    public static IntegrationResponse serverErrorResponse(HttpStatus httpStatus, String responseBody) {
        return new IntegrationResponse(IntegrationResponseStatus.SERVER_ERROR, httpStatus, responseBody, null);
    }

    public static IntegrationResponse clientErrorResponse(HttpStatus httpStatus, String responseBody) {
        return new IntegrationResponse(IntegrationResponseStatus.CLIENT_ERROR, httpStatus, responseBody, null);
    }

    public static IntegrationResponse serverErrorResponse(String error) {
        return new IntegrationResponse(IntegrationResponseStatus.SERVER_ERROR, null, null, error);
    }

    public static IntegrationResponse clientErrorResponse(String error) {
        return new IntegrationResponse(IntegrationResponseStatus.CLIENT_ERROR, null, null, error);
    }
}

