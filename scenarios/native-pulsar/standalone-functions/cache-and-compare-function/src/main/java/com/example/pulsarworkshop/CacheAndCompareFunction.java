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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.slf4j.Logger;

public class CacheAndCompareFunction implements Function<AutoProductList, Void> {
    private Logger logger;
    private ObjectMapper mapper;
    private String outputTopic;

    public CqlSession astraDbSession;

    private PreparedStatement preparedSelect;
    private String dbClientId;
    private String dbClientSecret;
    private Schema<AutoProduct> schema;

    private boolean initalized;

    // Exposed just for tests.
    public void setAstraDbSession(CqlSession astraDbSession) {
        this.astraDbSession = astraDbSession;
    }

    public void setLogger(Logger logger){
        this.logger = logger;
    }

    @Override
    public void initialize(Context context) throws Exception {
        this.logger = context.getLogger();
        Function.super.initialize(context);
        this.schema = Schema.AVRO(AutoProduct.class);

        this.setConfigs();
        this.astraDbSession = CqlSession.builder()
                .withCloudSecureConnectBundle(CacheAndCompareFunction.class.getResourceAsStream("/secure-connect-demo.zip"))
                .withAuthCredentials(this.dbClientId,this.dbClientSecret)
                .withKeyspace("device")
                .build();

        this.logger = context.getLogger();
    }
    public void setConfigs(){
        // For future, use Pulsar Secret instead of config.properties for improved security.
        try (InputStream input = CacheAndCompareFunction.class.getResourceAsStream("/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            this.dbClientSecret = prop.getProperty("DB_CLIENT_SECRET");
            this.dbClientId = prop.getProperty("DB_CLIENT_ID");

        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public Cache<String, AutoProduct>
            queue = new Cache2kBuilder<String, AutoProduct>() {}
            .build();

    public String computeHash(AutoProduct product) {
        // Return computed hash
        return product.getProductId().toString()
                + product.getProductName().toString()
                + product.getPrice()
                + product.getDescription().toString()
                + product.getStoreId().toString();  // This is a mock; actual implementation may vary.
    }

    public boolean isProductInCache(AutoProduct product) {
        return queue.containsKey(product.getProductId().toString());
    }

    public void regenerateCache(List<AutoProduct> productList) {
        queue.removeAll(); // Clear the cache
        for (AutoProduct product : productList) {
            queue.put(product.getProductId().toString(), product);
        }
    }

    public void syncWithCassandra(List<AutoProduct> incomingList, Context context) throws PulsarClientException {

        List<String> queryList = new ArrayList<>();

        for (AutoProduct product : incomingList) {
            if (!isProductInCache(product)) {
                // INSERT logic
                var result = context.newOutputMessage(context.getOutputTopic(), schema);
                var val = result.value(product);
                val.sendAsync();
            } else {
                AutoProduct cachedProduct = queue.get(product.getProductId().toString());
                String newHash = computeHash(product);
                String cacheHash = computeHash(cachedProduct);
                if (!newHash.equals(cacheHash)) { // Check if existing record matches incoming record
                    // UPDATE logic (if not exact match)
                    context.newOutputMessage(context.getOutputTopic(), schema).value(product).sendAsync();
                    queue.remove(product.getProductId().toString());
                } else { // Record is exact match.
                    // SKIP record. (Ensure it's not deleted)
                    queue.remove(product.getProductId().toString());
                }
            }
        }

        for (String productId : queue.asMap().keySet()) {
            // DELETE logic for any remaining, assumes above operations were synchronous
            var product = queue.get(productId);
            // TODO: need to set values to null
            context.newOutputMessage(context.getOutputTopic(), schema).value(product).sendAsync();
        }

        regenerateCache(incomingList);
    }
    public Stream<AutoProduct> getProductsFromCassandra(){
        String selectQuery = "SELECT product_id, product_name, description, price, store_id FROM workshop.auto_products;";
        var resultSet = this.astraDbSession.execute(selectQuery).all().parallelStream().map(
                row -> {
                    AutoProduct product = new AutoProduct();
                    product.setProductId(row.getString("product_id"));
                    product.setProductName(row.getString("product_name"));
                    product.setDescription(row.getString("description"));
                    product.setPrice(row.getDouble("price"));
                    product.setStoreId(row.getString("store_id"));
                    return product;
                }
        );
        return resultSet;
    }
    public void populateCacheFromCassandra() {
        var resultSet = getProductsFromCassandra();
        resultSet.forEach(product ->
                queue.put(product.getProductId().toString(), product));
    }

    /*
    Flow is that a file comes in with new and updated products.
     For each product, check the cache. If outdated, update the cache and write the change to the DB.
     After processing the entire file, any products that weren't touched in the cache are no longer valid and
     should result in deletions in the DB.
     */
    @Override
    public Void process(AutoProductList input, Context context) throws Exception {
        if(!this.initalized){
            populateCacheFromCassandra();
            this.initalized = true;
        }
        syncWithCassandra(input.getProducts(), context);
        return null;
    }

    @Override
    public void close() throws Exception {
        Function.super.close();
        this.astraDbSession.close();
    }

}
