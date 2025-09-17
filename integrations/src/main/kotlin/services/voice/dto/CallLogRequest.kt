package services.voice.dto

data class CallLogRequest(
    val callSid: String,
    val entityName: String,
    val entityId: Long,
    val direction: String,
    val fromNumber: String,
    val toNumber: String,
    val status: String = "initiated",
    val exophone: String? = null
)
