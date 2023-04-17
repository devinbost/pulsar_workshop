package com.example.pulsarworkshop;

import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class IoTSensorQueueReceiver extends S4JCmdApp {
    // Must be set before initializing the "logger" object.
    private final static String APP_NAME = "IoTSensorQueueReceiver";
    static { System.setProperty("log_file_base_name", getLogFileName(API_TYPE, APP_NAME)); }
    private final static Logger logger = LoggerFactory.getLogger(IoTSensorQueueReceiver.class);

    private static PulsarConnectionFactory connectionFactory;
    private static JMSContext jmsContext;
    private static JMSConsumer jmsConsumer;
    private static Queue queueDestination;

    public IoTSensorQueueReceiver(String appName, String[] inputParams) {
        super(appName, inputParams);

        logger.info("Starting application: \"" + appName + "\" ...");
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new IoTSensorQueueReceiver(APP_NAME, args);
        int exitCode = workshopApp.run();
        System.exit(exitCode);
    }

    @Override
    public void processExtendedInputParams() throws InvalidParamException {
        super.processExtendedInputParams();
    }

    @Override
    public void runApp() throws WorkshopRuntimException {
        try {
            if (connectionFactory == null) {
                connectionFactory = createPulsarJmsConnectionFactory();

                if (jmsContext == null) {
                    jmsContext = createJmsContext(connectionFactory);
                }

                if (queueDestination == null) {
                    queueDestination = createQueueDestination(jmsContext, topicName);
                    if (jmsConsumer == null) {
                        jmsConsumer = jmsContext.createConsumer(queueDestination);
                    }
                }
            }

            int msgRecvd = 0;
            if (numMsg == -1) {
                numMsg = Integer.MAX_VALUE;
            }

            while (msgRecvd < numMsg) {
                Message message = jmsConsumer.receive();
                logger.info("Message received from topic {}: value={}",
                        queueDestination.getQueueName(),
                        message.getBody(String.class));
                msgRecvd++;
            }
        }
        catch (JMSException jmsException) {
            throw new WorkshopRuntimException("Unexpected error when receiving JMS messages from a queue! " + jmsException.getMessage());
        }
    }

    @Override
    public void termApp() {
        try {
            if (jmsContext != null) {
                jmsContext.close();
            }

            if (connectionFactory != null) {
                connectionFactory.close();
            }
        }
        finally {
            logger.info("Terminating application: \"" + appName + "\" ...");
        }
    }
}
