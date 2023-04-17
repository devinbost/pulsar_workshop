package com.example.pulsarworkshop;

import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import com.example.pulsarworkshop.util.CsvFileLineScanner;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


public class IoTSensorKafkaProducer extends S4KCmdApp {
    // Must be set before initializing the "logger" object.
    private final static String APP_NAME = "IoTSensorKafkaProducer";
    static { System.setProperty("log_file_base_name", getLogFileName(API_TYPE, APP_NAME)); }
    private final static Logger logger = LoggerFactory.getLogger(IoTSensorKafkaProducer.class);
    private static Producer<String, String> kafkaProducer;
    private File iotSensorDataCsvFile;

    public IoTSensorKafkaProducer(String appName, String[] inputParams) {
        super(appName, inputParams);
        addRequiredCommandLineOption("csv","csvFile", true, "IoT sensor data CSV file.");

        logger.info("Starting application: \"" + appName + "\" ...");
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new IoTSensorKafkaProducer(APP_NAME, args);
        int exitCode = workshopApp.run();
        System.exit(exitCode);
    }

    private KafkaProducer<String, String> createKafkaProducer() {
        Properties properties = getBaseKafkaCfgProperties();
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(properties);
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
            if (kafkaProducer == null) {
                kafkaProducer = createKafkaProducer();
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
                        ProducerRecord<String, String> message =
                                new ProducerRecord<>(topicName, csvLine);
                        kafkaProducer.send(message);
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

        }
        catch (IOException ioException) {
            throw new WorkshopRuntimException("Failed to read from the workload data source file! " + ioException.getMessage());
        }
    }

    @Override
    public void termApp() {
        try {
            kafkaProducer.flush();
            kafkaProducer.close();
        }
        finally {
            logger.info("Terminating application: \"" + appName + "\" ...");
        }
    }
}
