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
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

public class GroupSortTopFunction implements Function<DeviceTSList, Void> {
    private Logger logger;
    private ObjectMapper mapper;
    private String outputTopic;
    private Schema<DeviceTS> schema;
    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.schema = Schema.AVRO(DeviceTS.class);
    }

    @Override
    public Void process(DeviceTSList input, Context context) throws Exception {
        Map<CharSequence, Optional<DeviceTS>> result = input.getDeviceTSArray().stream()
                .collect(Collectors.groupingBy(DeviceTS::getTagId,
                        Collectors.maxBy(Comparator.comparing(o -> Instant.parse(o.getEventTime().toString())))));
        List<DeviceTS> flatList = result.values().stream()
                .flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toList());
        flatList.stream().forEach( i -> {
            try {
                context.newOutputMessage(context.getOutputTopic(), schema).value(i).sendAsync();
            } catch (PulsarClientException e) {
                throw new RuntimeException(e);
            }
        });
        return null; // workaround for Avro auto-type generation bug. See https://stackoverflow.com/questions/62944201/jsonmappingexception-occurs-while-converting-object-to-json-string-org-apache/68087222#68087222
    }

    @Override
    public void close() throws Exception {
        Function.super.close();
    }

}
