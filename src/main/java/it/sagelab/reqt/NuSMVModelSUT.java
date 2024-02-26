package it.sagelab.reqt;

import it.sagelab.specpro.models.ltl.Atom;
import it.sagelab.specpro.models.ltl.assign.Assignment;
import it.sagelab.specpro.models.ltl.assign.Trace;
import it.sagelab.specpro.reasoners.parser.NuSMVOutputParser;
import it.sagelab.specpro.testing.InvalidInputException;
import it.sagelab.specpro.testing.SUT;
import org.apache.commons.io.IOUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class NuSMVModelSUT extends SUT {

    Trace trace;
    private final String modelFile;
    private int index = 0;

    public NuSMVModelSUT(Set<Atom> inputs, Set<Atom> outputs, String modelFile) {
        super(inputs, outputs);
        this.modelFile = modelFile;
    }


    @Override
    public void reset() {
        trace = new Trace();
        ++index;
    }

    @Override
    public Assignment exec(Assignment input) throws InvalidInputException {
        trace.add(input);
        String fileName = modelFile.replace(".smv", ".temp.smv");
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            FileReader fileReader = new FileReader(modelFile);
            IOUtils.copy(fileReader, fileWriter);

            fileWriter.write(buildFormula());

            fileReader.close();
            fileWriter.close();

            if(System.getenv("SPECPRO_NUSMV") == null) {
                throw new RuntimeException("Environment variable SPECPRO_NUSMV not defined");
            }

            ProcessBuilder builder = new ProcessBuilder(System.getenv("SPECPRO_NUSMV"), fileName);
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process process = builder.start();

            String inputStr = IOUtils.toString(process.getInputStream());

            process. waitFor();
            //System.out.println(inputStr);

            ArrayList<Trace> traces = NuSMVOutputParser.parseString(inputStr);

            if(traces.size() == 0 || traces.get(0).size() == 0) {
                throw new InvalidInputException(input.toString());
            }

            int size = trace.size();
            traces.get(0).setStartCycle(traces.get(0).getStartCycle() + 1);
            trace = traces.get(0).getPrefix(size);

            if(!trace.last().isCompatible(input)) {
                System.out.println(inputStr);
                System.out.println(input);
                System.out.println(traces.get(0).size() + " -- " + trace.size());
                System.out.println(trace);
            }

            return trace.last();


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }




        return null;
    }

    private String buildFormula() {
        StringBuilder builder = new StringBuilder();
        builder.append("LTLSPEC !(");

        for(int i = 0; i < trace.size(); ++i) {
            builder.append(buildAssignmentFormula(trace.get(i)));
            if(i < trace.size() - 1) {
                builder.append(" & X(");
            }
        }

        for(int i = 0; i < trace.size(); ++i) {
            builder.append(")");
        }
        return builder.toString();
    }

    private String buildAssignmentFormula(Assignment assignment) {
        StringBuilder builder = new StringBuilder();

        for(Map.Entry<Atom, Boolean> entry: assignment.getAssignments().entrySet()) {
            if(builder.length() > 0) {
                builder.append(" & ");
            }
            if(entry.getValue().equals(Boolean.FALSE)) {
                builder.append("!");
            }
            builder.append(entry.getKey().getName());
        }

        return builder.toString();
    }
}
