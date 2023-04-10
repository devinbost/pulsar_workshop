package com.example.pulsarworkshop;

import com.example.pulsarworkshop.pojo.IoTSensorData;
import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import org.apache.commons.cli.Option;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.shade.org.apache.commons.lang3.StringUtils;
import org.bouncycastle.math.raw.Nat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoTSensorConsumerAvro extends NativePulsarCmdApp {

    private final static Logger logger = LoggerFactory.getLogger(IoTSensorConsumerAvro.class);

    private String subscriptionName;
    private SubscriptionType subscriptionType = SubscriptionType.Exclusive;

    private PulsarClient pulsarClient;

    private Consumer<IoTSensorData> pulsarConsumer;

    public IoTSensorConsumerAvro(String appName, String[] inputParams) {
        super(appName, inputParams);
        addCommandLineOption(new Option("sbt","subType", true, "Pulsar subscription type."));
        addCommandLineOption(new Option("sbn", "subName", true, "Pulsar subscription name."));
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new IoTSensorConsumerAvro("IoTSensorConsumerAvro", args);
        int exitCode = workshopApp.run();
        System.exit(exitCode);
    }

    @Override
    public void processExtendedInputParams() throws InvalidParamException {
        // (Required) Pulsar subscription name
        subscriptionName = processStringInputParam("sbn");
        if ( StringUtils.isBlank(subscriptionName) ) {
            throw new InvalidParamException("Must provide a subscription name for a consumer!");
        }

        // (Optional) Pulsar subscription type
        String subType = processStringInputParam("sbt");
        if (!StringUtils.isBlank(subType)) {
        try {
	            subscriptionType = SubscriptionType.valueOf(subType);
	        }
	        catch (IllegalArgumentException iae) {
	            subscriptionType = SubscriptionType.Exclusive;
	        }
        }
    }

    @Override
    public void runApp() {
        try {
            pulsarClient = createNativePulsarClient();

            ConsumerBuilder<IoTSensorData> consumerBuilder = pulsarClient.newConsumer(Schema.AVRO(IoTSensorData.class));
            consumerBuilder.topic(pulsarTopicName);
            consumerBuilder.subscriptionName(subscriptionName);
            consumerBuilder.subscriptionType(subscriptionType);
            pulsarConsumer = consumerBuilder.subscribe();

            int msgRecvd = 0;
            if (numMsg == -1) {
                numMsg = Integer.MAX_VALUE;
            }

            while (msgRecvd < numMsg) {
                Message<IoTSensorData> message = pulsarConsumer.receive();
                IoTSensorData sensorData = message.getValue();

                if (logger.isDebugEnabled()) {
                    logger.debug(">>> ({}) Message received and acknowledged: " +
                                    "msg-key={}; msg-properties={}; msg-payload={}",
                            pulsarConsumer.getConsumerName(),
                            message.getKey(),
                            message.getProperties(),
                            sensorData);
                }
                pulsarConsumer.acknowledge(message);

                msgRecvd++;
            }

        }
        catch (PulsarClientException pce) {
            throw new WorkshopRuntimException("Unexpected error when consuming Pulsar messages: " + pce.getMessage());
        }
    }

    @Override
    public void termApp() {
        try {
            if (pulsarConsumer != null) {
                pulsarConsumer.close();
            }

            if (pulsarClient != null) {
                pulsarClient.close();
            }
        }
        catch (PulsarClientException pce) {
            throw new WorkshopRuntimException("Failed to terminate Pulsar producer or client!");
        }
    }
}
