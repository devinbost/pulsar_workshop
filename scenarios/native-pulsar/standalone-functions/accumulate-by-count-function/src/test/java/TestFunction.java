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
import com.example.pulsarworkshop.DeviceTS;
import com.example.pulsarworkshop.DeviceTSList;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestFunction {

    private List<DeviceTS> collection;
    @Test
    public void testAccumulator(){
        this.collection = new ArrayList<DeviceTS>();
        //when(mockContext.newOutputMessage(topic, schema))
        var mockdevice = new DeviceTS();
        var result = accumulate(mockdevice);
        Assertions.assertEquals(1, this.collection.size());
        Assertions.assertNull(result);
        for (int i = 0; i < 99; i++) {
            result = accumulate(mockdevice);
            Assertions.assertNull(result);
        }
        Assertions.assertEquals(100, collection.size());
        result = accumulate(mockdevice);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, collection.size());
    }
    public DeviceTSList accumulate(DeviceTS input){
        collection.add(input);
        if (collection.size() > 100) {
            var list = new DeviceTSList();
            list.setDeviceTSArray(collection);
            collection.clear();
            return list;
        }
        return null;
    }
}
