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

import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.TypedMessageBuilder;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.apache.pulsar.functions.api.Record;
import org.slf4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddMetadataFunc implements Function<String, Void> {
    private static DateFormat timeFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS");
    private static TypedMessageBuilder<String> outputMessageBuilder;
    private static Logger LOG;

    @Override
    public void initialize(Context context) throws PulsarClientException {
        LOG = context.getLogger();

        String outputTopic = context.getOutputTopic();
        outputMessageBuilder = context.newOutputMessage(outputTopic, Schema.STRING);
    }

    @Override
    public Void process(String input, Context context) {
        Record<?> currentRecord = context.getCurrentRecord();
        Optional<String> keyOpt = currentRecord.getKey();
        Map<String, String> msgProperties = currentRecord.getProperties();

        if (keyOpt.isPresent()) {
            outputMessageBuilder.key(keyOpt.get());
        }
        for (String propKey : msgProperties.keySet()) {
            outputMessageBuilder.property(propKey, msgProperties.get(propKey));
        }
        // a newly added custom metadata
        outputMessageBuilder.property("internal_process_time",
                timeFormat.format(Calendar.getInstance().getTime()) );

        outputMessageBuilder.value(input);

        outputMessageBuilder.sendAsync();

        return null;
    }

    @Override
    public void close() {
    }
}
