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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.apache.avro.generic.GenericData;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.functions.api.utils.FunctionRecord;
import org.slf4j.Logger;
// Prod version would use the ConcurrentHashMap to cache the schemas for performance.
// See example here: https://github.com/datastax/pulsar-transformations/blob/91db9a0d4374c9cc98e97ff453b7f950183bf5a6/pulsar-transformations/src/main/java/com/datastax/oss/pulsar/functions/transforms/DropFieldStep.java#L77

/// Note: This function only works in the Datastax distribution of Pulsar
public class EncryptionFunction implements Function<GenericObject, Record<GenericObject>> {

    public Set<String> sensitiveFieldNames = new HashSet<>();
    public String encryptionKeyIdName = "user_id";

    public LookupService lookupService = new LookupService();
    public Logger logger;

    public EncryptionFunction() throws NoSuchAlgorithmException {
    }

    @Override
    public void initialize(Context context) throws Exception {
        Function.super.initialize(context);
        logger = context.getLogger();
        String sensitiveFieldString = (String) context.getUserConfigValueOrDefault("sensitiveFields", "note");

        if (sensitiveFieldString.length() > 0){
            if (sensitiveFieldString.indexOf(",") > 0) { // Process multiple fields
                sensitiveFieldNames = new HashSet<String>(Arrays.asList(sensitiveFieldString.split(",")));
            } else {
                sensitiveFieldNames.add(sensitiveFieldString);
            }
        } else {
            throw new NullPointerException("Function was initialized with no values for: sensitiveFields in the userConfig");
        }
        encryptionKeyIdName = (String) context.getUserConfigValueOrDefault("encryptionKeyName", "user_id");
    }

    @Override
    public Record<GenericObject> process(GenericObject input, Context context) throws Exception {
        org.apache.avro.generic.GenericRecord contentRecord = (org.apache.avro.generic.GenericRecord) input.getNativeObject();
        var avroSchema = contentRecord.getSchema();
        var keyIdInRecord =  String.valueOf(contentRecord.get(encryptionKeyIdName)); // encryptionKeyIdName = "user_id" by default but could be overridden in userConfig
        if (sensitiveFieldNames.contains(keyIdInRecord)){
            throw new IllegalArgumentException("Can't sanitize the ID we're using to get encryption/decryption since it must be available for the consumer's lookup");
        }
        String encryptionKey = lookupService.lookup(keyIdInRecord);
        if (encryptionKey ==  null) {
            // no key for the user, so we've cryptographically shredded their data.
            return null;
        }

        GenericRecord newRecord = buildNewRecord(avroSchema);
        for (org.apache.avro.Schema.Field field : avroSchema.getFields()) {
            if (sensitiveFieldNames.contains(field.name())){
                 var afterStuffDone =  contentRecord.get(field.name());
                newRecord.put(field.name() + "_encrypted", lookupService.encrypt(afterStuffDone.toString(), encryptionKey));
            } else {
                newRecord.put(field.name(), contentRecord.get(field.name()));
            }

        }
        org.apache.pulsar.client.api.Schema valueSchema = org.apache.pulsar.client.api.Schema.NATIVE_AVRO(newRecord.getSchema());

        FunctionRecord.FunctionRecordBuilder<GenericObject> recordBuilder =
                context
                        .newOutputRecordBuilder(valueSchema)
                        .value(serializeGenericRecord(newRecord));

        return recordBuilder.build();
    }

    private GenericRecord buildNewRecord(org.apache.avro.Schema avroSchema) {
        var newSensitiveFields = avroSchema
                .getFields()
                .stream()
                .filter(f -> sensitiveFieldNames.contains(f.name()))
                .map(f -> new org.apache.avro.Schema.Field(f.name() + "_encrypted",
                        Schema.createUnion(org.apache.avro.Schema.create(Schema.Type.NULL),
                                        org.apache.avro.Schema.create(Schema.Type.STRING))
                        , f.doc(), f.defaultVal(), f.order()));
        var newNonSensitiveFields = avroSchema
                .getFields()
                .stream()
                .filter(f -> !sensitiveFieldNames.contains(f.name()))
                .map(f -> new org.apache.avro.Schema.Field(f.name(), f.schema(), f.doc(), f.defaultVal(), f.order()));

        var combinedFields = Stream.concat(newSensitiveFields, newNonSensitiveFields)
                .collect(Collectors.toList());

        org.apache.avro.Schema newFieldsWrapper =
                org.apache.avro.Schema.createRecord(
                        avroSchema.getName() + "Encrypted",
                        avroSchema.getDoc(),
                        avroSchema.getNamespace(),
                        avroSchema.isError(),
                        combinedFields);

        GenericRecord newRecord = new GenericData.Record(newFieldsWrapper);
        return newRecord;
    }

    public static byte[] serializeGenericRecord(org.apache.avro.generic.GenericRecord record) throws IOException {
        GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(record.getSchema());
        ByteArrayOutputStream oo = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().directBinaryEncoder(oo, null);
        writer.write(record, encoder);
        return oo.toByteArray();
    }

    @Override
    public void close() throws Exception {
        Function.super.close();
    }
}
