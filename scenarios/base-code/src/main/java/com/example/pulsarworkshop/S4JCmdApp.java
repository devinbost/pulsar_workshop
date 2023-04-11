package com.example.pulsarworkshop;

import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import com.example.pulsarworkshop.exception.InvalidParamException;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.HashMap;
import java.util.Map;

abstract public class S4JCmdApp extends PulsarWorkshopCmdApp {

    protected int jmsSessionMode = JMSContext.AUTO_ACKNOWLEDGE;

    public S4JCmdApp(String appName, String[] inputParams) {
        super(appName, inputParams);
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

        jmsConnMap.put("webServiceUrl", pulsarClientConf.getValue("webServiceUrl"));
        jmsConnMap.put("brokerServiceUrl", pulsarClientConf.getValue("brokerServiceUrl"));
        jmsConnMap.put("authPlugin", pulsarClientConf.getValue("authPlugin"));
        jmsConnMap.put("authParams", pulsarClientConf.getValue("authParams"));
        jmsConnMap.put("tlsEnableHostnameVerification", pulsarClientConf.getValue("tlsEnableHostnameVerification"));
        jmsConnMap.put("tlsTrustCertsFilePath", pulsarClientConf.getValue("tlsTrustCertsFilePath"));
        jmsConnMap.put("tlsAllowInsecureConnection", pulsarClientConf.getValue("tlsAllowInsecureConnection"));

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
