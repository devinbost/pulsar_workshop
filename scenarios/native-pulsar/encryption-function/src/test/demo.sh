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