package com.example.pulsarworkshop;

import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import com.example.pulsarworkshop.util.CsvFileLineScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.File;
import java.io.IOException;

public class IoTSensorTopicPublisher extends S4JCmdApp {
    private final static Logger logger = LoggerFactory.getLogger(IoTSensorTopicPublisher.class);

    private static PulsarConnectionFactory connectionFactory;
    private static JMSContext jmsContext;
    private static JMSProducer jmsProducer;
    private static Topic topicDestination;

    private File iotSensorDataCsvFile;
    public IoTSensorTopicPublisher(String appName, String[] inputParams) {
        super(appName, inputParams);
        addRequiredCommandLineOption("csv","csvFile", true, "IoT sensor data CSV file.");
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new IoTSensorTopicPublisher("IoTSensorTopicPublisher", args);
        int exitCode = workshopApp.run();
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
    public void runApp() throws WorkshopRuntimException {
        try {
            if (connectionFactory == null) {
                connectionFactory = createPulsarJmsConnectionFactory();

                if (jmsContext == null) {
                    jmsContext = createJmsContext(connectionFactory);
                    jmsProducer = jmsContext.createProducer();
                }

                if (topicDestination == null) {
                    topicDestination = createTopicDestination(jmsContext, pulsarTopicName);
                }
            }

            assert (iotSensorDataCsvFile != null);
            CsvFileLineScanner csvFileLineScanner = new CsvFileLineScanner(iotSensorDataCsvFile);

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
                        jmsProducer.send(topicDestination, csvLine);
                        if (logger.isDebugEnabled()) {
                            logger.debug(">>> IoT sensor data published to topic {} (line# {}, {})",
                                    topicDestination.getTopicName(), msgSent, csvLine);
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

        }
        catch (IOException ioException) {
            throw new WorkshopRuntimException("Failed to read from the workload data source file! " + ioException.getMessage());
        }
        catch (JMSException jmsException) {
            throw new WorkshopRuntimException("Unexpected error when publishing JMS messages to a topic! " + jmsException.getMessage());
        }
    }

    @Override
    public void termApp() {
        if (jmsContext != null) {
            jmsContext.close();
        }

        if (connectionFactory != null) {
            connectionFactory.close();
        }
    }
}
