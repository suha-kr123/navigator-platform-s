package serializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Custom Jackson serializer for LocalDate that formats dates as "DD-MM-YYYY".
 */
class LocalDateTimeDeSerializer : JsonDeserializer<LocalDateTime>() {

    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): LocalDateTime? {
        return p?.valueAsString?.let { LocalDateTime.parse(it, formatter) }
    }
}
