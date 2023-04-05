package com.example.pulsarworkshop;

import com.example.pulsarworkshop.util.PulsarClientConf;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.pulsar.client.api.*;

import com.example.pulsarworkshop.exception.HelpExitException;
import com.example.pulsarworkshop.exception.InvalidParamException;
import com.example.pulsarworkshop.exception.WorkshopRuntimException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

abstract public class PulsarWorkshopCmdApp {

    protected String[] rawCmdInputParams;

    // -1 means to process all available messages (indefinitely)
    protected Integer numMsg;
    protected String pulsarTopicName;
    protected File clientConnFile;
    protected boolean useAstraStreaming;

    protected PulsarClientConf pulsarClientConf;

    protected final String appName;

    private CommandLine commandLine;
    private final DefaultParser commandParser;
    private final Options cliOptions = new Options();
    
    public abstract void processExtendedInputParams() throws InvalidParamException;
    public abstract void runApp();
    public abstract void termApp();


    public PulsarWorkshopCmdApp(String appName, String[] inputParams) {
        this.appName = appName;
        this.rawCmdInputParams = inputParams;
        this.commandParser = new DefaultParser();

        addCommandLineOption(new Option("h", "help", false, "Displays the usage method."));
        addCommandLineOption(new Option("n","numMsg", true, "Number of messages to process."));
        addCommandLineOption(new Option("t", "topic", true, "Pulsar topic name."));
        addCommandLineOption(new Option("c","connFile", true, "\"client.conf\" file path."));
        addCommandLineOption(new Option("a", "astra", false, "Whether to use Astra streaming."));
    }

    protected void addCommandLineOption(Option option) {
    	cliOptions.addOption(option);
    }

    public int run() {
        int exitCode = 0;
        try {
            this.processInputParams();
            this.runApp();
        }
        catch (HelpExitException hee) {
            this.usage(appName);
            exitCode = 1;
        }
        catch (InvalidParamException ipe) {
            System.out.println("\n[ERROR] Invalid input value(s) detected!");
            ipe.printStackTrace();
            exitCode = 2;
        }
        catch (WorkshopRuntimException wre) {
            System.out.println("\n[ERROR] Unexpected runtime error detected!");
            wre.printStackTrace();
            exitCode = 3;
        }
        finally {
            this.termApp();
        }
        
        return exitCode;
    }

    public void usage(String appNme) {
        PrintWriter printWriter = new PrintWriter(System.out, true);

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(printWriter, 150, appName,
                "Command Line Options:",
                cliOptions, 2, 1, "", true);

        System.out.println();
    }
    
    public void processInputParams() throws HelpExitException, InvalidParamException {

    	if (commandLine == null) {
            try {
                commandLine = commandParser.parse(cliOptions, rawCmdInputParams);
            } catch (ParseException e) {
                throw new InvalidParamException("Failed to parse application CLI input parameters: " + e.getMessage());
            }
    	}
    	
    	// CLI option for help messages
        if (commandLine.hasOption("h")) {
            throw new HelpExitException();
        }

        // (Required) CLI option for number of messages
        numMsg = processIntegerInputParam("n");
    	if ( (numMsg <= 0) && (numMsg != -1) ) {
    		throw new InvalidParamException("Message number must be a positive integer or -1 (all available raw input)!");
    	}    	

        // (Required) CLI option for Pulsar topic
        pulsarTopicName = processStringInputParam("t");

        // (Optional) CLI option for client.conf file
        clientConnFile = processFileInputParam("c");
        if (clientConnFile != null) {
            pulsarClientConf = new PulsarClientConf(clientConnFile);
        }

        // (Optional) Whether to use Astra Streaming
        useAstraStreaming = processBooleanInputParam("a");

        processExtendedInputParams();
    }

    public boolean processBooleanInputParam(String optionName) {
        Option option = cliOptions.getOption(optionName);

        // Default value if not present on command line
        boolean boolVal;
        String value = commandLine.getOptionValue(option.getOpt());

        if (option.isRequired() && commandLine.getOptionValue(option) == null) {
            throw new InvalidParamException("Empty value for argument '" + optionName +"'");
        }
        boolVal = BooleanUtils.toBoolean(value);

        return boolVal;
    }

    public int processIntegerInputParam(String optionName) {
        Option option = cliOptions.getOption(optionName);
        
        // Default value if not present on command line
        int intVal = 0;
    	String value = commandLine.getOptionValue(option.getOpt());        	

        if (option.isRequired() && commandLine.getOptionValue(option) == null) {
            throw new InvalidParamException("Empty value for argument '" + optionName +"'");
        }
    	intVal = NumberUtils.toInt(value);
        
        return intVal;
    }
    
    public String processStringInputParam(String optionName) {

    	Option option = cliOptions.getOption(optionName);
        String value = commandLine.getOptionValue(option);

        if (option.isRequired() && StringUtils.isBlank(value)) {
            throw new InvalidParamException("Empty value for argument '" + optionName +"'");
        }

        return value;
    }
    
    public File processFileInputParam(String optionName) {
        File file = null;

        Option option = cliOptions.getOption(optionName);
        if (commandLine.getOptionValue(option) != null) {

        	String path = commandLine.getOptionValue(option.getOpt());    	
	        try {
	            file = new File(path);
	            file.getCanonicalPath();
	        } catch (IOException ex) {
	        	throw new InvalidParamException("Invalid file path for param '" + optionName + "': " + path);
	        }
    	}

        return file;
    }

    protected PulsarClientConf getPulsarClientConf() {
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
    
    protected PulsarClient createNativePulsarClient() throws PulsarClientException {
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
