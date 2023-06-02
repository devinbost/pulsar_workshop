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

import com.example.pulsarworkshop.pojo.IoTSensorData;
import com.example.pulsarworkshop.pojo.IoTSensorDataUtils;
import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import com.example.pulsarworkshop.util.CsvFileLineScanner;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class IoTSensorProducerAvro extends NativePulsarCmdApp {

    // Must be set before initializing the "logger" object.
    private final static String APP_NAME = "IoTSensorProducerAvro";
    static { System.setProperty("log_file_base_name", getLogFileName(API_TYPE, APP_NAME)); }
    private final static Logger logger = LoggerFactory.getLogger(IoTSensorProducerAvro.class);

    private static File iotSensorDataCsvFile;
    private static PulsarClient pulsarClient;
    private static Producer<IoTSensorData> pulsarProducer;

    public IoTSensorProducerAvro(String appName, String[] inputParams) {
        super(appName, inputParams);
        addRequiredCommandLineOption("csv","csvFile", true, "IoT sensor data CSV file.");

        logger.info("Starting application: \"" + appName + "\" ...");
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new IoTSensorProducerAvro(APP_NAME, args);
        int exitCode = workshopApp.runCmdApp();
        System.exit(exitCode);
    }

    @Override
    public void processExtendedInputParams() throws InvalidParamException {
        super.processExtendedInputParams();

        // (Required) CLI option for IoT sensor source file
        iotSensorDataCsvFile = processFileInputParam("csv");
        if ( iotSensorDataCsvFile == null) {
            throw new InvalidParamException("Must provided a valid IoT sensor source data csv file!");
        }
    }

    @Override
    public void execute() throws WorkshopRuntimException {
        try {
            if (pulsarClient == null ) {
                pulsarClient = createNativePulsarClient();
                if (pulsarProducer == null) {
                    ProducerBuilder<IoTSensorData> producerBuilder = pulsarClient.newProducer(Schema.AVRO(IoTSensorData.class));
                    pulsarProducer = producerBuilder.topic(topicName).create();
                }
            }

            assert (iotSensorDataCsvFile != null);

            CsvFileLineScanner csvFileLineScanner = new CsvFileLineScanner(iotSensorDataCsvFile);
            TypedMessageBuilder<IoTSensorData> messageBuilder = pulsarProducer.newMessage();

            boolean isTitleLine = true;
            String titleLine = "";
            int msgSent = 0;
            if (numMsg == -1) {
                numMsg = Integer.MAX_VALUE;
            }

            while (csvFileLineScanner.hasNextLine()) {
                String csvLine = csvFileLineScanner.getNextLine();
                // Skip the first line which is a title line
                if (!isTitleLine && StringUtils.isNotBlank(csvLine)) {
                    if (msgSent < numMsg) {
                        IoTSensorData data = IoTSensorDataUtils.csvToPojo(csvLine);
                        MessageId messageId = messageBuilder
                                .value(data)
                                .send();
                        logger.info("Published a message with raw value: [{}] {}",
                                msgSent,
                                csvLine);

                        msgSent++;
                    } else {
                        break;
                    }
                } else {
                    isTitleLine = false;
                    titleLine = csvLine;
                }
            }

        } catch (PulsarClientException pce) {
            pce.printStackTrace();
            throw new WorkshopRuntimException("Unexpected error when producing Pulsar messages: " + pce.getMessage());
        } catch (IOException ioException) {
            throw new WorkshopRuntimException("Failed to read from the workload data source file: " + ioException.getMessage());
        }
    }

    @Override
    public void termCmdApp() {
        try {
            if (pulsarProducer != null) {
                pulsarProducer.close();
            }

            if (pulsarClient != null) {
                pulsarClient.close();
            }
        }
        catch (PulsarClientException pce) {
            throw new WorkshopRuntimException("Failed to terminate Pulsar producer or client!");
        }
        finally {
            logger.info("Terminating application: \"" + appName + "\" ...");
        }
    }
}
