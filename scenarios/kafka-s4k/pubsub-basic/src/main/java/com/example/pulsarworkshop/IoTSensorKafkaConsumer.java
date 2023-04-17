package com.example.pulsarworkshop;

import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

public class IoTSensorKafkaSubscriber extends S4KCmdApp {
    // Must be set before initializing the "logger" object.
    private final static String APP_NAME = "IoTSensorKafkaSubscriber";
    static { System.setProperty("log_file_base_name", getLogFileName(API_TYPE, APP_NAME)); }
    private final static Logger logger = LoggerFactory.getLogger(IoTSensorKafkaSubscriber.class);
    private static Consumer<String, String> kafkaConsumer;
    private String consumerGroupName;

    public IoTSensorKafkaSubscriber(String appName, String[] inputParams) {
        super(appName, inputParams);
        addRequiredCommandLineOption("cg", "group.id",
                true, "Consumer group ID.");

        logger.info("Starting application: \"" + appName + "\" ...");
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new IoTSensorKafkaSubscriber(APP_NAME, args);
        int exitCode = workshopApp.run();
        System.exit(exitCode);
    }

    @Override
    public void processExtendedInputParams() throws InvalidParamException {
        super.processExtendedInputParams();

        // (Required) Subscription type identifier
        consumerGroupName = processStringInputParam("cg");
    }

    private KafkaConsumer<String, String> createKafkaConsumer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootStrapServerUrl);
        properties.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaConsumer<>(properties);
    }

    @Override
    public void runApp() throws WorkshopRuntimException {
        try {
            if (kafkaConsumer == null) {
                kafkaConsumer = createKafkaConsumer();
            }

            int msgRecvd = 0;
            if (numMsg == -1) {
                numMsg = Integer.MAX_VALUE;
            }

            while (msgRecvd < numMsg) {
                Message message = jmsConsumer.receive();
                if (logger.isDebugEnabled()) {
                    logger.debug(">>> Message received from topic {} (msg-payload={})",
                            topicDestination.getTopicName(), message.getBody(String.class));
                }

                msgRecvd++;
            }
        }
        catch (JMSException jmsException) {
            throw new WorkshopRuntimException("Unexpected error when consuming JMS messages from a topic! " + jmsException.getMessage());
        }
    }

    @Override
    public void termApp() {
        try {
            kafkaConsumer.close();
        }
        finally {
            logger.info("Terminating application: \"" + appName + "\" ...");
        }
    }
}
