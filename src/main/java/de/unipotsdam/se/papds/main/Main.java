package de.unipotsdam.se.papds.main;

import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.sourcepos.Debug;
import com.ibm.wala.util.CancelException;
import de.unipotsdam.se.papds.analysis.demand.Query;
import de.unipotsdam.se.papds.analysis.demand.QueryBuilder;
import org.apache.commons.cli.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;


public class Main {

    public static String scopeFileName;
    public static String mainClass;
    public static String exFile;
    public static String mode;

    public static boolean debug = false;

    public static String TARGET = "/path/to/pointerPDS.jar";


    private static Options options = new Options();
    private static CommandLineParser parser = new DefaultParser();
    private static CommandLine cmd = null;
    private static HelpFormatter help = new HelpFormatter();

    public static void loadProperties(String fileName) throws IOException {
        FileInputStream propfile = new FileInputStream(fileName);
        Properties prop = new Properties();
        prop.load(propfile);


        if (!prop.contains("EXCLUDE-FILE")) {
            System.out.println("EXCLUDE-FILE is not set. Using exclude.txt");
        }

        scopeFileName = prop.getProperty("SCOPE-FILE");
        mainClass = prop.getProperty("MAIN-CLASS");
        exFile = prop.getProperty("EXCLUDE-FILE", "exclude.txt");
        mode = prop.getProperty("MODE"); //MODE is either full or demand


        if (mode == null) {
            System.out.println("Mode property not found. Mode should be \"demand\" or \"full\".");
            System.exit(1);
        }

        if (mainClass == null) {
            System.out.println("MAIN-CLASS value not found. Set MAIN-CLASS in properties file.");
            System.exit(1);
        }

        if (scopeFileName == null) {
            System.out.println("Scope File Name not found. Setup Scope file");
            System.exit(1);
        }

        if (!mode.toLowerCase().equals("demand") && !mode.toLowerCase().equals("full")) {
            System.out.println("Mode should be \"demand\" or \"full\"");
            System.exit(1);
        }

        if (mode.toLowerCase().equals("demand")) {
            System.out.println("MODE set to demand. Doing a demand driven pointer analysis for a variable in main main function.");
        }
    }


    public static void showHelp() {
        help.printHelp(TARGET, options);
    }

    public static void main(String[] args) throws IOException, CallGraphBuilderCancelException {


        options.addOption(Option.builder("d").required(false).desc("Run in debug mode").build());
        options.addOption(Option.builder("a").required().hasArg().numberOfArgs(1).argName("ANALYSIS").desc("Choose Analysis <andersen/pds>").build());
        options.addOption(Option.builder("p").required(false).hasArg().numberOfArgs(1).argName("PROPERTIES FILE").desc("Set properties file").build());
        options.addOption(Option.builder("h").required(false).desc("Print this help").build());
        options.addOption(Option.builder("i").required(false).argName("INPUT JAR FILE").desc("Input Jar File").build());
        options.addOption(Option.builder("m").required(false).argName("MAIN CLASS").desc("Path to Main Class").build());
        options.addOption(Option.builder("r").required(false).argName("REFLECTION OPTION").desc("no-flow-casts\nfull").build());
        options.addOption(Option.builder("full").required(false).desc("Full pointer analysis").build());
        options.addOption(Option.builder("demand").required(false).desc("Demand driven pointer analysis").build());

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            Main.showHelp();
        }


        if (cmd.hasOption("h")) {
            Main.showHelp();
            System.exit(0);
        }

        if (cmd.hasOption('d')) {
            debug = true;
        }

        String reflectionOption = "no-flow-casts";
        if (cmd.hasOption("r")) {
            String optionValue = cmd.getOptionValue("r");
            if (!reflectionOption.toLowerCase().equals("full") && !reflectionOption.toLowerCase().equals("no-flow-casts")) {
                System.out.println("Setting no-flow-casts as reflection options");
            } else {
                reflectionOption = optionValue;
            }
        }


        //Check for the parameters
        if (cmd.hasOption('p')) {
            String properties = cmd.getOptionValue("p");
            loadProperties(properties);
            Driver.setUpFromScopefile(mainClass, exFile, scopeFileName, reflectionOption);
        } else if (cmd.hasOption('i')) {
            mainClass = "L" + cmd.getOptionValue("m");
            //TODO: Allow the analyzer to use option -i
            System.out.println("Not implemented. Wait for future release");
        } else {
            System.out.println("Either -p or -i option is required");
            Main.showHelp();
        }

        if (debug) {
            System.out.println("Setting DEBUG level to DEBUG");
            Debug.setMinLogLevel(Debug.LogLevel.DEBUG);
            Debug.setLogFile("debugfile");
        } else {
            System.out.println("Setting DEBUG level to INFO");
            Debug.setMinLogLevel(Debug.LogLevel.INFO);
        }

        String analysis = cmd.getOptionValue("a");
        if (analysis.equals("andersen")) {
            try {
                Driver.doAndersenAnalysis();
                System.exit(0);
            } catch (CancelException e) {
                e.printStackTrace();
            }
        } else if (analysis.equals("pds")){
            long beginTime = System.currentTimeMillis();

            if (cmd.hasOption("full")) {
                System.out.println("Mode: Full. Doing a full-pointer analysis");
                Driver.buildPAG();
                Driver.fullAnalysis();
            } else if (cmd.hasOption("demand")) {
                Driver.buildPAG();
                //Initialize Query Builder
                QueryBuilder queryBuilder = Driver.queryBuilderInitializer();
                List<Query> queries = queryBuilder.buildPointerQueries();
                for (Query query : queries) {
                    System.out.println("---------------------------------------\n" +
                            "Computing Points-to set for Query: " + query);
                    long queryBeginTime = System.currentTimeMillis();
                    Driver.demandDrivenAnalysis(query);
                    long queryEndTime = System.currentTimeMillis();
                    System.out.println("Total Time for the query = " + (queryEndTime-queryBeginTime)/1000.0 +
                            "sec\n--------------------------------------------------");
                }
            } else {
                System.out.println("Illegal mode. Specify demand driven or full pointer analysis");
                System.exit(1);
            }
            long endTime = System.currentTimeMillis();
            Debug.info("Total Time = " + (endTime - beginTime) / 1000.0 + " sec");
            System.exit(0);
        } else {
            Main.showHelp();
            System.exit(1);
        }
    }
}
