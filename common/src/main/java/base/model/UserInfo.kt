package base.model

data class UserInfo(
    val username: String,
    val email: String,
    val phoneNumber: String,
    val roles: List<String> = emptyList()
)
