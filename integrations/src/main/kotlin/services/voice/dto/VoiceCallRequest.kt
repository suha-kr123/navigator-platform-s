package services.voice.dto

data class VoiceCallRequest(
    val fromNumber: String,
    val toNumber: String,
    val exophone: String? = null,
    val entityName: String? = null,
    val entityId: Long? = null,
    val callPurpose: String? = null
)
