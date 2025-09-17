package services.voice.provider.exotel.data

data class ExotelConfiguration(
    val accountSid: String,
    val authToken: String,
    val subdomain: String,
    val callerId: String,
    val webhookUrl: String,
    val apiKey: String = "",
    val apiToken: String = "",
    val recordingEnabled: Boolean = true,
    val maxCallDuration: Int = 3600,
    val retryAttempts: Int = 3,
    val timeout: Int = 30
)
