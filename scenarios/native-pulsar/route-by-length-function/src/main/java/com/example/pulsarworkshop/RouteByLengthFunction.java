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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.Arrays;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

public class RouteByLengthFunction implements Function<String, Void> {
    private Logger logger;
    private ObjectMapper mapper;
    private Schema<DeviceTSList> schemaList;
    private Schema<DeviceTS> schemaSingle;

    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.schemaList = Schema.AVRO(DeviceTSList.class);
        this.schemaSingle = Schema.AVRO(DeviceTS.class);
    }
    @Override
    public Void process(String input, Context context) throws Exception {
        // derive key from input
        var myObjects = Arrays.asList(mapper.readValue(input, DeviceTS[].class));
        if (myObjects.size() > 1){
            var deviceList = new DeviceTSList();
            deviceList.setDeviceTSArray(myObjects);
            context.newOutputMessage(context.getOutputTopic() + "-list", schemaList).value(deviceList).sendAsync();
        } else {
            context.newOutputMessage(context.getOutputTopic() + "-single", schemaSingle).value(myObjects.get(0)).sendAsync();
        }
        return null; // workaround for Avro auto-type generation bug. See https://stackoverflow.com/questions/62944201/jsonmappingexception-occurs-while-converting-object-to-json-string-org-apache/68087222#68087222
    }

    @Override
    public void close() throws Exception {
        Function.super.close();
    }

}
