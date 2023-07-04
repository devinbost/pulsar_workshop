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
import com.example.pulsarworkshop.TransformFunction;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.apache.pulsar.functions.api.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import static org.mockito.Mockito.*;

public class Tests {

    @Test
    public void testDateConversion(){
        var timestampString = "2023-05-24T00:00:00Z";

        TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(timestampString);
        Instant i = Instant.from(ta);
        Date date = Date.from(i);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        String monthPadded = String.format("%02d" , month);
        var yyyymm = Integer.valueOf(year + monthPadded);
        // Ensure logic is correct for converting input field value to target yyyymm format
        Assertions.assertEquals(202305, yyyymm);
    }
    @Test
    public void testTransformFunction() throws Exception {
        var fn = new TransformFunction();
        //[{"tag_id": "tag1", "data_quality": 7, "event_time": "2023-05-24T00:00:00Z", "event_value": 34.56},
        var input = new DeviceTS("tag1", 7, "2023-05-24T00:00:00Z", 34.56);
        var mockContext = mock(Context.class);
        var mockLogger = mock(Logger.class);
        when(mockContext.getLogger()).thenReturn(mockLogger);
        var result = fn.processLogic(input, mockContext);
        Assertions.assertEquals(input.getTagId(), result.getTagId());
        Assertions.assertEquals(input.getEventTime(), result.getEventTime());
        Assertions.assertEquals(input.getEventValue(), result.getEventValue());
        Assertions.assertEquals(input.getDataQuality(), result.getDataQuality());
        Assertions.assertEquals(202305, result.getYyyymm());
    }
}
