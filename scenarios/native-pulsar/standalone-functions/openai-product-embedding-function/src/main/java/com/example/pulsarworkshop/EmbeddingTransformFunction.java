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

import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

public class EmbeddingTransformFunction implements Function<Order, Void> {
    private Logger logger;
    private Schema<OrderWithEmbedding> schema;

    private String openAiToken;

    private OpenAiService openAiService;
    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.schema = Schema.AVRO(OrderWithEmbedding.class);
        setConfigs();
        this.openAiService = new OpenAiService(this.openAiToken);
    }
    public void setConfigs(){
        // For future, use Pulsar Secret instead of config.properties for improved security.
        try (InputStream input = EmbeddingTransformFunction.class.getResourceAsStream("/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            this.openAiToken = prop.getProperty("OPEN_AI_TOKEN");

        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }
    @Override
    public Void process(Order input, Context context) throws Exception {
        var newObj = processLogic(input, context);

        context.newOutputMessage(context.getOutputTopic(), schema).value(newObj).sendAsync();
        return null;
    }
    public OrderWithEmbedding processLogic(Order input, Context context) throws Exception {

        var productTextList = new ArrayList<String>();
        productTextList.add(input.getProductName().toString() + " - " + input.getProductDescription().toString());
        // (Concatenating to simplify by producing a single embedding.)
        // Creating separate embeddings instead might improve accuracy of future models.

        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                .model("text-embedding-ada-002")
                .input(productTextList)
                .build();

        var embedding = this.openAiService.createEmbeddings(embeddingRequest).getData().get(0).getEmbedding();

        var newObj = new OrderWithEmbedding(input.getOrderId(), input.getCustomerId(), input.getCustomerFirstName(), input.getCustomerLastName(), input.getCustomerEmail(), input.getCustomerPhone(), input.getCustomerAddress(), input.getProductId(), input.getProductName(), input.getProductDescription(), input.getProductPrice(), input.getOrderQuantity(), input.getOrderDate(), input.getTotalAmount(), input.getShippingAddress(), embedding);
        return newObj;
    }

    @Override
    public void close() throws Exception {
        Function.super.close();
    }
}
