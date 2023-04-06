package com.example.pulsarworkshop;

import com.example.pulsarworkshop.pojo.IoTSensorData;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.TypedMessageBuilder;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.apache.pulsar.functions.api.Record;
import org.slf4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AddMetadataFuncAvro implements Function<IoTSensorData, Void> {
    private static DateFormat timeFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS");
    private static TypedMessageBuilder<IoTSensorData> outputMessageBuilder;
    private static Logger LOG;

    @Override
    public void initialize(Context context) throws PulsarClientException {
        LOG = context.getLogger();

        String outputTopic = context.getOutputTopic();
        outputMessageBuilder = context.newOutputMessage(outputTopic, Schema.AVRO(IoTSensorData.class));
    }


    @Override
    public Void process(IoTSensorData input, Context context) {
        Record<IoTSensorData> currentRecord = (Record<IoTSensorData>) context.getCurrentRecord();
        Optional<String> keyOpt = currentRecord.getKey();
        Map<String, String> msgProperties = currentRecord.getProperties();

        if (keyOpt.isPresent()) {
            outputMessageBuilder.key(keyOpt.get());
        }
        for (String propKey : msgProperties.keySet()) {
            outputMessageBuilder.property(propKey, msgProperties.get(propKey));
        }
        // a newly added custom metadata
        outputMessageBuilder.property("internal_process_time",
                timeFormat.format(Calendar.getInstance().getTime()) );

        outputMessageBuilder.value(input);

        outputMessageBuilder.sendAsync();

        return null;
    }

    @Override
    public void close() {
    }
}
