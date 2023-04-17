package com.example.pulsarworkshop;

abstract public class S4RCmdApp extends PulsarWorkshopCmdApp {
    protected final static String API_TYPE = "rabbitmq-s4r";
    public S4RCmdApp(String appName, String[] inputParams) {
        super(appName, inputParams);
        // Add the S4R/RabbitMQ specific CLI options that are common to all S4R/RabbitMQ client applications
    }
}
