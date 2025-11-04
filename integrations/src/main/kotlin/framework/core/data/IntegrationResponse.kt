package framework.core.data

import org.springframework.http.HttpStatus

data class IntegrationResponse(
    val responseStatus: IntegrationResponseStatus,
    val httpStatus: HttpStatus?,
    val responseBody: String?,
    val errorMessage: String?
) {
    fun isSuccess(): Boolean {
        return IntegrationResponseStatus.SUCCESS == responseStatus
    }

    companion object {
        @JvmStatic
        fun successResponse(httpStatus: HttpStatus, responseBody: String?): IntegrationResponse {
            return IntegrationResponse(IntegrationResponseStatus.SUCCESS, httpStatus, responseBody, null)
        }

        @JvmStatic
        fun serverErrorResponse(httpStatus: HttpStatus, responseBody: String?): IntegrationResponse {
            return IntegrationResponse(IntegrationResponseStatus.SERVER_ERROR, httpStatus, responseBody, null)
        }

        @JvmStatic
        fun clientErrorResponse(httpStatus: HttpStatus, responseBody: String?): IntegrationResponse {
            return IntegrationResponse(IntegrationResponseStatus.CLIENT_ERROR, httpStatus, responseBody, null)
        }

        @JvmStatic
        fun serverErrorResponse(error: String?): IntegrationResponse {
            return IntegrationResponse(IntegrationResponseStatus.SERVER_ERROR, null, null, error)
        }

        @JvmStatic
        fun clientErrorResponse(error: String?): IntegrationResponse {
            return IntegrationResponse(IntegrationResponseStatus.CLIENT_ERROR, null, null, error)
        }
    }
}
