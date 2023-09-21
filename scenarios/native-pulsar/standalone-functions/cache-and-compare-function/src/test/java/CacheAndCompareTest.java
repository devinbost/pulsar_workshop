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
import static org.mockito.Mockito.mock;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.example.pulsarworkshop.AutoProduct;
import com.example.pulsarworkshop.AutoProductList;
import com.example.pulsarworkshop.CacheAndCompareFunction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.Arrays;
import java.util.List;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.TypedMessageBuilder;
import org.apache.pulsar.functions.api.Context;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.concurrent.CompletableFuture;
import static org.mockito.Mockito.*;
public class CacheAndCompareTest {
    @Mock
    private Context contextMock;

    @Captor
    private ArgumentCaptor<AutoProduct> productCaptor;
    @Mock
    private CqlSession cqlSessionMock;

    @Mock
    private TypedMessageBuilder mockContextResponse;

    @Mock
    private ResultSet resultSetMock;

    private static ObjectMapper objectMapper;

    @BeforeAll
    static public void setupOnce(){
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @BeforeEach
    public void setup() throws PulsarClientException {
        MockitoAnnotations.openMocks(this);
        when(contextMock.newOutputMessage(anyString(), any())).thenReturn(mock(TypedMessageBuilder.class));

        mockContextResponse = mock(TypedMessageBuilder.class);
        when(contextMock.newOutputMessage(any(), any())).thenReturn(mockContextResponse);
        when(mockContextResponse.value(any())).thenReturn(mockContextResponse);
        var mockFuture = mock(CompletableFuture.class);
        when(mockContextResponse.sendAsync()).thenReturn(mockFuture);

        resultSetMock = mock(ResultSet.class);
        when(cqlSessionMock.execute(anyString())).thenReturn(resultSetMock);

    }

//    @Test
//    public void test1() throws Exception {
//        // Make sure it loads data from AstraDB
//        var func = new CacheAndCompareFunction();
//        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
//        var session = EmbeddedCassandraServerHelper.getSession();
//        func.setAstraDbSession(session);
//        new CQLDataLoader(session).load(new ClassPathCQLDataSet("people.cql", "people"));
//
//    }
    @Test
    public void test2() throws Exception {
        // When sent a sequence of two identical files, the function should emit nothing (null for each)
        String jsonString = "{\"products\":[{\"product_id\":\"p12345\",\"product_name\":\"Car Model A\",\"description\":\"A classic car model\",\"price\":50000.0,\"store_id\":\"s001\"},{\"product_id\":\"p67890\",\"product_name\":\"Car Model B\",\"description\":\"A modern car model\",\"price\":55000.0,\"store_id\":\"s002\"}]}";

        AutoProductList productList = objectMapper.readValue(jsonString, AutoProductList.class);

        var func = new CacheAndCompareFunction();
        func.astraDbSession = cqlSessionMock;

        // Test case where DB is empty:
        List<Row> results = Arrays.asList();
        when(resultSetMock.all()).thenReturn(results);

        func.process(productList, contextMock);

        verify(contextMock, atLeastOnce()).newOutputMessage(any(), any());
        verify(mockContextResponse, atLeastOnce()).value(productCaptor.capture());


        List<AutoProduct> capturedProducts = productCaptor.getAllValues();
        // When DB is empty, then each incoming product should end up on the capturedProducts list:
        Assert.assertTrue(capturedProducts.size() == 2);
        Assert.assertTrue(capturedProducts.get(0).getProductId().toString().equals("p12345")
        || capturedProducts.get(1).getProductId().toString().equals("p12345"));
        Assert.assertTrue(capturedProducts.get(0).getProductId().toString().equals("p67890")
                || capturedProducts.get(1).getProductId().toString().equals("p67890"));
        // Make sure the products aren't the same:
        Assert.assertTrue(!capturedProducts.get(0).getProductId().equals(capturedProducts.get(1).getProductId()));
    }
    @Test
    public void test21() throws Exception {
        // Next, test the situation where the DB has all the values.
    }
    @Test
    public void test3() throws Exception {
        // When second file has an updated row, the function should emit the updated row
    }
    @Test
    public void test4() throws Exception {
        // When second file has a missing row, the function should emit the missing row with a null value
    }
    @Test
    public void test5() throws Exception {
        // When second file has a new row, the function should emit the new row
    }
    @Test
    public void test6() throws Exception {
        // Test that the data is correctly loaded from the DB upon startup.
    }
}
