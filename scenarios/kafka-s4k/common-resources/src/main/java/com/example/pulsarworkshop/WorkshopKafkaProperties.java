package com.example.pulsarworkshop;

import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

public class WorkshopKafkaProperties {

    private final Properties configProps = new Properties();

    public WorkshopKafkaProperties(File propFile) throws WorkshopRuntimException {
        String canonicalFilePath = "";

        try {
            canonicalFilePath = propFile.getCanonicalPath();

            Parameters params = new Parameters();

            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.properties()
                                    .setFileName(canonicalFilePath));

            Configuration config = builder.getConfiguration();

            for (Iterator<String> it = config.getKeys(); it.hasNext(); ) {
                String confKey = it.next();
                String confVal = config.getProperty(confKey).toString();

                if (!StringUtils.isBlank(confVal)) {
                    configProps.put(confKey, confVal);
                }
            }
        } catch (IOException ioe) {
            throw new WorkshopRuntimException("Can't read the specified properties file!");
        } catch (ConfigurationException cex) {
            throw new WorkshopRuntimException(
                    "Error loading configuration items from the specified properties file: " + canonicalFilePath);
        }
    }

    public Properties getConfigProps() {
        return configProps;
    }

    public String getValue(String confKey) {
        if (StringUtils.isNotBlank(confKey))
            return ((String) configProps.get(confKey));
        else
            return "";
    }

    public String toString() {
        return new ToStringBuilder(this).
                append("clientConfMap", configProps.toString()).
                toString();
    }
}
