package kafka.serializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Slf4j
public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        log.debug("Configure GeneralAvroSerializer. isKey={}", isKey);
    }

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

            writer.write(data, encoder);
            encoder.flush();

            byte[] bytes = out.toByteArray();
            log.debug("Avro serialized: topic={}, schema={}, sizeBytes={}",
                    topic, data.getSchema().getFullName(), bytes.length);
            return bytes;
        } catch (Exception e) {
            log.error("Avro serialization failed: topic={}, schema={}",
                    topic, data.getSchema().getFullName(), e);
            throw new SerializationException("Avro serialization failed for topic=" + topic, e);
        }
    }

    @Override
    public void close() {
        log.debug("Close GeneralAvroSerializer");
    }

}
