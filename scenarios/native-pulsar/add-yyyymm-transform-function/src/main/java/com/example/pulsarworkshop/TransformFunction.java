package com.example.pulsarworkshop;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

public class TransformFunction implements Function<DeviceTS, Void> {
    private Logger logger;
    private Schema<DeviceTSNew> schema;
    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.schema = Schema.AVRO(DeviceTSNew.class);
    }
    @Override
    public Void process(DeviceTS input, Context context) throws Exception {
        var newObj = processLogic(input, context);

        context.newOutputMessage(context.getOutputTopic(), schema).value(newObj).sendAsync();
        return null;
    }
    public DeviceTSNew processLogic(DeviceTS input, Context context) throws Exception {
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
