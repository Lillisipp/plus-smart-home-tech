package kafka.serializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer {
    private final DecoderFactory decoderFactory;
    private final SpecificDatumReader<T> reader;


    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public Object deserialize(String topic, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            log.error("Avro deserialize error: topic={}, bytes={}", topic, bytes.length, e);
            return null;
        }
    }
}
