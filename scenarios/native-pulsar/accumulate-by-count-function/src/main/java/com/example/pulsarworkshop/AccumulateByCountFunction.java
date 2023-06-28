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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;
import org.apache.pulsar.functions.api.Record;

public class AccumulateByCountFunction implements Function<DeviceTS, Void> {
    private Logger logger;
    private ObjectMapper mapper;
    private String outputTopic;
    private Schema<DeviceTSList> schema;
    private List<Pair> collection;
    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.schema = Schema.AVRO(DeviceTSList.class);

        this.collection = new ArrayList<>();
    }
    @Data
    public class Pair {
        public Record<?> record;
        public DeviceTS DeviceTS;
    }
    @Override
    public Void process(DeviceTS input, Context context) throws Exception {
        var pair = new Pair();
        pair.setRecord(context.getCurrentRecord());
        pair.setDeviceTS(input);
        this.collection.add(pair);
        if (this.collection.size() >= 100) {
            var list = new DeviceTSList();
            var devices = this.collection.stream().map(t -> t.getDeviceTS()).collect(Collectors.toList());
            list.setDeviceTSArray(devices);
            context.newOutputMessage(context.getOutputTopic(), schema).value(list).sendAsync();
            // Note: It's possible for duplicates to flow through if this function dies before it flushes.
            this.collection.stream().forEach(t -> t.getRecord().ack());
            this.collection.clear();
            // Make sure retention is enabled since we're automatically acknowledging and don't want data loss
            // before the collection flushes to the stream!
        }
        return null;
    }
    @Override
    public void close() throws Exception {
        Function.super.close();
    }

}
