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

import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.pojo.IoTSensorData;
import com.example.pulsarworkshop.util.SpringPulsarCmdAppUtils;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Schema;
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
import org.springframework.pulsar.core.ProducerBuilderCustomizer;
import org.springframework.pulsar.core.PulsarConsumerFactory;
import org.springframework.pulsar.listener.AckMode;

import java.util.ArrayList;
import java.util.Collections;

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

     @Value("${spring.pulsar.consumer.subscription-name}")
     private String subscriptionName;

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

    @Autowired
    PulsarConsumerFactory pulsarConsumerFactory;

    @Override
    public void run(String... args) throws Exception {

        logger.info(">> numMessages: {}, topic: {}", numMessages, topic);

        SpringPulsarCmdAppUtils.processNumMsgInputParam(numMessages);
        SpringPulsarCmdAppUtils.processTopicNameInputParam(topic);

        // Set the required consumer configurations
        ConsumerBuilderCustomizer<IoTSensorData> consumerBuilderCustomizer =
                consumerBuilder -> {
                    consumerBuilder
                            .consumerName(IoTSensorSpbpConsumer.APP_NAME)
                            .subscriptionType(SubscriptionType.Shared);
                };

        Consumer<IoTSensorData> consumer = pulsarConsumerFactory.createConsumer(
                Schema.AVRO(IoTSensorData.class),
                Collections.singletonList(topic),
                subscriptionName,
                consumerBuilderCustomizer);

        while ((numMessages == -1) || (totalMsgReceived < numMessages)) {
            Message<IoTSensorData> message = consumer.receive();
            logger.info("Message received and acknowledged: key={}; properties={}; value={}",
                    message.getKey(),
                    message.getProperties(),
                    message.getValue());
            consumer.acknowledge(message);
            totalMsgReceived++;
        }
    }


    /*
     * Below is another way of receiving messages asynchronously.
     * Since we want to more control of the consumer in this demo (and receive messages syncrhonously),
     *   we'll keep using the above approach
     ----------------------------------------
    @PulsarListener(
            topics = "${spbp-pubsub.topic}",
            subscriptionName = "${spring.pulsar.consumer.subscription-name}",
            //consumerCustomizer = "myConsumerCustomizer",
            subscriptionType = SubscriptionType.Exclusive,
            schemaType = SchemaType.AVRO,
            ackMode = AckMode.RECORD)
    public void listen(IoTSensorData message, org.apache.pulsar.client.api.Consumer<IoTSensorData> consumer) {
        // NOTE: When accessing the Consumer object this way, do NOT invoke any operations that would change the
        //       Consumer’s cursor position by invoking any receive methods. All such operations must be done by the container.
        while ((numMessages == -1) || (totalMsgReceived++ < numMessages)) {
            logger.info("Successfully received message: msg-payload={})", message);
            totalMsgReceived++;
        }
    }

    @Bean
    public ConsumerBuilderCustomizer<IoTSensorData> myConsumerCustomizer() {
        return cb -> {
            cb.subscriptionType(SubscriptionType.valueOf(subTypeStr));
        };
    }
    */
}
