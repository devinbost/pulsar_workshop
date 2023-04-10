package com.example.pulsarworkshop;

import com.example.pulsarworkshop.exception.HelpExitException;
import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;
import com.example.pulsarworkshop.util.PulsarClientConf;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

abstract public class NativePulsarCmdApp extends PulsarWorkshopCmdApp {

    public NativePulsarCmdApp(String appName, String[] inputParams) {
        super(appName, inputParams);
    }

    public PulsarClientConf getPulsarClientConf() {
        PulsarClientConf pulsarClientConf = null;
        if (clientConnFile != null) {
            pulsarClientConf = new PulsarClientConf(clientConnFile);
        }
        if (pulsarClientConf == null) {
            throw new WorkshopRuntimException(
                    "Can't properly read the Pulsar connection information from the \"client.conf\" file!");
        }
        return pulsarClientConf;
    }
    
    public PulsarClient createNativePulsarClient() throws PulsarClientException {
        ClientBuilder clientBuilder = PulsarClient.builder();

        PulsarClientConf clientConf = getPulsarClientConf();

        String pulsarSvcUrl = clientConf.getValue("brokerServiceUrl");
        clientBuilder.serviceUrl(pulsarSvcUrl);

        String authPluginClassName = clientConf.getValue("authPlugin");
        String authParams = clientConf.getValue("authParams");
        if ( !StringUtils.isAnyBlank(authPluginClassName, authParams) ) {
            clientBuilder.authentication(authPluginClassName, authParams);
        }

        // For Astra streaming, there is no need for this section.
        // But for Luna streaming, they're required if TLS is expected.
        if ( !useAstraStreaming && StringUtils.contains(pulsarSvcUrl, "pulsar+ssl") ) {
            boolean tlsHostnameVerificationEnable = BooleanUtils.toBoolean(
                    clientConf.getValue("tlsEnableHostnameVerification"));
            clientBuilder.enableTlsHostnameVerification(tlsHostnameVerificationEnable);

            String tlsTrustCertsFilePath =
                    clientConf.getValue("tlsTrustCertsFilePath");
            if (!StringUtils.isBlank(tlsTrustCertsFilePath)) {
                clientBuilder.tlsTrustCertsFilePath(tlsTrustCertsFilePath);
            }

            boolean tlsAllowInsecureConnection = BooleanUtils.toBoolean(
                    clientConf.getValue("tlsAllowInsecureConnection"));
            clientBuilder.allowTlsInsecureConnection(tlsAllowInsecureConnection);
        }

        return clientBuilder.build();
    }
}
