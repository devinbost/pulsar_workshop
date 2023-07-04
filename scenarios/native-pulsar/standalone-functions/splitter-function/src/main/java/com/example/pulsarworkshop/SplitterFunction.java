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
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

public class SplitterFunction implements Function<String, Void> {
    private Logger logger;
    private ObjectMapper mapper;

    private Schema<DeviceTS> schema;
    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.schema = Schema.AVRO(DeviceTS.class);
    }
    @Override
    public Void process(String input, Context context) throws Exception {
        var myObjects = Arrays.asList(mapper.readValue(input, DeviceTS[].class));
        // For production, we could create a Stream-based parser to improve performance.
        myObjects.forEach(entry -> {
            try {
                context.newOutputMessage(context.getOutputTopic(), schema).value(entry).sendAsync();
            } catch (PulsarClientException e) {
                context.getLogger().error(e.toString());
            }
        });
        return null;
    }

    @Override
    public void close() throws Exception {
        Function.super.close();
    }
}
