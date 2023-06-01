package com.example.pulsarworkshop;


import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SizeUnit;

public class Common {
    public static PulsarClient makeClient(AppArgs args) throws URISyntaxException, PulsarClientException {
        String trustCertFilePath;
        if (args.enableTls){
            if (args.debug){
                trustCertFilePath = getTrustCertFilePath();

            } else {
                trustCertFilePath = args.trustCertPath;
            }
            PulsarClient client = PulsarClient.builder()
                    .serviceUrl(args.serviceUrl)
                    .authentication(AuthenticationFactory.token(args.token))
                    .tlsTrustCertsFilePath(trustCertFilePath)
                    .allowTlsInsecureConnection(false)
                    .enableTlsHostnameVerification(false) // setting to false due to port forwarding for demo
                    .memoryLimit(0, SizeUnit.BYTES)
                    .build();
            return client;
        } else {
            PulsarClient client = PulsarClient.builder()
                    .serviceUrl(args.serviceUrl)
                    .authentication(AuthenticationFactory.token(args.token))
                    .memoryLimit(0, SizeUnit.BYTES)
                    .build();
            return client;
        }
    }
    private static String getTrustCertFilePath() throws URISyntaxException {
        URL res = DemoLoadProducer.class.getClassLoader().getResource("ca.crt");
        File file = Paths.get(res.toURI()).toFile();
        String trustCertFilePath = file.getAbsolutePath();
        return trustCertFilePath;
    }
}