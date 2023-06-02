/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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