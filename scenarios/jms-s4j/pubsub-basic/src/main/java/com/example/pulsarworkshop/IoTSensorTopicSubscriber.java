package com.example.pulsarworkshop;

import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class IoTSensorTopicSubscriber extends S4JCmdApp {
    private final static Logger logger = LoggerFactory.getLogger(IoTSensorTopicSubscriber.class);

    private static PulsarConnectionFactory connectionFactory;
    private static JMSContext jmsContext;
    private static JMSConsumer jmsConsumer;
    private static Topic topicDestination;

    /**
     * Valid subscription types
     * - nsd: non-shared, non-durable
     * - s  : shared, non-durable
     * - d  : non-shared, durable
     * - sd : shared, durable
     */
    private static final String[] VALID_SUB_TYPES = {"nsd", "s", "d", "sd"};
    private static String subType = "nsd";

    public IoTSensorTopicSubscriber(String appName, String[] inputParams) {
        super(appName, inputParams);
        addOptionalCommandLineOption(
                "st","subType", true,
                "Subscriber type: nsd (Non-Shared/Non-Durable), s (Shared), d (Durable), sd (DurableShared)");
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new IoTSensorTopicSubscriber("IoTSensorTopicSubscriber", args);
        int exitCode = workshopApp.run();
        System.exit(exitCode);
    }

    @Override
    public void processExtendedInputParams() throws InvalidParamException {
        super.processExtendedInputParams();

        // (Optional) Subscription type identifier
        subType = processStringInputParam("st", subType);
    }

    @Override
    public void runApp() throws WorkshopRuntimException {
        try {
            if (connectionFactory == null) {
                connectionFactory = createPulsarJmsConnectionFactory();

                if (jmsContext == null) {
                    jmsContext = createJmsContext(connectionFactory);
                }

                if (topicDestination == null) {
                    topicDestination = createTopicDestination(jmsContext, pulsarTopicName);
                    if (jmsConsumer == null) {

                        // use a random alphanumeric string as the subscription name
                        String subName = RandomStringUtils.randomAlphabetic(20);

                        // nsd
                        if (StringUtils.equalsIgnoreCase(subType, VALID_SUB_TYPES[0]))
                            jmsConsumer = jmsContext.createConsumer(topicDestination);
                        // s
                        else if (StringUtils.equalsIgnoreCase(subType, VALID_SUB_TYPES[1]))
                            jmsConsumer = jmsContext.createSharedConsumer(topicDestination, subName);
                        // d
                        else if (StringUtils.equalsIgnoreCase(subType, VALID_SUB_TYPES[2]))
                            jmsConsumer = jmsContext.createDurableConsumer(topicDestination, subName);
                        // sd
                        else if (StringUtils.equalsIgnoreCase(subType, VALID_SUB_TYPES[3]))
                            jmsConsumer = jmsContext.createSharedDurableConsumer(topicDestination, subName);
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
                    logger.debug(">>> Message received from queue {} (msg-payload={})",
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
        if (jmsContext != null) {
            jmsContext.close();
        }

        if (connectionFactory != null) {
            connectionFactory.close();
        }
    }
}
