package com.example.pulsarworkshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class DeduplicationFunction implements Function<DeviceTS, Void> {
    private Logger logger;
    private ObjectMapper mapper;
    private String outputTopic;

    private Schema<DeviceTS> schema;

    private Cache<Object, Object> cache;
    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.schema = Schema.AVRO(DeviceTS.class);

        this.cache = CacheBuilder.newBuilder()
                .maximumSize(100000) // Can be tuned.
                .build();
    }
    @Override
    public Void process(DeviceTS input, Context context) throws Exception {
        // derive key from input
        var key = input.getTagId().toString() + input.getEventTime().toString() + input.getDataQuality();

        if (cache.getIfPresent(key) != null){
            return null; // we received a duplicate because it's already in the cache.
        }
        else {
            cache.put(key, input);
            context.newOutputMessage(context.getOutputTopic(), schema).value(input).sendAsync();
        }
        return null; // workaround for Avro auto-type generation bug. See https://stackoverflow.com/questions/62944201/jsonmappingexception-occurs-while-converting-object-to-json-string-org-apache/68087222#68087222
    }

    @Override
    public void close() throws Exception {
        Function.super.close();
    }

}
