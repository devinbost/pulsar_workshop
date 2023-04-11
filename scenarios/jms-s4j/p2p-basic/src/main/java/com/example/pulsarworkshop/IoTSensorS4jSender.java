package com.example.pulsarworkshop;

import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import com.example.pulsarworkshop.util.CsvFileLineScanner;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import java.io.File;
import java.io.IOException;

public class IoTSensorS4jSender extends S4JCmdApp {
    private final static Logger logger = LoggerFactory.getLogger(IoTSensorS4jSender.class);

    private static PulsarConnectionFactory connectionFactory;
    private static JMSContext jmsContext;
    private static JMSProducer jmsProducer;
    private static Destination queueDestination;

    private File iotSensorDataCsvFile;
    public IoTSensorS4jSender(String appName, String[] inputParams) {
        super(appName, inputParams);
        addRequiredCommandLineOption("csv","csvFile", true, "IoT sensor data CSV file.");
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new IoTSensorS4jSender("IoTSensorS4jSender", args);
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

                if (queueDestination == null) {
                    queueDestination = createQueueDestination(jmsContext, pulsarTopicName);
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
                        jmsProducer.send(queueDestination, csvLine);
                        if (logger.isDebugEnabled()) {
                            logger.debug(">>> IoT sensor data published - line# {}, {}",
                                    msgSent, csvLine);
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

        } catch (IOException ioException) {
            throw new WorkshopRuntimException("Failed to read from the workload data source file: " + ioException.getMessage());
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
