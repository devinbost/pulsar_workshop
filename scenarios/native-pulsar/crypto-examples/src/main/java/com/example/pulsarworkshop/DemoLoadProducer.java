package com.example.pulsarworkshop;


import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.pulsar.client.api.*;
import java.util.concurrent.ThreadLocalRandom;

public class DemoLoadProducer implements AutoCloseable  {
    private static PulsarClient client;
    private static ConcurrentHashMap<Integer, Producer<byte[]>> producerDict;
    public static void main(String... argv) throws InterruptedException, IOException, URISyntaxException {
        System.out.println("Starting app");
        AppArgs args = new AppArgs();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);
        client = Common.makeClient(args);
        System.out.println("Created Pulsar client");
        List<Integer> range = IntStream.rangeClosed(1, 1000)
                .boxed().collect(Collectors.toList());
        producerDict = new ConcurrentHashMap<>();
        while(true){
            range.stream().forEach(topicNum ->
            {
                if (args.debug){
                    System.out.println(String.format("Processing topic number %s", topicNum));
                }
                String topicName = args.topicBase + "-" + topicNum.toString();
                Producer<byte[]> producer = producerDict.computeIfAbsent(topicNum, t -> {
                    try {
                        return client.newProducer()
                                .blockIfQueueFull(true)
                                .maxPendingMessages(1000)
                                .topic(topicName).create();
                    } catch (PulsarClientException e) {
                        System.out.println("Error: failed to create producer");
                        throw new RuntimeException(e);
                    }
                });
                // For each topic, produce a random number of messages.
                int randomMsgCount = ThreadLocalRandom.current().nextInt(1, 100 + 1);
                List<Integer> randomMsgRange = IntStream.rangeClosed(1, randomMsgCount)
                        .boxed().collect(Collectors.toList());
                randomMsgRange.stream().forEach(msgNum -> {
                    String message = String.format("Example topic %s - message %s ", topicNum, msgNum);
                    System.out.println(message);
                    try {
                        producer.send(message.getBytes());
                    } catch (PulsarClientException e) {
                        System.out.println("Error: failed to produce");
                        throw new RuntimeException(e);
                    }
                });
            });
        }
    }
    @Override
    public void close() throws Exception {
        producerDict.values().stream().forEach(t -> {
            try {
                t.close();
            } catch (PulsarClientException e) {
                System.out.println("Couldn't close. Already closed?");
            }
        });
        client.close();
    }
}

