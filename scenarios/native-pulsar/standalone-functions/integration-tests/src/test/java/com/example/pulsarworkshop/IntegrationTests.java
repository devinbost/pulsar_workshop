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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IntegrationTests {

    // Input data is:
    /*
        [
            {
                "tag_id": "tag1",
                "data_quality": 7,
                "event_time": "2023-05-24T00:00:00Z",
                "event_value": 34.56
            },
            {
                "tag_id": "tag2",
                "data_quality": 5,
                "event_time": "2023-05-24T02:00:00Z",
                "event_value": 78.9
            },
            {
                "tag_id": "tag3",
                "data_quality": 8,
                "event_time": "2023-05-24T03:30:00Z",
                "event_value": 100.01
            }
        ]
     */

    // Output should be list of Avro objects with a matching schema, something like:
    /*
    {
      "type": "record",
      "name": "Event",
      "fields": [
        {
          "name": "tag_id",
          "type": "string"
        },
        {
          "name": "data_quality",
          "type": "int"
        },
        {
          "name": "event_time",
          "type": "string"
        },
        {
          "name": "event_value",
          "type": "double"
        }
      ]
    }
     */
    private String dbClientId;
    private String dbClientSecret;
    private CqlSession astraDbSession;
    private PreparedStatement preparedSelect;
    @Test
    public void testSplitterFunction() throws PulsarClientException {
        String SERVICE_URL = "pulsar+ssl://pulsar-gcp-uscentral1.streaming.datastax.com:6651";
        // Create client object
        PulsarClient client = PulsarClient.builder()
                .serviceUrl(SERVICE_URL)
                .authentication(
                        AuthenticationFactory.token(getToken())
                )
                .build();

        // Create producer on a topic
        Producer<String> producer = client.newProducer(Schema.STRING)
                .topic("persistent://mytenant/default/input")
                .create();

        String testString1 = "[{\"tag_id\": \"tag1\", \"data_quality\": 7, \"event_time\": \"2023-05-24T00:00:00Z\", \"event_value\": 34.56}, {\"tag_id\": \"tag2\", \"data_quality\": 5, \"event_time\": \"2023-05-24T02:00:00Z\", \"event_value\": 78.9}, {\"tag_id\": \"tag3\", \"data_quality\": 8, \"event_time\": \"2023-05-24T03:30:00Z\", \"event_value\": 100.01}]";
        String testString = "[{\"tag_id\": \"tag4\", \"data_quality\": 73, \"event_time\": \"2023-05-21T00:00:00Z\", \"event_value\": 12.56}]";
        // Send a message to the topic
        producer.send(testString);

        //Close the producer
        producer.close();

        // Close the client
        client.close();
    }
    public String getToken(){
        String token = "";
        try (InputStream input = IntegrationTests.class.getResourceAsStream("/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            token = prop.getProperty("ASTRA_TOKEN");

        } catch (IOException ex) {
            System.out.println("ERROR: Couldn't read Astra token when running test");
        }
        return token;
    }
    @Test
    public void testCurrentValueFlow() throws PulsarClientException, InterruptedException {
        String SERVICE_URL = "pulsar+ssl://pulsar-gcp-uscentral1.streaming.datastax.com:6651";
        // Create client object
        PulsarClient client = PulsarClient.builder()
                .serviceUrl(SERVICE_URL)
                .authentication(
                        AuthenticationFactory.token(getToken())
                )
                .build();

        this.setConfigs();
        this.astraDbSession = CqlSession.builder()
                .withCloudSecureConnectBundle(IntegrationTests.class.getResourceAsStream("/secure-connect-demo.zip"))
                .withAuthCredentials(this.dbClientId,this.dbClientSecret)
                .withKeyspace("device")
                .build();

        this.prepareQueries();

        // Create producer on a topic
        Producer<String> producer = client.newProducer(Schema.STRING)
                .topic("persistent://mytenant/default/input")
                .create();

        for (int i = 0; i < 100; i++) {
            String testString = "[{\"tag_id\": \"tag" + i + "\", \"data_quality\": 73, \"event_time\": \"2023-05-21T00:10:00Z\", \"event_value\": 12.56}]";
            producer.send(testString);
        }

        for (int i = 0; i < 100; i++) {
            String testString = "[{\"tag_id\": \"tag" + i + "\", \"data_quality\": 73, \"event_time\": \"2023-05-21T00:00:00Z\", \"event_value\": 12.56}]";
            producer.send(testString);
        }
        // Incoming messages are older, so the last entries in the DB should still be the original ones.
        Thread.sleep(3000); // Wait a few seconds for data to flush through function flow.

        var testdevice = new DeviceTS();
        testdevice.setTagId("tag1");
        testdevice.setDataQuality(73);
        testdevice.setEventTime("2023-05-21T00:10:00Z");
        testdevice.setEventValue(12.56);

        var outputs = getResultSet(testdevice);
        Assertions.assertEquals(1, outputs.size());
        var row = outputs.get(0);
        var lastUpdatedTime = row.getInstant("event_time");
        var expectedTime = Instant.parse(testdevice.getEventTime());
        Assertions.assertEquals(expectedTime, lastUpdatedTime);

        // Then, write newer messages and check that they flushed to the DB

        for (int i = 0; i < 100; i++) {
            String testString = "[{\"tag_id\": \"tag" + i + "\", \"data_quality\": 73, \"event_time\": \"2023-05-21T00:30:00Z\", \"event_value\": 12.56}]";
            producer.send(testString);
        }
        Thread.sleep(3000); // Wait a few seconds for data to flush through function flow.

        var testdevice2 = new DeviceTS();
        testdevice.setTagId("tag1");
        testdevice.setDataQuality(73);
        testdevice.setEventTime("2023-05-21T00:30:00Z");
        testdevice.setEventValue(12.56);

        outputs = getResultSet(testdevice2);
        Assertions.assertEquals(1, outputs.size());
        row = outputs.get(0);
        lastUpdatedTime = row.getInstant("event_time");
        expectedTime = Instant.parse(testdevice2.getEventTime());
        Assertions.assertEquals(expectedTime, lastUpdatedTime);

        // Next, we need to make sure that things still work when we have multiple incoming objects.
        for (int i = 0; i < 100; i++) {
            String testString = "[{\"tag_id\": \"tag" + i + "\", \"data_quality\": 7, \"event_time\": \"2023-05-24T00:00:00Z\", \"event_value\": 34.56}, {\"tag_id\": \"tag" + i + "\", \"data_quality\": 8, \"event_time\": \"2023-05-24T02:00:00Z\", \"event_value\": 78.9}, {\"tag_id\": \"tag3\", \"data_quality\": 7, \"event_time\": \"2023-05-24T03:30:00Z\", \"event_value\": 100.01}]";
            producer.send(testString);
        }
        Thread.sleep(3000);
        var testdevice3 = new DeviceTS();
        testdevice.setTagId("tag1");
        testdevice.setDataQuality(7);
        testdevice.setEventTime("2023-05-24T00:00:00Z");
        testdevice.setEventValue(34.56);

        outputs = getResultSet(testdevice3);
        Assertions.assertEquals(1, outputs.size());
        row = outputs.get(0);
        lastUpdatedTime = row.getInstant("event_time");
        expectedTime = Instant.parse(testdevice3.getEventTime());
        Assertions.assertEquals(expectedTime, lastUpdatedTime);
        // It should be the new value.
        //Close the producer
        producer.close();

        // Close the client
        client.close();
        this.astraDbSession.close();
    }
    public void setConfigs(){
        // For future, use Pulsar Secret instead of config.properties for improved security.
        try (InputStream input = IntegrationTests.class.getResourceAsStream("/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            this.dbClientSecret = prop.getProperty("DB_CLIENT_SECRET");
            this.dbClientId = prop.getProperty("DB_CLIENT_ID");

        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }
    public void prepareQueries(){
        String selectQuery = "SELECT tag_id, data_quality, event_time, event_value FROM device.current_value WHERE tag_id = ? AND data_quality = ?";
        this.preparedSelect = this.astraDbSession.prepare(selectQuery);
    }
    public List<Row> getResultSet(DeviceTS input){
        var boundStatement = this.preparedSelect.bind(input.getTagId(), input.getDataQuality());
        var output = this.astraDbSession.execute(boundStatement);
        return output.all();
    }
}
