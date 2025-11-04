package services.voice.dto

data class VoiceCallResponse(
    val callSid: String,
    val status: String,
    val fromNumber: String,
    val toNumber: String,
    val duration: Int? = null,
    val recordingUrl: String? = null,
    val errorMessage: String? = null
)
