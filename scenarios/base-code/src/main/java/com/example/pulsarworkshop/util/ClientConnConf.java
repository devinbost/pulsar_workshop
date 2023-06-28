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
package com.example.pulsarworkshop.util;

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
import java.util.*;

public class ClientConnConf {
    private final Map<String, String> clientConfMap = new HashMap<>();

    public ClientConnConf(File clientConnFile) throws WorkshopRuntimException {
        String canonicalFilePath = "";

        try {
            canonicalFilePath = clientConnFile.getCanonicalPath();

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
                    clientConfMap.put(confKey, confVal);
                }
            }
        } catch (IOException ioe) {
            throw new WorkshopRuntimException("Can't read the specified properties file!");
        } catch (ConfigurationException cex) {
            throw new WorkshopRuntimException(
                    "Error loading configuration items from the specified properties file: " + canonicalFilePath);
        }
    }

    public String getValue(String confKey) {
        if (StringUtils.isNotBlank(confKey))
            return clientConfMap.get(confKey);
        else
            return "";
    }

    public String toString() {
        return new ToStringBuilder(this).
                append("clientConfMap", clientConfMap.toString()).
                toString();
    }
    public Map<String, String> getClientConfMap() {
         return this.clientConfMap; 
    }
}
