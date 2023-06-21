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
package com.datastax.demo.example;

import com.example.pulsarworkshop.DeviceTS;
import com.example.pulsarworkshop.DeviceTSNew;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.common.schema.KeyValue;
import org.slf4j.Logger;

public class TransformFunction implements Function<GenericObject, Void> {
    private Logger logger;
    private Schema<DeviceTSNew> schema;
    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.schema = Schema.AVRO(DeviceTSNew.class);
    }
    @Override
    public Void process(GenericObject input, Context context) throws Exception {
        var record = extractRecord(input);
        var newRecord = processLogic(record, context);
        context.newOutputMessage(context.getOutputTopic(), schema).value(newRecord).sendAsync();
        return null;
    }
    public DeviceTSNew extractRecord(GenericObject genericObject) {
        KeyValue<GenericRecord, GenericRecord> keyValue = (KeyValue<GenericRecord, GenericRecord>) genericObject.getNativeObject();
        GenericRecord keyGenObject = keyValue.getKey();
        GenericRecord valGenObject = keyValue.getValue();

        var tag_id = (String) keyGenObject.getField("tag_id");
        var data_quality = (Integer) keyGenObject.getField("data_quality");
        var event_time = Instant.ofEpochMilli((Long)keyGenObject.getField("event_time")).toString();
        var event_value = (Double) valGenObject.getField("event_value");


        DeviceTSNew deviceTS = new DeviceTSNew();
        deviceTS.setTagId(tag_id);
        deviceTS.setDataQuality(data_quality);
        deviceTS.setEventTime(event_time);
        deviceTS.setEventValue(event_value);
        return deviceTS;
    }
    public DeviceTSNew processLogic(DeviceTSNew input, Context context) throws Exception {
        var timestampString = input.getEventTime().toString();
        TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(timestampString);
        Instant i = Instant.from(ta);
        Date date = Date.from(i);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        String monthPadded = String.format("%02d" , month);
        var yyyymm = Integer.valueOf(year + monthPadded);

        var newObj = new DeviceTSNew(input.getTagId(), input.getDataQuality(), input.getEventTime(),
                input.getEventValue(), yyyymm.intValue());
        return newObj;
    }

    @Override
    public void close() throws Exception {
        Function.super.close();
    }
}
