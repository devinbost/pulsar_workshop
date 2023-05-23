package com.example.pulsarworkshop.util;

import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.pojo.IoTSensorData;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class SpringPulsarCmdAppUtils {
    public static String getLogFileName(String apiType, String appName) {
        return apiType + "-" + appName;
    }

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

    public static File processFileInputParam(String filePathInputParam) throws InvalidParamException {
        File file;

        try {
            file = new File(filePathInputParam);
            file.getCanonicalPath();
        } catch (IOException ex) {
            throw new InvalidParamException("Invalid input file parameter: '" + filePathInputParam + "'!");
        }
        return file;
    }

    public static void processNumMsgInputParam(int intValInputParam) throws InvalidParamException {
        if ( (intValInputParam <= 0) && (intValInputParam != -1)) {
            throw new InvalidParamException("Invalid input message number parameter: '" + intValInputParam + "'!");
        }
    }

    public static void processTopicNameInputParam(String topicNameInputParam) throws InvalidParamException {
        if (StringUtils.isBlank(topicNameInputParam)) {
            throw new InvalidParamException("Invalid input topic name parameter: '" + topicNameInputParam + "'!");
        }
    }

    public static void processSubTypeInputParam(String subTypeInputParam) throws InvalidParamException {
        if (!StringUtils.equalsAnyIgnoreCase(subTypeInputParam,
                "exclusive", "fail_over", "shared", "key_shared")) {
            throw new InvalidParamException("Invalid input subscription type parameter: '" + subTypeInputParam + "'!");
        }
    }
}
