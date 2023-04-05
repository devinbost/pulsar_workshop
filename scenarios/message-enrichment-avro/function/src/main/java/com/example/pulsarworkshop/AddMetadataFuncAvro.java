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


    @Override
    public Void process(IoTSensorData input, Context context) throws Exception {

        Logger LOG = context.getLogger();
        String outputTopic = context.getOutputTopic();

        String inputTopics = context.getInputTopics().stream().collect(Collectors.joining(", "));
        String logMessage = String.format(
                "A message with a value of \"%s\" has arrived on one of the following topics: %s\n",
                input,
                inputTopics);
        LOG.info(logMessage);

        try {
            Record<IoTSensorData> currentRecord = (Record<IoTSensorData>) context.getCurrentRecord();

            Optional<String> keyOpt = currentRecord.getKey();
            Map<String, String> msgProperties = currentRecord.getProperties();

            TypedMessageBuilder<IoTSensorData> messageBuilder
                    = context.newOutputMessage(outputTopic, Schema.AVRO(IoTSensorData.class));
            if (keyOpt.isPresent()) {
                messageBuilder.key(keyOpt.get());
            }

            for (String propKey : msgProperties.keySet()) {
                messageBuilder.property(propKey, msgProperties.get(propKey));
            }

            // a newly added custom metadata
            messageBuilder.property("internal_process_time",
                    timeFormat.format(Calendar.getInstance().getTime()) );

            messageBuilder.value(input);

            MessageId messageId = messageBuilder.send();
        }
        catch (PulsarClientException e) {
            LOG.error(e.toString());
        }

        return null;
    }
}
