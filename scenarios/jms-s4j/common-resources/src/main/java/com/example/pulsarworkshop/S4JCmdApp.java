package com.example.pulsarworkshop;

import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import com.example.pulsarworkshop.exception.InvalidParamException;
import org.apache.commons.lang3.StringUtils;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.HashMap;
import java.util.Map;

abstract public class S4JCmdApp extends PulsarWorkshopCmdApp {
    protected final static String API_TYPE = "jms-s4j";
    protected int jmsSessionMode = JMSContext.AUTO_ACKNOWLEDGE;

    public S4JCmdApp(String appName, String[] inputParams) {
        super(appName, inputParams);
        // Add the S4J/JMS specific CLI options that are common to all S4J/JMS client applications
        addOptionalCommandLineOption("s","session", true, "JMS session mode");
    }

    @Override
    public void processExtendedInputParams() throws InvalidParamException {
        // (Optional) JMS session mode. If not specified, use the default JMSContext.AUTO_ACKNOWLEDGE
        if (cliOptions.hasOption("s")) {
            int inputSessionMode = processIntegerInputParam("s");
            if (inputSessionMode != -1) {
                jmsSessionMode = inputSessionMode;
            }

            if ( (jmsSessionMode < JMSContext.SESSION_TRANSACTED) || (jmsSessionMode > JMSContext.DUPS_OK_ACKNOWLEDGE) ) {
                throw new InvalidParamException("Must provide a valid JMS session mode value!");
            }
        }
    }

    public PulsarConnectionFactory createPulsarJmsConnectionFactory(){
        return createPulsarJmsConnectionFactory(new HashMap<>());
    }

    public PulsarConnectionFactory createPulsarJmsConnectionFactory(Map<String, Object> cfgMap)  {
        Map<String, Object> jmsConnMap = new HashMap<>(cfgMap);

        jmsConnMap.put("webServiceUrl", clientConnConf.getValue("webServiceUrl"));
        jmsConnMap.put("brokerServiceUrl", clientConnConf.getValue("brokerServiceUrl"));
        jmsConnMap.put("authPlugin", clientConnConf.getValue("authPlugin"));
        jmsConnMap.put("authParams", clientConnConf.getValue("authParams"));
        jmsConnMap.put("tlsEnableHostnameVerification", clientConnConf.getValue("tlsEnableHostnameVerification"));
        jmsConnMap.put("tlsTrustCertsFilePath", clientConnConf.getValue("tlsTrustCertsFilePath"));
        jmsConnMap.put("tlsAllowInsecureConnection", clientConnConf.getValue("tlsAllowInsecureConnection"));

        return new PulsarConnectionFactory(jmsConnMap);
    }

    public JMSContext createJmsContext(PulsarConnectionFactory factory) {
        assert (factory != null);
        return createJmsContext(factory, JMSContext.AUTO_ACKNOWLEDGE);
    }
    public JMSContext createJmsContext(PulsarConnectionFactory factory, int sessionMode) {
        assert (factory != null);
        assert ((sessionMode == JMSContext.AUTO_ACKNOWLEDGE) ||
                (sessionMode == JMSContext.CLIENT_ACKNOWLEDGE) ||
                (sessionMode == JMSContext.DUPS_OK_ACKNOWLEDGE) ||
                (sessionMode == JMSContext.SESSION_TRANSACTED));
        return factory.createContext(sessionMode);
    }

    public Queue createQueueDestination(JMSContext jmsContext, String destName) {
        assert (jmsContext != null);
        assert (StringUtils.isNotBlank(destName));

        if  (!(StringUtils.startsWith("persistent://", destName))) {
            destName = "persistent://" + destName;
        }

        return jmsContext.createQueue(destName);
    }

    public Topic createTopicDestination(JMSContext jmsContext, String destName) {
        assert (jmsContext != null);
        assert (StringUtils.isNotBlank(destName));

        if  (!(StringUtils.startsWith("persistent://", destName))) {
            destName = "persistent://" + destName;
        }

        return jmsContext.createTopic(destName);
    }
}
