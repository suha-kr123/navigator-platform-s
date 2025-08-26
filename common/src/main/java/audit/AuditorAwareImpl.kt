package audit

import base.context.UserContext
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuditorAwareImpl : AuditorAware<String> {
    companion object {
        private const val MAX_USERNAME_LENGTH = 255
    }

    override fun getCurrentAuditor(): Optional<String> {
        val username = UserContext.getUserInfo()?.username ?: "system"
        // Truncate username to 255 characters to prevent database issues
        return Optional.of(username.take(MAX_USERNAME_LENGTH))
    }
}
