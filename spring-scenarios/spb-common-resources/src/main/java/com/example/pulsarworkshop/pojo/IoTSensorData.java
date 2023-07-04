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

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IoTSensorData {
    // Timestamp of reading (scientific notation)
    // e.g. 1.43701862E9
    @Getter @Setter private String ts;
    // Device name
    @Getter @Setter private String device;
    // Carbon monoxide
    @Getter @Setter private double co;
    // Humidity (%)
    @Getter @Setter private double humidity;
    // Light detected?
    @Getter @Setter private boolean light;
    // Liquefied petroleum gas
    @Getter @Setter private double lpg;
    // Motion detected?
    @Getter @Setter private boolean motion;
    // Smoke
    @Getter @Setter private double smoke;
    // Temperature (F)
    @Getter @Setter private double temp;
}
