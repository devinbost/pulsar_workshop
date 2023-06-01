package com.example.pulsarworkshop;

import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;

public class DemoCryptoConsumer implements AutoCloseable {
    private static PulsarClient client;
    public static void main(String... argv)
            throws IOException, URISyntaxException, NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        System.out.println("Starting app");
        AppArgs args = new AppArgs();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);
        client = Common.makeClient(args);
        System.out.println("Created Pulsar client");

        Consumer<CreditInquiryEncrypted> consumer = client.newConsumer(Schema.AVRO(CreditInquiryEncrypted.class))
                .subscriptionName("crypto-demo-sub")
                .topic("persistent://public/default/credit-inquiries12")
                .subscribe();

        while (true) {
            System.out.println("Starting loop");
            // Block for up to 1 second for a message
            Message<CreditInquiryEncrypted> msg = consumer.receive();
            if(msg != null){
                //var obj = msg.
                System.out.printf("Message received: %s", msg.getValue());
                // Acknowledge the message to remove it from the message backlog
                LookupService service = new LookupService();
                System.out.println("Note before decryption:");
                System.out.printf("%s is note for user: %d%n", msg.getValue().getNote_encrypted(),
                        msg.getValue().getUser_id());
                var decryptedField = service.decryptField(msg.getValue().getNote_encrypted(), msg.getValue().getUser_id().toString());
                System.out.println("Note after decryption:");
                System.out.printf("%s is note for user: %d%n", decryptedField,
                        msg.getValue().getUser_id());
                consumer.acknowledge(msg);
            }

        }
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}

