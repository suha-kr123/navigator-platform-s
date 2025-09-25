package configs

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Configuration
class AppConfig {
    @Bean
    fun modelMapper(): ModelMapper {
        val modelMapper = ModelMapper()

        // Configure ModelMapper for better compatibility with Kotlin data classes
        modelMapper.configuration.isSkipNullEnabled = true
        modelMapper.configuration.isAmbiguityIgnored = true
        modelMapper.configuration.matchingStrategy = org.modelmapper.convention.MatchingStrategies.STRICT
        modelMapper.configuration.isFieldMatchingEnabled = true
        modelMapper.configuration.fieldAccessLevel = org.modelmapper.config.Configuration.AccessLevel.PRIVATE

        return modelMapper
    }

    @Bean
    @org.springframework.context.annotation.Primary
    fun objectMapper(): ObjectMapper = ObjectMapper().apply {
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        val isoDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        val javaTimeModule = JavaTimeModule().apply {
            addSerializer(LocalDate::class.java, LocalDateSerializer(dateFormatter))
            addDeserializer(LocalDate::class.java, LocalDateDeserializer(dateFormatter))
            addSerializer(java.time.LocalTime::class.java, LocalTimeSerializer(timeFormatter))
            addDeserializer(java.time.LocalTime::class.java, LocalTimeDeserializer(timeFormatter))
            addSerializer(java.time.LocalDateTime::class.java, LocalDateTimeSerializer(dateTimeFormatter))
            addDeserializer(java.time.LocalDateTime::class.java, LocalDateTimeDeserializer(isoDateTimeFormatter))
        }
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        registerModule(javaTimeModule)
        // registerModule(ParameterNamesModule())
        registerModule(kotlinModule())
    }

    @Bean
    fun messageSource(): MessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasename("classpath:messages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

    @Bean
    fun localeResolver(): LocaleResolver =
        SessionLocaleResolver().apply {
            setDefaultLocale(Locale.ENGLISH)
        }
}
