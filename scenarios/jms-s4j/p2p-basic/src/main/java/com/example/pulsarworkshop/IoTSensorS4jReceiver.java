package com.example.pulsarworkshop;

import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class IoTSensorS4jReceiver extends S4JCmdApp {
    private final static Logger logger = LoggerFactory.getLogger(IoTSensorS4jReceiver.class);

    private static PulsarConnectionFactory connectionFactory;
    private static JMSContext jmsContext;
    private static JMSConsumer jmsConsumer;
    private static Destination queueDestination;

    public IoTSensorS4jReceiver(String appName, String[] inputParams) {
        super(appName, inputParams);
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new IoTSensorS4jReceiver("IoTSensorS4jReceiver", args);
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
                    queueDestination = createQueueDestination(jmsContext, pulsarTopicName);
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
                if (logger.isDebugEnabled()) {
                    logger.debug(">>> Message received and acknowledged: msg-payload={}",
                            message.getBody(String.class));
                }

                msgRecvd++;
            }
        }
        catch (JMSException jmsException) {
            throw new WorkshopRuntimException("Unexpected error when consuming a JMS message: " + jmsException.getMessage());
        }
    }

    @Override
    public void termApp() {
        if (jmsContext != null) {
            jmsContext.close();
        }

        if (connectionFactory != null) {
            connectionFactory.close();
        }
    }
}
