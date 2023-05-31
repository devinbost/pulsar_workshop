package com.example.pulsarworkshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.Arrays;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

public class SplitterFunction implements Function<String, Void> {
    private Logger logger;
    private ObjectMapper mapper;

    private Schema<DeviceTS> schema;
    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.schema = Schema.AVRO(DeviceTS.class);
    }
    @Override
    public Void process(String input, Context context) throws Exception {
        var myObjects = Arrays.asList(mapper.readValue(input, DeviceTS[].class));
        // For production, we could create a Stream-based parser to improve performance.
        myObjects.forEach(entry -> {
            try {
                context.newOutputMessage(context.getOutputTopic(), schema).value(entry).sendAsync();
            } catch (PulsarClientException e) {
                context.getLogger().error(e.toString());
            }
        });
        return null;
    }

    @Override
    public void close() throws Exception {
        Function.super.close();
    }
}
