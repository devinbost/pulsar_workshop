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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

public class FilterIfExistsFunction implements Function<DeviceTS, Void> {
    private Logger logger;
    private ObjectMapper mapper;
    private String outputTopic;

    private CqlSession astraDbSession;

    private PreparedStatement preparedSelect;
    private String dbClientId;
    private String dbClientSecret;
    private Schema<DeviceTS> schema;

    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.schema = Schema.AVRO(DeviceTS.class);

        this.setConfigs();
        this.astraDbSession = CqlSession.builder()
                .withCloudSecureConnectBundle(FilterIfExistsFunction.class.getResourceAsStream("/secure-connect-demo.zip"))
                .withAuthCredentials(this.dbClientId,this.dbClientSecret)
                .withKeyspace("device")
                .build();

        this.prepareQueries();
        this.logger = context.getLogger();
    }
    public void setConfigs(){
        // For future, use Pulsar Secret instead of config.properties for improved security.
        try (InputStream input = FilterIfExistsFunction.class.getResourceAsStream("/config.properties")) {
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
    // Note that this approach will only catch very old duplicates since multiple consecutive messages will race
    // if received closely together.
    public List<Row> getResultSet(DeviceTS input){
        var boundStatement = this.preparedSelect.bind(input.getTagId(), input.getDataQuality());
        var output = this.astraDbSession.execute(boundStatement);
        return output.all();
    }
    @Override
    public Void process(DeviceTS input, Context context) throws Exception {
        var outputs = getResultSet(input);
        DeviceTS DeviceTSObj = new DeviceTS();

        if(outputs.size() > 0) { // i.e. if matching data exists in target table
            var row = outputs.get(0);
            var lastUpdatedTime = row.getInstant("event_time");
            if (lastUpdatedTime.isBefore(Instant.parse(input.getEventTime()))) {
                // if last entry is older than now, update it.
                context.newOutputMessage(context.getOutputTopic(), schema).value(input).sendAsync();
            }
            else {
                // otherwise, filter this out since it's older than what's in the DB
            }
            return null; // workaround for Avro auto-type generation bug. See https://stackoverflow.com/questions/62944201/jsonmappingexception-occurs-while-converting-object-to-json-string-org-apache/68087222#68087222
        }
        else {
            context.newOutputMessage(context.getOutputTopic(), schema).value(input).sendAsync();
            // workaround for Avro auto-type generation bug. See https://stackoverflow.com/questions/62944201/jsonmappingexception-occurs-while-converting-object-to-json-string-org-apache/68087222#68087222
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        Function.super.close();
        this.astraDbSession.close();
    }

}
