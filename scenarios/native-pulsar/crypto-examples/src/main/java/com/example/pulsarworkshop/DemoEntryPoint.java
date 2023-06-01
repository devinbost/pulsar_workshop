package com.example.pulsarworkshop;


import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.net.URISyntaxException;

public class DemoEntryPoint {
    public static void main(String... argv) throws InterruptedException, IOException, URISyntaxException {
        System.out.println("Starting app");
        AppArgs args = new AppArgs();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);
        switch (args.usage){
            case "produce":
                var producerApp = new DemoLoadProducer();
                producerApp.main(argv);
                break;
            case "consume":
                var consumerApp = new DemoRegexConsumer();
                consumerApp.main(argv);
                break;
            default:
                break;
        }
    }
}
