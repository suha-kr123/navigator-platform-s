package serializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Custom Jackson serializer for LocalDate that formats dates as "DD-MM-YYYY".
 */
class LocalDateDeSerializer : JsonDeserializer<LocalDate>() {

    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): LocalDate? {
        return p?.valueAsString?.let { LocalDate.parse(it, formatter) }
    }
}
