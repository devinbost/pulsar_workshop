package com.example.pulsarworkshop;

import com.example.pulsarworkshop.common.PulsarWorkshopCmdApp;
import com.example.pulsarworkshop.common.exception.InvalidParamException;
import com.example.pulsarworkshop.common.exception.WorkshopRuntimException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.shade.org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebeziumReplicator extends PulsarWorkshopCmdApp {

    private final static Logger logger = LoggerFactory.getLogger(PulsarConsumerFullCfg.class);

    // Default to consume 20 messages
    // -1 means to consume all available messages (indefinitely)
    private int numMsg = 20;
    private String subsriptionName;
    private SubscriptionType subscriptionType = SubscriptionType.Exclusive;

    PulsarClient consumerClient;

    PulsarClient producerClient;

    Consumer<GenericRecord> consumer;

    Producer<Product> producer;

    public PulsarConsumerFullCfg(String[] inputParams) {
        super(inputParams);

        cliOptions.addOption(new Option("num","numMsg", true, "Number of message to produce."));
        cliOptions.addOption(new Option("sbt","subType", true, "Pulsar subscription type."));
        cliOptions.addOption(new Option("sbn", "subName", true, "Pulsar subscription name."));
    }

    public static void main(String[] args) {
        PulsarWorkshopCmdApp workshopApp = new PulsarConsumerFullCfg(args);

        int exitCode = workshopApp.run("PulsarConsumerFullCfg");

        System.exit(exitCode);
    }

    @Override
    public void processInputParams() throws InvalidParamException {
        CommandLine commandLine = null;

        try {
            commandLine = cmdParser.parse(cliOptions, rawCmdInputParams);
        } catch (ParseException e) {
            throw new InvalidParamException("Failed to parse application CLI input parameters: " + e.getMessage());
        }

        super.processBasicInputParams(commandLine);

        // (Required) CLI option for number of messages
        numMsg = processIntegerInputParam(commandLine, "num");
    	if ( (numMsg <= 0) && (numMsg != -1) ) {
    		throw new InvalidParamException("Message number must be a positive integer or -1 (all available raw input)!");
    	}    	

        // (Required) Pulsar subscription name
        subsriptionName = processStringInputParam(commandLine, "subName");
        String subType = processStringInputParam(commandLine, "subType");
        if (!StringUtils.isBlank(subType)) {
        try {
	            subscriptionType = SubscriptionType.valueOf(subType);
	        }
	        catch (IllegalArgumentException iae) {
	            subscriptionType = SubscriptionType.Exclusive;
	        }
        }
    }
    public void fixThis() { 
        // This method needs to be refactored to use the abstract class, parameters, etc.
               consumerClient = PulsarClient.builder()
                .serviceUrl(localServiceUrl)
                .authentication(
                        AuthenticationFactory.token(astraToken)
                )
                .build();

        // Create client object
        producerClient = PulsarClient.builder()
                .serviceUrl(astraServiceUrl)
                .authentication(
                        AuthenticationFactory.token(astraToken)
                )
                .build();

        // Create consumer on a topic with a subscription
        consumer = consumerClient.newConsumer(Schema.AUTO_CONSUME())
                .topic("public/default/dbserver1.inventory.products")
                .subscriptionName("demo-subscription")
                .subscribe();

        // Create producer on a topic
        producer = producerClient.newProducer(Schema.AVRO(Product.class))
                .topic("persistent://cdctest04/default/products")
                .create();
    }

    @Override
    public void runApp() {
        try {
            fixThis();
                   boolean receivedMsg = false;
        // Loop until a message is received
        do {
            System.out.println("Checking for message");
            // Block for up to 1 second for a message
            Message<GenericRecord> msg = consumer.receive(10, TimeUnit.SECONDS);

            if(msg != null){

                GenericRecord input = msg.getValue();
                //CDC Uses KeyValue Schema
                KeyValue<GenericRecord, GenericRecord> keyValue = (KeyValue<GenericRecord, GenericRecord>) input.getNativeObject();

                GenericRecord keyGenRecord = keyValue.getKey();
                displayGenericRecordFields("key",keyGenRecord);

                GenericRecord valGenRecord = keyValue.getValue();
                displayGenericRecordFields("value",valGenRecord);

                var productId = (int)keyGenRecord.getField("id");
                var name = (String)valGenRecord.getField("name");
                var description = (String)valGenRecord.getField("description") + "!!!!!!";
                var weight = (Double)valGenRecord.getField("weight");
                var product = new Product(productId, name, description, weight);
                System.out.println("Sending product:" + product.toString());
                producer.send(product);
                // Acknowledge the message to remove it from the message backlog
                consumer.acknowledge(msg);

                receivedMsg = true;
            }

        } while (!receivedMsg);
        // TODO: Need to refactor this to process the specified number of messages instead of just one.

        //Close the producer
        producer.close();

        // Close the client
        consumerClient.close();

        //Close the consumer
        consumer.close();

        // Close the consumerClient
        consumerClient.close();

        }
        catch (PulsarClientException pce) {
            throw new WorkshopRuntimException("Unexpected error when consuming Pulsar messages!");
        }
    }

    @Override
    public void termApp() {
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
    private static void displayGenericRecordFields(String recordName,GenericRecord genericRecord) {
        System.out.printf("---Fields in %s: ", recordName);
        genericRecord.getFields().stream().forEach((fieldtmp) ->
                System.out.printf("%s=%s",fieldtmp.getName(),genericRecord.getField(fieldtmp) ));
        System.out.println(" ---");
    }
}
