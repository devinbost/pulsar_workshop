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
package com.example.pulsarworkshop;


import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;

public class AppArgs {

    @Parameter(names = { "--usage", "-us" }, description = "produce or consume")
    public String usage = "produce";
    @Parameter(names = { "--topic-base", "-t" }, description = "Base part of topic")
    public String topicBase = "persistent://public/default/testme";

    @Parameter(names = {"--subscription-name", "-s"}, description = "Name of the subscription")
    public String subscription = "example-subscription";

    @Parameter(names = {"--service-url", "-u"}, description = "Pulsar endpoint.")
    public String serviceUrl = "pulsar://localhost:6650";

    @Parameter(names = {"--token", "-tk"}, description = "Token used for token auth")
    public String token = "exampleToken";

    @Parameter(names = {"--trust-cert-path", "-tc"}, description = "Trust cert file path. Only applicable if debug is false and TLS is true.")
    public String trustCertPath = "/path/to/cert.crt";

    @Parameter(names = {"--enable-tls", "-tls"}, description = "Enable TLS")
    public boolean enableTls = false;

    @Parameter(names = {"--debug"}, description = "Enable debug mode")
    public boolean debug = true;
}

