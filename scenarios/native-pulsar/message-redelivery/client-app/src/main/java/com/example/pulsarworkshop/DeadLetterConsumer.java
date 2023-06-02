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

import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadLetterConsumer extends NativePulsarCmdApp {

    private final static Logger logger = LoggerFactory.getLogger(DeadLetterConsumer.class);

    private final static String APP_NAME = "DeadLetterConsumer";
    private final String SUB_NAME = "demo-subscription";
    private PulsarClient pulsarClient;
    private Consumer<?> pulsarConsumer;

    private String deadLetterTopicName;

    public DeadLetterConsumer(String appName, String[] inputParams) {
        super(appName, inputParams);
        logger.info("Starting application: \"" + appName + "\" ...");                    
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new DeadLetterConsumer(APP_NAME, args);
        int exitCode = workshopApp.runCmdApp();
        System.exit(exitCode);
    }

    @Override
    public void execute() {
        try {
        	
            pulsarClient = createNativePulsarClient();
            System.out.println("########### Using dlt: " + deadLetterTopicName);

            Consumer<byte[]> pulsarConsumer = pulsarClient.newConsumer()
                    .ackTimeout(1, TimeUnit.SECONDS)
                    .topic(topicName)
                    .subscriptionName(SUB_NAME)
                    .subscriptionType(SubscriptionType.Shared)
                    .subscribe();

        	// Negative Acknowledge message until re-delivery attempts are exceeded
            Message<byte[]> message = pulsarConsumer.receive();
            System.out.println("###########");
            System.out.println("########### Received message from dead letter topic: " + new String(message.getData()));
            System.out.println("###########");
            pulsarConsumer.acknowledge(message);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	throw new WorkshopRuntimException("Unexpected error when consuming Pulsar messages: " + e.getMessage());
        }
    }

    @Override
    public void termCmdApp() {
        try {
            if (pulsarConsumer != null) {
                pulsarConsumer.close();
            }

            if (pulsarClient != null) {
                pulsarClient.close();
            }
        }
        catch (PulsarClientException pce) {
            throw new WorkshopRuntimException("Failed to terminate Pulsar producer or client!");
        }
    }
}