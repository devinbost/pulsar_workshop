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


import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;

public class DemoSchemaProducer implements AutoCloseable  {
    private static PulsarClient client;
    public static void main(String... argv) throws InterruptedException, IOException, URISyntaxException {
        System.out.println("Starting app");
        AppArgs args = new AppArgs();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);
        client = Common.makeClient(args);
        System.out.println("Created Pulsar client");
        var inquiry = makeTestCreditInquiry();

        Producer<CreditInquiry> producer = client
                .newProducer(Schema.AVRO(CreditInquiry.class))
                .sendTimeout(1, TimeUnit.SECONDS)
                .topic("persistent://mytenant/mynamespace1/credit-inquiries").create();
        producer.newMessage().value(inquiry).send();
        producer.close();
        // Show that the following line won't compile:
        //producer1.send("test".getBytes());
        System.out.println("Produced message");
    }

    private static CreditInquiry makeTestCreditInquiry() {
        var creditInquiry = CreditInquiry.builder()
                .user_id(23425)
                .score(800)
                .customer_token("aLK32JA5FDK23-JSAkls.23aayyt982=")
                .created_time(System.currentTimeMillis())
                .note("test me")
                .build();
        return creditInquiry;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
