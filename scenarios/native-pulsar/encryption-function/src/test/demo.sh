#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

cd /Users/devin.bost/Downloads/apache-pulsar-2.11.0
bin/pulsar standalone
bin/pulsar-admin functions localrun \
  --jar  /Users/devin.bost/Downloads/java/load-demo/target/load-demo-0.0.1-jar-with-dependencies.jar \
  --classname com.datastax.demo.fico.EncryptionFunction \
  --inputs persistent://public/default/credit-inquiries3 \
  --output persistent://public/default/credit-inquiries6 \
  --client-auth-params file:///Users/devin.bost/Downloads/apache-pulsar-2.10.3/token.jwt \
  --client-auth-plugin org.apache.pulsar.client.impl.auth.AuthenticationToken \
  --use-tls true \
  --tls-trust-cert-path