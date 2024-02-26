package it.sagelab.reqt;

import it.sagelab.specpro.fe.AbstractLTLFrontEnd;
import it.sagelab.specpro.fe.LTLFrontEnd;
import it.sagelab.specpro.fe.PSPFrontEnd;
import it.sagelab.specpro.fe.kiss.MealyMachineBuilder;
import it.sagelab.specpro.models.ba.BuchiAutomaton;
import it.sagelab.specpro.models.ltl.LTLDcSpec;
import it.sagelab.specpro.models.ltl.LTLSpec;
import it.sagelab.specpro.models.ltl.assign.Trace;
import it.sagelab.specpro.reasoners.LTL2BA;
import it.sagelab.specpro.testing.MealyMachineSUT;
import it.sagelab.specpro.testing.SUT;
import it.sagelab.specpro.testing.TestingEnvironment;
import it.sagelab.specpro.testing.generators.GDFSTestGenerator;
import it.sagelab.specpro.testing.oracles.TestOracle;
import net.openhft.compiler.CompilerUtils;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainCLI {

    private static LTLSpec spec;

    public static SUT getSUT(String type, String sutFile) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        switch (type) {
            case "kiss":
                return new MealyMachineSUT(MealyMachineBuilder.parseKISSFile(sutFile));
            case "nusmv":
                return new NuSMVModelSUT(spec.getInputVariables(), spec.getOutputVariables(), sutFile);
            case "custom":


                File file = new File(sutFile);
                String javaCode = IOUtils.toString(new FileInputStream(file));
                CompilerUtils.addClassPath(file.getParent());

                String className = file.getName().replace(".java", "");

                Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava(className, javaCode);
                return (SUT) aClass.newInstance();
            default:
                throw new IOException("SUT type specified not valid");
        }
    }

    public static void main(String[] args) {

        DefaultParser commandLineParser =  new DefaultParser();

        Options options = new Options();
        options.addOption("h", "help", false, "Print the help message and exit");

        options.addOption("r", "reqFile", true, "Input Requirement File");
        options.addOption("s", "sut", true, "File representing the sut");
        options.addOption(null, "sutType", true, "Type of the SUT: custom [default], kiss or nusmv");
        options.addOption(null, "kMin", true, "Minimum length of a test");
        options.addOption(null, "kMax", true, "Maximum length of a test");

        try {
            CommandLine commandLine = commandLineParser.parse(options, args, false);

            if (commandLine.hasOption("h")) {
                new HelpFormatter().printHelp("ReqT-CLI", options);
                System.exit(0);
            }


            if(!commandLine.hasOption("r")) {
                throw new IOException("Requirement file not specified!");
            }
            String reqFile = commandLine.getOptionValue("r");

            if(!commandLine.hasOption("s")) {
                throw new IOException("SUT file not specified!");
            }
            String sutFile = commandLine.getOptionValue("s");

            int kmin = 5, kmax = 20;
            if(commandLine.hasOption("kMin")){
                kmin = Integer.parseInt(commandLine.getOptionValue("kMin"));
            }
            if(commandLine.hasOption("kMax")){
                kmax = Integer.parseInt(commandLine.getOptionValue("kMax"));
            }

            AbstractLTLFrontEnd fe;
            if(reqFile.endsWith(".req")) {
                fe = new PSPFrontEnd();
            } else {
                fe = new LTLFrontEnd();
            }
            System.out.println("Parsing specification...");
            spec = fe.parseFile(reqFile);
            System.out.println("Parsing specification... [DONE]");

            System.out.println("Building NBA...");
            LTL2BA ltl2ba = new LTL2BA();
            ltl2ba.setType(LTL2BA.AutomatonType.NBA);
            ltl2ba.setOptimizationLevel(LTL2BA.OptimizationLevel.LOW);
            BuchiAutomaton automaton = ltl2ba.translate(spec);
            System.out.println("Building NBA... [DONE]");
            System.out.println("Expanding Edges...");
            automaton.expandEdges();
            System.out.println("Expanding Edges... [DONE]");
            System.out.println("# Vertex: " + automaton.vertexSet().size());
            System.out.println("# Edges: " + automaton.edgeSet().size());

            System.out.println("Setting Environment...");
            GDFSTestGenerator testGenerator = new GDFSTestGenerator(automaton, spec.getInputVariables());
            testGenerator.setMinLength(kmin);

            String sutType = "custom";
            if(commandLine.hasOption("sutType")) {
                sutType = commandLine.getOptionValue("sutType").toLowerCase();
            }

            SUT sut = getSUT(sutType, sutFile);

            TestingEnvironment environment = new TestingEnvironment(testGenerator, sut);
            environment.setMaxTraceLength(kmax);

            if (spec instanceof LTLDcSpec) {
                environment.enableLTLDcTranslation((LTLDcSpec) spec);
            }
            System.out.println("Setting Environment... [DONE]");

            System.out.println("Testing...");
            HashMap<Trace, TestOracle.Value> result = environment.runTests();
            System.out.println("Testing... [DONE]");
            System.out.println("Printing result (" + result.size() + " tests):");
            for(Map.Entry<Trace, TestOracle.Value> entry: result.entrySet()) {
                System.out.println(entry.getValue() + " - " + entry.getKey());
            }


        } catch (ParseException | IOException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            System.exit(-1);
        }
    }
}
