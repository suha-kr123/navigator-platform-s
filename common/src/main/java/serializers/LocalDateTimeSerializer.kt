package serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Custom Jackson serializer for LocalDate that formats dates as "DD-MM-YYYY".
 */
class LocalDateTimeSerializer : JsonSerializer<LocalDateTime>() {

    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

    override fun serialize(value: LocalDateTime?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value != null) {
            gen.writeString(formatter.format(value))
        } else {
            gen.writeNull()
        }
    }
}
