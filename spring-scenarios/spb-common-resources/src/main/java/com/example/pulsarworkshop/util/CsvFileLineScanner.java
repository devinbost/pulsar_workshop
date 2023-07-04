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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;

public class CsvFileLineScanner {

    private File csvFile;

    private LineIterator lineIterator;

    public CsvFileLineScanner(File file) throws IOException  {
        this.csvFile = file;
        this.lineIterator = FileUtils.lineIterator(csvFile, "UTF-8");
    }

    public boolean hasNextLine() {
        return lineIterator.hasNext();
    }

    public String getNextLine() {
        return lineIterator.nextLine();
    }

    public void close() throws IOException {
        if (lineIterator != null) {
            lineIterator.close();
        }
    }

}
