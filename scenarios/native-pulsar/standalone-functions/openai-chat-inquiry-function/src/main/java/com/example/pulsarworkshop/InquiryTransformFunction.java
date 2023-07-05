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
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

public class InquiryTransformFunction implements Function<String, Void> {
    private Logger logger;
    private Schema<InquiryResult> schema;
    private String openAiToken;
    private ObjectMapper mapper;
    private CqlSession astraDbSession;
    private PreparedStatement preparedSelect;
    private String dbClientId;
    private String dbClientSecret;
    private OpenAiService openAiService;
    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.schema = Schema.AVRO(InquiryResult.class);
        setConfigs();
        this.openAiService = new OpenAiService(this.openAiToken);
        this.mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        this.astraDbSession = CqlSession.builder()
                .withCloudSecureConnectBundle(InquiryTransformFunction.class.getResourceAsStream("/secure-connect-demo.zip"))
                .withAuthCredentials(this.dbClientId,this.dbClientSecret)
                .withKeyspace("openai")
                .build();
        this.prepareQueries();
    }
    public void setConfigs(){
        // For future, use Pulsar Secret instead of config.properties for improved security.
        try (InputStream input = InquiryTransformFunction.class.getResourceAsStream("/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            this.openAiToken = prop.getProperty("OPEN_AI_TOKEN");
            this.dbClientSecret = prop.getProperty("DB_CLIENT_SECRET");
            this.dbClientId = prop.getProperty("DB_CLIENT_ID");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }
    @Override
    public Void process(String input, Context context) throws Exception {
        var newObj = processLogic(input, context);

        context.newOutputMessage(context.getOutputTopic(), schema).value(newObj).sendAsync();
        return null;
    }
    public void prepareQueries(){
        String selectQuery = "SELECT order_id, customer_id, customer_first_name, customer_last_name, customer_email, customer_phone, customer_address, product_id, product_name, product_description, product_price, order_quantity, order_date, total_amount, shipping_address, embedding FROM openai.order_with_embedding WHERE customer_id = ? ORDER BY embedding ANN OF ? LIMIT 1";
        this.preparedSelect = this.astraDbSession.prepare(selectQuery);
    }
    public InquiryResult processLogic(String input, Context context) throws Exception {
        // Deserialize the input string into Inquiry type
        // Then, produce this type to Pulsar
        var myInquiry = mapper.readValue(input, Inquiry.class);

        var inquiryList = new ArrayList<String>();
        inquiryList.add(myInquiry.getInquiryText().toString());

        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
            .model("text-embedding-ada-002")
            .input(inquiryList)
            .build();
        // Request embedding of inquiry text
        var embedding = this.openAiService.createEmbeddings(embeddingRequest).getData().get(0).getEmbedding();

        // TODO: Run vector search against table in DB
        var embeddingString = "[" + embedding.stream().map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";

        var boundStatement = this.preparedSelect.bind(myInquiry.getCustomerId(), embeddingString);
        var outputs = this.astraDbSession.execute(boundStatement).all();
        var output = outputs.get(0);
        // product_name, product_description, product_price, order_date
        var product_name = output.getString("product_name");
        var product_description = output.getString("product_description");
        var order_date = output.getString("order_date");

        var inquiryResult = new InquiryResult(product_name, product_description, order_date);
        return inquiryResult;
    }

    @Override
    public void close() throws Exception {
        Function.super.close();
    }
}
