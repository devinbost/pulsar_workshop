package com.example.pulsarworkshop;

import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import com.example.pulsarworkshop.util.ClientConnConf;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

abstract public class NativePulsarCmdApp extends PulsarWorkshopCmdApp {
    protected final static String API_TYPE = "native-pulsar";
    public NativePulsarCmdApp(String appName, String[] inputParams) {
        super(appName, inputParams);
        addOptionalCommandLineOption("a", "astra",
                false, "Whether to use Astra streaming.");
    }

    @Override
    public void processExtendedInputParams() throws InvalidParamException {
        // (Optional) Whether to use Astra Streaming
        useAstraStreaming = processBooleanInputParam("a", true);
    }

    public PulsarClient createNativePulsarClient() throws PulsarClientException {
        ClientBuilder clientBuilder = PulsarClient.builder();

        String pulsarSvcUrl = clientConnConf.getValue("brokerServiceUrl");
        clientBuilder.serviceUrl(pulsarSvcUrl);

        String authPluginClassName = clientConnConf.getValue("authPlugin");
        String authParams = clientConnConf.getValue("authParams");
        if ( !StringUtils.isAnyBlank(authPluginClassName, authParams) ) {
            clientBuilder.authentication(authPluginClassName, authParams);
        }

        // For Astra streaming, there is no need for this section.
        // But for Luna streaming, they're required if TLS is expected.
        if ( !useAstraStreaming && StringUtils.contains(pulsarSvcUrl, "pulsar+ssl") ) {
            boolean tlsHostnameVerificationEnable = BooleanUtils.toBoolean(
                    clientConnConf.getValue("tlsEnableHostnameVerification"));
            clientBuilder.enableTlsHostnameVerification(tlsHostnameVerificationEnable);

            String tlsTrustCertsFilePath =
                    clientConnConf.getValue("tlsTrustCertsFilePath");
            if (!StringUtils.isBlank(tlsTrustCertsFilePath)) {
                clientBuilder.tlsTrustCertsFilePath(tlsTrustCertsFilePath);
            }

            boolean tlsAllowInsecureConnection = BooleanUtils.toBoolean(
                    clientConnConf.getValue("tlsAllowInsecureConnection"));
            clientBuilder.allowTlsInsecureConnection(tlsAllowInsecureConnection);
        }

        return clientBuilder.build();
    }
}
