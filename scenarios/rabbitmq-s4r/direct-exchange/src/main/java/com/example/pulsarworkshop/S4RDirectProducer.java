package com.example.pulsarworkshop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.BuiltinExchangeType;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class S4RDirectProducer extends S4RCmdApp {
    private final static String APP_NAME = "S4RDirectProducer";
    static { System.setProperty("log_file_base_name", getLogFileName(API_TYPE, APP_NAME)); }
    private final static Logger logger = LoggerFactory.getLogger(S4RDirectProducer.class);
    public S4RDirectProducer(String appName, String[] inputParams) {
        super(appName, inputParams);
    }
    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new S4RDirectProducer("S4RDirectProducer",args);
        int exitCode = workshopApp.runCmdApp();
        System.exit(exitCode);
    }

    @Override
    public void execute() {
        try {
            S4RFactory= new ConnectionFactory();
            S4RFactory.setHost(S4RRabbitMQHost);
            S4RFactory.setPort(S4RPort);
            S4RFactory.setUsername(S4RUser);
            S4RFactory.setPassword(S4RPassword);
            S4RFactory.setVirtualHost(S4RVirtualHost);
            if(AstraInUse) {
                S4RFactory.useSslProtocol();    
            }
            connection = S4RFactory.newConnection();
            channel = connection.createChannel();
            channel.confirmSelect();
            channel.exchangeDeclare(S4RExchangeName, BuiltinExchangeType.DIRECT);
            logger.info("Exchange name is: " + S4RExchangeName);
            int msgSent = 0;
            while (numMsg > msgSent) {
                String message = S4RMessage; 
                channel.basicPublish(S4RExchangeName, S4RRoutingKey, null, message.getBytes());
                if (logger.isDebugEnabled()) {
                    logger.debug("S4R Published a message: {} Routing Key {}", msgSent, S4RRoutingKey);
                }
                msgSent++;
                channel.waitForConfirmsOrDie(5000);  //basically flush after each message published
            }
        } catch (Exception e) {
            throw new WorkshopRuntimException("Unexpected error when producing S4R messages: " + e.getMessage());  
        }
    }

    @Override
    public void termCmdApp() {
        try {
            channel.close();
            connection.close();
        } catch (IOException ioe) {
            throw new WorkshopRuntimException("Unexpected error when shutting down S4R Producer IO Exception: " + ioe.getMessage());  
        } catch (TimeoutException te) {
            throw new WorkshopRuntimException("Unexpected error when shutting down S4R Producer Timeout Exception: " + te.getMessage());  
        }
    }
}
