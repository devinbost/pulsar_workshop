package com.example.pulsarworkshop;

import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import com.example.pulsarworkshop.util.CsvFileLineScanner;
import org.apache.commons.cli.Option;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class IoTSensorProducer extends PulsarWorkshopCmdApp {

    private final static Logger logger = LoggerFactory.getLogger(IoTSensorProducer.class);

    private File iotSensorDataCsvFile;
    private PulsarClient pulsarClient;
    private Producer pulsarProducer;

    public IoTSensorProducer(String appName, String[] inputParams) {
        super(appName, inputParams);
        addCommandLineOption(new Option("csv","csvFile", true, "IoT sensor data CSV file."));
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new IoTSensorProducer("IoTSensorProducer", args);
        int exitCode = workshopApp.run();
        System.exit(exitCode);
    }

    @Override
    public void processExtendedInputParams() throws InvalidParamException {
        // (Required) CLI option for IoT sensor source file
        iotSensorDataCsvFile = processFileInputParam("csv");
        if ( iotSensorDataCsvFile == null) {
            throw new InvalidParamException("Must provided a valid IoT sensor source data csv file!");
        }
    }

    @Override
    public void runApp() throws WorkshopRuntimException {
        try {
            pulsarClient = createNativePulsarClient();
            ProducerBuilder<?> producerBuilder = pulsarClient.newProducer();
            pulsarProducer = producerBuilder.topic(pulsarTopicName).create();

            assert (iotSensorDataCsvFile != null);

            CsvFileLineScanner csvFileLineScanner = new CsvFileLineScanner(iotSensorDataCsvFile);
            TypedMessageBuilder messageBuilder = pulsarProducer.newMessage();

            boolean isTitleLine = true;
            String titleLine = "";
            int msgSent = 0;
            if (numMsg == -1) {
                numMsg = Integer.MAX_VALUE;
            }

            while (csvFileLineScanner.hasNextLine()) {
                String csvLine = csvFileLineScanner.getNextLine();
                // Skip the first line which is a title line
                if (!isTitleLine) {
                    if (msgSent < numMsg) {
                        MessageId messageId = messageBuilder
                                .value(csvLine.getBytes(StandardCharsets.UTF_8))
                                .send();
                        if (logger.isDebugEnabled()) {
                            logger.debug(">>> Published a message: {}", messageId);
                        }

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
    public void termApp() {
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
    }
}
