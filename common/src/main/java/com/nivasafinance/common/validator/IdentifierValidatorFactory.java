package com.nivasafinance.common.validator;

import com.nivasafinance.common.enums.IdentifierType;
import com.nivasafinance.common.validator.impl.PanIdentifierValidator;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class IdentifierValidatorFactory {

    private final Map<IdentifierType, IdentifierValidator> validators = new EnumMap<>(IdentifierType.class);

    public IdentifierValidatorFactory(MessageSource messageSource) {
        validators.put(IdentifierType.PAN, new PanIdentifierValidator(messageSource));
    }

    public IdentifierValidator getValidator(IdentifierType type) {
        return validators.get(type);
    }
}
