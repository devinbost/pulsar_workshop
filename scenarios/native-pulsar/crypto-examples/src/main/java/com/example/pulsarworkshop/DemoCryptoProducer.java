package com.example.pulsarworkshop;

import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;

public class DemoCryptoProducer implements AutoCloseable  {
    private static PulsarClient client;
    public static void main(String... argv) throws IOException, URISyntaxException {
        System.out.println("Starting app");
        AppArgs args = new AppArgs();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);
        client = Common.makeClient(args);
        System.out.println("Created Pulsar client");
        var inquiry = makeTestCreditInquiry();

        System.out.println("Creating Pulsar producer");
        Producer<CreditInquiry> producer = client.newProducer(Schema.AVRO(CreditInquiry.class))
                .sendTimeout(1, TimeUnit.SECONDS)
                .topic("persistent://public/default/credit-inquiries3").create();
        System.out.println("Sending message");
        producer.newMessage().value(inquiry).send();
        System.out.println("Produced message");
        producer.close();
        System.out.println("Closed producer");
    }

    private static CreditInquiry makeTestCreditInquiry() {
        var creditInquiry = CreditInquiry.builder()
                .user_id(23425)
                .score(800)
                .customer_token("kalklajsdasfdasf")
                .created_time(System.currentTimeMillis())
                .note("SSN 123-34-5634")
                .build();
        return creditInquiry;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}