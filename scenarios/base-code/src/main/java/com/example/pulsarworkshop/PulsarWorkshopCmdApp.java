package com.example.pulsarworkshop;

import com.example.pulsarworkshop.util.PulsarClientConf;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

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

    protected CommandLine commandLine;
    protected final DefaultParser commandParser;
    protected final Options cliOptions = new Options();
    
    public abstract void processExtendedInputParams() throws InvalidParamException;
    public abstract void runApp();
    public abstract void termApp();


    public PulsarWorkshopCmdApp(String appName, String[] inputParams) {
        this.appName = appName;
        this.rawCmdInputParams = inputParams;
        this.commandParser = new DefaultParser();

        addOptionalCommandLineOption("h", "help", false, "Displays the usage method.");
        addRequiredCommandLineOption("n","numMsg", true, "Number of messages to process.");
        addRequiredCommandLineOption("t", "topic", true, "Pulsar topic name.");
        addRequiredCommandLineOption("c","connFile", true, "\"client.conf\" file path.");
    }

    protected void addRequiredCommandLineOption(String option, String longOption, boolean hasArg, String description) {
        Option opt = new Option(option, longOption, hasArg, description);
        opt.setRequired(true);
    	cliOptions.addOption(opt);
    }

    protected void addOptionalCommandLineOption(String option, String longOption, boolean hasArg, String description) {
        Option opt = new Option(option, longOption, hasArg, description);
        opt.setRequired(false);
        cliOptions.addOption(opt);
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

        processExtendedInputParams();
    }

    public boolean processBooleanInputParam(String optionName) {
        Option option = cliOptions.getOption(optionName);

        // Default value if not present on command line
        boolean boolVal = false;

        if (option.isRequired()) {
            String value = commandLine.getOptionValue(option.getOpt());
            if (StringUtils.isBlank(value))
                throw new InvalidParamException("Empty value for argument '" + optionName +"'");
            else
                boolVal=BooleanUtils.toBoolean(value);
        }

        return boolVal;
    }

    public int processIntegerInputParam(String optionName) {
        return processIntegerInputParam(optionName, 0);
    }
    public int processIntegerInputParam(String optionName, int dftValue) {
        Option option = cliOptions.getOption(optionName);

        // Default value if not present on command line
        int intVal = dftValue;

        if (option.isRequired()) {
            String value = commandLine.getOptionValue(option.getOpt());
            if (StringUtils.isBlank(value)) {
                throw new InvalidParamException("Empty value for argument '" + optionName + "'");
            }
            intVal = NumberUtils.toInt(value);
        }

        return intVal;
    }

    public String processStringInputParam(String optionName) {
        return processStringInputParam(optionName, null);
    }
    public String processStringInputParam(String optionName, String dftValue) {
    	Option option = cliOptions.getOption(optionName);

        String strVal = dftValue;

        if (option.isRequired()) {
            String value = commandLine.getOptionValue(option);
            if (StringUtils.isBlank(value)) {
                throw new InvalidParamException("Empty value for argument '" + optionName + "'");
            }
            strVal = value;
        }

        return strVal;
    }
    
    public File processFileInputParam(String optionName) {
        Option option = cliOptions.getOption(optionName);

        File file = null;

        if (option.isRequired()) {
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
}
