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
package com.example.pulsarworkshop.pojo;

import java.util.regex.Pattern;

public class IoTSensorDataUtils {
    public static IoTSensorData csvToPojo(String csvLine) {
        String csvLineNoQuote = csvLine.replaceAll("\"", "");
        Pattern pattern = Pattern.compile(",");
        String[] fields = pattern.split(csvLineNoQuote);

        return IoTSensorData.builder()
                .ts(fields[0])
                .device(fields[1])
                .co(Double.parseDouble(fields[2]))
                .humidity(Double.parseDouble(fields[3]))
                .light(Boolean.parseBoolean(fields[4]))
                .lpg(Double.parseDouble(fields[5]))
                .motion(Boolean.parseBoolean(fields[6]))
                .smoke(Double.parseDouble(fields[7]))
                .temp(Double.parseDouble(fields[8]))
                .build();
    }
}
