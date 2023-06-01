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
