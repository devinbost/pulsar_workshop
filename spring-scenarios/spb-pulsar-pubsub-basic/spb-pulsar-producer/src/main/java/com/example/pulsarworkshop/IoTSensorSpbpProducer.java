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
import com.example.pulsarworkshop.util.CsvFileLineScanner;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.pulsarworkshop.util.SpringPulsarCmdAppUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.pulsar.core.ProducerBuilderCustomizer;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.pulsar.core.TypedMessageBuilderCustomizer;

import java.io.File;

@SpringBootApplication
public class IoTSensorSpbpProducer implements CommandLineRunner  {
    private final static String API_TYPE = "spring-boot-pulsar";
    private final static String APP_NAME = "IoTSensorSpbpProducer";

    // Must be set before initializing the "logger" object.
    static { System.setProperty("log_file_base_name", SpringPulsarCmdAppUtils.getLogFileName(API_TYPE, APP_NAME)); }
    private final static Logger logger = LoggerFactory.getLogger(IoTSensorSpbpProducer.class);

    private final static String[] SensorLocations = {"alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta"};

    /**
     * Spring boot Pulsar producer doc:
     *   https://docs.spring.io/spring-pulsar/docs/current-SNAPSHOT/reference/html/#_message_production
     */


    @Value("${spbp-pubsub.num-msg}")
    private int numMessages;

    @Value("${spbp-pubsub.topic}")
    private String topic;

    @Value("${spbp-pubsub.csvFile}")
    private String iotCsvFilePath;

    private static int totalMsgSent;

    public static void main(String[] args) {
        logger.info("Starting Spring boot Pulsar producer application 'IoTSensorSpbpProducer'");

        ConfigurableApplicationContext ctx = null;
        try {
            ctx = SpringApplication.run(IoTSensorSpbpProducer.class, args);
        }
        catch (InvalidParamException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid configuration value detected: " + ex.getMessage());
            }
            ex.printStackTrace();
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
            logger.info("Existing Spring boot Pulsar producer application 'IoTSensorSpbpProducer'");
            logger.info("Total message sent: {}\n", totalMsgSent);
        }
    }

    @Autowired
    PulsarTemplate<IoTSensorData> pulsarTemplate;

    @Override
    public void run(String... args) throws Exception {

        // Set the required Producer configurations
        ProducerBuilderCustomizer<IoTSensorData> producerBuilderCustomizer =
                producerBuilder -> {
                    producerBuilder
                            .producerName(IoTSensorSpbpProducer.APP_NAME)
                            .blockIfQueueFull(true);
                };

        // Customized message builder - adding message key, properties, and body
        TypedMessageBuilderCustomizer<IoTSensorData> typedMessageBuilderCustomize =
                messageBuilder -> {
                    messageBuilder.property(
                            "source_file_name", StringUtils.substringAfterLast(iotCsvFilePath, "/"));
                };

        File iotSensorDataCsvFile = SpringPulsarCmdAppUtils.processFileInputParam(iotCsvFilePath);

        CsvFileLineScanner csvFileLineScanner = new CsvFileLineScanner(iotSensorDataCsvFile);
        SpringPulsarCmdAppUtils.processNumMsgInputParam(numMessages);
        SpringPulsarCmdAppUtils.processTopicNameInputParam(topic);

        boolean isTitleLine = true;
        String titleLine = "";

        while (csvFileLineScanner.hasNextLine()) {
            String csvLine = csvFileLineScanner.getNextLine();
            // Skip the first line which is a title line
            if (!isTitleLine) {
                if ((numMessages == -1) || (totalMsgSent < numMessages)) {
                    IoTSensorData data = SpringPulsarCmdAppUtils.csvToPojo(csvLine);
                    MessageId messageId = pulsarTemplate
                            .newMessage(data)
                            .withTopic(topic)
                            .withMessageCustomizer(typedMessageBuilderCustomize)
                            .withProducerCustomizer(producerBuilderCustomizer)
                            .send();

                    logger.info("Successfully sent message: {} msg-payload={})", messageId, data);
                    totalMsgSent++;
                } else {
                    break;
                }
            } else {
                isTitleLine = false;
                titleLine = csvLine;
            }
        }
    }
}
