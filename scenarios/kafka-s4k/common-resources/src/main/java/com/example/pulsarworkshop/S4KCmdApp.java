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
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Properties;

abstract public class S4KCmdApp extends PulsarWorkshopCmdApp {
    protected final static String API_TYPE = "kafka-s4k";

    // - Only relevant for non-Astra Streaming deployment
    // - For Astra streaming properties, the corresponding Kafka properties
    //   can be derived from Astra Streaming client.conf file
    protected File kafkaCfgPropFile;
    protected WorkshopKafkaProperties kafkaCfgProperties;
    protected File schemaRegistryCfgPropFile;
    protected WorkshopKafkaProperties schemaRegistryCfgProperties;

    public S4KCmdApp(String appName, String[] inputParams) {
        super(appName, inputParams);
        // Add the S4K/Kafka specific CLI options that are common to all S4K/Kafka client applications
        addOptionalCommandLineOption("kp", "kafka.prop",
                true, "Kafka configuration properties file.");

        addOptionalCommandLineOption("srp", "schema.registry.prop",
                true, "Kafka schema registry configuration properties file.");
    }


    @Override
    public void processExtendedInputParams() throws InvalidParamException {
        // (Optional) Only relevant with non-Astra Streaming deployment
        if (!useAstraStreaming) {
            kafkaCfgPropFile = processFileInputParam("kp");
            if (kafkaCfgPropFile == null) {
                throw new InvalidParamException("Must provide a valid Kafka configuration properties file as the value for \"kp\" option!");
            }

            schemaRegistryCfgPropFile = processFileInputParam("srp");

            kafkaCfgProperties = new WorkshopKafkaProperties(kafkaCfgPropFile);
            schemaRegistryCfgProperties = new WorkshopKafkaProperties(schemaRegistryCfgPropFile);
        }
    }

    protected Properties getBaseKafkaCfgProperties() {
        if (useAstraStreaming) {
            Properties properties = new Properties();
            properties.put("bootstrap.servers", getASBootstrapServerUrl());
            properties.put("security.protocol","SASL_SSL");
            properties.put("sasl.mechanism","PLAIN");
            properties.put("sasl.jaas.config",
                    String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username='%s' password='%s';",
                            getASUserName(), getASPassword()));
            return properties;
        }
        else {
            return kafkaCfgProperties.getConfigProps();
        }
    }

    private String getASUserName() {
        // topicName is in the format of "<tenant>/<namespace>/<topic>"
        // use "<tenant>" as the Kafka username
        return StringUtils.substringBefore(topicName, "/");
    }

    private String getASPassword() {
        return  clientConnConf.getValue("authParams");
    }

    private String getASBootstrapServerUrl() {
        // pulsarBrokerSvcUrl has the format of
        // - pulsar+ssl://pulsar-gcp-uscentral1.api.streaming.datastax.com:6651, or
        // - https://pulsar-gcp-uscentral1.api.streaming.datastax.com:<port>
        String pulsarBrokerSvcUrl = clientConnConf.getValue("brokerServiceUrl");
        String srvHostName = StringUtils.substringBetween(pulsarBrokerSvcUrl, "//pulsar-", ":");
        return  "kafka-" + srvHostName + ":9093";
    }

    private String getASSchemaRegistryUrl() {
        // pulsarBrokerSvcUrl has the format of
        // - pulsar+ssl://pulsar-gcp-uscentral1.api.streaming.datastax.com:6651, or
        // - https://pulsar-gcp-uscentral1.api.streaming.datastax.com:<port>
        String pulsarBrokerSvcUrl = clientConnConf.getValue("brokerServiceUrl");
        String srvHostName = StringUtils.substringBetween(pulsarBrokerSvcUrl, "//pulsar-", ":");
        return  "https://kafka-" + srvHostName + ":8081";
    }
}
