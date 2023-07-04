/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.example.pulsarworkshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.BuiltinExchangeType;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class S4RTopicProducer extends S4RCmdApp {
    private final static String APP_NAME = "S4RTopicProducer";
    static { System.setProperty("log_file_base_name", getLogFileName(API_TYPE, APP_NAME)); }
    private final static Logger logger = LoggerFactory.getLogger(S4RTopicProducer.class);
    public S4RTopicProducer(String appName, String[] inputParams) {
        super(appName, inputParams);
    }
    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new S4RTopicProducer("S4RTopicProducer",args);
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
            channel.exchangeDeclare(S4RExchangeName, BuiltinExchangeType.TOPIC);
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
