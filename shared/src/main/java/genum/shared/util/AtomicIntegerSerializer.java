package genum.shared.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerSerializer extends JsonSerializer<AtomicInteger> {

    @Override
    public void serialize(AtomicInteger value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(value.get());
    }
}
