package com.nivasafinance.features.call.enums;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Deserializes {@link AtlasJobStatus} from JSON strings.
 * Unknown values deserialize as {@code null}.
 */
public final class AtlasJobStatusDeserializer extends JsonDeserializer<AtlasJobStatus> {

    @Override
    public AtlasJobStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == null || t == JsonToken.VALUE_NULL) {
            return null;
        }
        return AtlasJobStatus.fromWireOrLegacy(p.getValueAsString());
    }
}
