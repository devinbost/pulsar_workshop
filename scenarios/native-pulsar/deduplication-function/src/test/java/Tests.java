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
import com.example.pulsarworkshop.DeviceTS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Tests {

    @Test
    public void testDeserialization() throws JsonProcessingException {
        String testString = "[{\"tag_id\": \"tag1\", \"data_quality\": 7, \"event_time\": \"2023-05-24T00:00:00Z\", \"event_value\": 34.56}, {\"tag_id\": \"tag2\", \"data_quality\": 5, \"event_time\": \"2023-05-24T02:00:00Z\", \"event_value\": 78.9}, {\"tag_id\": \"tag3\", \"data_quality\": 8, \"event_time\": \"2023-05-24T03:30:00Z\", \"event_value\": 100.01}]";
        var mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        var myObjects = Arrays.asList(mapper.readValue(testString, DeviceTS[].class));
    }

    @Test
    public void testSplitterFunction() throws JsonProcessingException {
        String testString = "[{\"tag_id\": \"tag1\", \"data_quality\": 7, \"event_time\": \"2023-05-24T00:00:00Z\", \"event_value\": 34.56}, {\"tag_id\": \"tag2\", \"data_quality\": 7, \"event_time\": \"2023-05-24T02:00:00Z\", \"event_value\": 78.9}, {\"tag_id\": \"tag3\", \"data_quality\": 7, \"event_time\": \"2023-05-24T03:30:00Z\", \"event_value\": 100.01}]";
        var mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        var myObjects = Arrays.asList(mapper.readValue(testString, DeviceTS[].class));
        // For production, we could create a Stream-based parser to improve performance.
        Assertions.assertEquals(3, myObjects.stream().count());
        myObjects.forEach(entry -> {
            Assertions.assertEquals(7, entry.getDataQuality());
        });
    }

}