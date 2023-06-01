package com.example.pulsarworkshop;


import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.SubscriptionType;
import java.util.regex.Pattern;

public class DemoRegexConsumer implements AutoCloseable  {
    private static Consumer<byte[]> consumer;
    private static PulsarClient client;
    public static void main(String ...  argv) throws InterruptedException, IOException, URISyntaxException {
        System.out.println("Starting consumer app");
        AppArgs args = new AppArgs();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);
        System.out.println("Creating client");
        client = Common.makeClient(args);
        System.out.println("Creating topic pattern");
        Pattern allTopicsInNamespace = Pattern.compile(args.topicBase + "-.*");
        System.out.println("Creating Pulsar consumer");
        consumer = client.newConsumer()
                .topicsPattern(allTopicsInNamespace)
                .patternAutoDiscoveryPeriod(15, TimeUnit.SECONDS)
                .subscriptionName(args.subscription)
                .subscriptionType(SubscriptionType.Shared)
                .subscribe();
        while (true) {
            System.out.println("Starting loop for topicBase: " + args.topicBase);
            // Block for up to 1 second for a message
            Message msg = consumer.receive(1, TimeUnit.SECONDS);
            if(msg != null){
                System.out.printf("Message received: %s", new String(msg.getData()));
                // Acknowledge the message to remove it from the message backlog
                consumer.acknowledge(msg);
            }
        }
    }
    @Override
    public void close() throws Exception {
        consumer.close();
        client.close();
    }
}

