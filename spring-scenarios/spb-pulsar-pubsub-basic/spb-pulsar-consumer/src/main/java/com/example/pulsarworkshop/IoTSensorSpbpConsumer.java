package com.example.pulsarworkshop;

import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.pojo.IoTSensorData;
import com.example.pulsarworkshop.util.SpringPulsarCmdAppUtils;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.schema.SchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.pulsar.core.ConsumerBuilderCustomizer;
import org.springframework.pulsar.listener.AckMode;

@SpringBootApplication
public class IoTSensorSpbpConsumer implements CommandLineRunner  {
    private final static String API_TYPE = "spring-boot-pulsar";
    private final static String APP_NAME = "IoTSensorSpbpConsumer";

    // Must be set before initializing the "logger" object.
    static { System.setProperty("log_file_base_name", SpringPulsarCmdAppUtils.getLogFileName(API_TYPE, APP_NAME)); }
    private final static Logger logger = LoggerFactory.getLogger(IoTSensorSpbpConsumer.class);

    /**
     * Spring boot Pulsar consumer doc:
     *   https://docs.spring.io/spring-pulsar/docs/current-SNAPSHOT/reference/html/#_message_consumption
     */

    private static int numMessages;

    @Value("${spbp-pubsub.num-msg}")
    public void setNameStatic(int num){
        IoTSensorSpbpConsumer.numMessages = num;
    }

    @Value("${spbp-pubsub.topic}")
    private String topic;

    //@Value("${spring.pulsar.consumer.subscription-type}")
    //private String subTypeStr;

    private static int totalMsgReceived;

    public static void main(String[] args) {
        logger.info("Starting Spring boot Pulsar consumer application 'IoTSensorSpbpConsumer'");

        ConfigurableApplicationContext ctx = null;
        try {
            ctx = SpringApplication.run(IoTSensorSpbpConsumer.class, args);
        }
        catch (InvalidParamException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid configuration value detected: " + ex.getMessage());
            }
            ex.printStackTrace();
        }
        finally {
            if (ctx != null) {
                while ( (numMessages == -1) || (totalMsgReceived < numMessages) ) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }

                ctx.close();
                logger.info("Existing Spring boot Pulsar consumer application 'IoTSensorSpbpConsumer'");
                logger.info("Total message received: {}\n", totalMsgReceived);
            }
        }
    }

    /*
     * Causing the following error:
     *   Error creating bean with name 'ioTSensorSpbpConsumer': Requested bean is currently in creation: Is there an unresolvable circular reference?
    ----------------------------------------
    @Bean
    public ConsumerBuilderCustomizer<IoTSensorData> myConsumerCustomizer() {
        return cb -> {
            cb.subscriptionType(SubscriptionType.valueOf(subTypeStr));
        };
    }
    */

    @PulsarListener(
            topics = "${spbp-pubsub.topic}",
            subscriptionName = "${spring.pulsar.consumer.subscription-name}",
            //consumerCustomizer = "myConsumerCustomizer",
            subscriptionType = SubscriptionType.Exclusive,
            schemaType = SchemaType.AVRO,
            ackMode = AckMode.RECORD)
    public void listen(IoTSensorData message) {
        while ((numMessages == -1) || (totalMsgReceived++ < numMessages)) {
            logger.info("Successfully received message: msg-payload={})", message);
            totalMsgReceived++;
        }
    }

    @Override
    public void run(String... args) throws Exception {
        SpringPulsarCmdAppUtils.processNumMsgInputParam(numMessages);
        SpringPulsarCmdAppUtils.processTopicNameInputParam(topic);
        //SpringPulsarCmdAppUtils.processSubTypeInputParam(subTypeStr);

        logger.info(">> numMessages: {}, topic: {}", numMessages, topic);
    }
}
