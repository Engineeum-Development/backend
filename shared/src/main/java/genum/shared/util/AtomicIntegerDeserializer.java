package genum.shared.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerDeserializer extends JsonDeserializer<AtomicInteger> {

    @Override
    public AtomicInteger deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        return new AtomicInteger(p.getIntValue());
    }
}
