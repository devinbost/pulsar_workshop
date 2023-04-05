package com.example.pulsarworkshop.pojo;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class IoTSensorDataUtils {
    public static IoTSensorData csvToPojo(String csvLine) {
        String csvLineNoQuote = StringUtils.replaceAll(csvLine, "\"", "");
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
