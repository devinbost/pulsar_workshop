package com.example.pulsarworkshop;

import com.example.pulsarworkshop.exception.InvalidParamException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import javax.jms.JMSContext;

abstract public class NativePulsarCmdApp extends PulsarWorkshopCmdApp {

    public NativePulsarCmdApp(String appName, String[] inputParams) {
        super(appName, inputParams);
        addOptionalCommandLineOption("a", "astra", false, "Whether to use Astra streaming.");
    }

    @Override
    public void processExtendedInputParams() throws InvalidParamException {
        // (Optional) Whether to use Astra Streaming
        useAstraStreaming = processBooleanInputParam("a");
    }

    public PulsarClient createNativePulsarClient() throws PulsarClientException {
        ClientBuilder clientBuilder = PulsarClient.builder();

        String pulsarSvcUrl = pulsarClientConf.getValue("brokerServiceUrl");
        clientBuilder.serviceUrl(pulsarSvcUrl);

        String authPluginClassName = pulsarClientConf.getValue("authPlugin");
        String authParams = pulsarClientConf.getValue("authParams");
        if ( !StringUtils.isAnyBlank(authPluginClassName, authParams) ) {
            clientBuilder.authentication(authPluginClassName, authParams);
        }

        // For Astra streaming, there is no need for this section.
        // But for Luna streaming, they're required if TLS is expected.
        if ( !useAstraStreaming && StringUtils.contains(pulsarSvcUrl, "pulsar+ssl") ) {
            boolean tlsHostnameVerificationEnable = BooleanUtils.toBoolean(
                    pulsarClientConf.getValue("tlsEnableHostnameVerification"));
            clientBuilder.enableTlsHostnameVerification(tlsHostnameVerificationEnable);

            String tlsTrustCertsFilePath =
                    pulsarClientConf.getValue("tlsTrustCertsFilePath");
            if (!StringUtils.isBlank(tlsTrustCertsFilePath)) {
                clientBuilder.tlsTrustCertsFilePath(tlsTrustCertsFilePath);
            }

            boolean tlsAllowInsecureConnection = BooleanUtils.toBoolean(
                    pulsarClientConf.getValue("tlsAllowInsecureConnection"));
            clientBuilder.allowTlsInsecureConnection(tlsAllowInsecureConnection);
        }

        return clientBuilder.build();
    }
}
