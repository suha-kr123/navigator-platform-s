package configs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import serializers.LocalDateDeSerializer
import serializers.LocalDateSerializer
import serializers.LocalDateTimeDeSerializer
import serializers.LocalDateTimeSerializer
import java.time.LocalDate
import java.time.LocalDateTime
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
    fun objectMapper() = ObjectMapper().apply {
        val localDateModule = SimpleModule()
        localDateModule.addSerializer(LocalDate::class.java, LocalDateSerializer())
        localDateModule.addDeserializer(LocalDate::class.java, LocalDateDeSerializer())
        registerModule(localDateModule)
        val localDateTimeModule = SimpleModule()
        localDateTimeModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer())
        localDateTimeModule.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeSerializer())
        registerModule(localDateTimeModule)
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
