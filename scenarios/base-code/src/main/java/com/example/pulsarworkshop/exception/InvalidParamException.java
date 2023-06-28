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
package com.example.pulsarworkshop.exception;

public class InvalidParamException extends RuntimeException {

    private String errorDescription;

    public InvalidParamException(String paramName, String errDesc) {
        super("Invalid setting for parameter (" + paramName + "): " + errDesc);
        this.errorDescription = "Invalid setting for parameter (" + paramName + "): " + errDesc;
    }

    public InvalidParamException(String fullErrDesc) {
        super(fullErrDesc);
        this.errorDescription = fullErrDesc;
    }

    public String getErrorDescription() { return errorDescription; }

}
