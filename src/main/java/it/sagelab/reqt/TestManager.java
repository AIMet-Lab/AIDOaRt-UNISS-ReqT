package it.sagelab.reqt;

import it.sagelab.specpro.fe.AbstractLTLFrontEnd;
import it.sagelab.specpro.fe.LTLFrontEnd;
import it.sagelab.specpro.fe.PSPFrontEnd;
import it.sagelab.specpro.models.ba.BuchiAutomaton;
import it.sagelab.specpro.models.ltl.LTLSpec;
import it.sagelab.specpro.models.ltl.assign.Trace;
import it.sagelab.specpro.reasoners.LTL2BA;
import it.sagelab.specpro.testing.SUT;
import it.sagelab.specpro.testing.TestingEnvironment;
import it.sagelab.specpro.testing.generators.GDFSTestGenerator;
import it.sagelab.specpro.testing.oracles.TestOracle;
import javafx.concurrent.Task;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestManager {

    private LTLSpec spec;

    private SUT sut;

    private int kMin;

    private int kMax;

    public void readRequirementSpecification(String filePath) throws IOException {
        AbstractLTLFrontEnd fe;
        if(filePath.endsWith(".req")) {
            fe = new PSPFrontEnd();
        } else {
            fe = new LTLFrontEnd();
        }
        spec = fe.parseFile(filePath);
    }

    public LTLSpec getSpec() {
        return spec;
    }

    public void setSut(SUT sut) {
        this.sut = sut;
    }

    public int getkMin() {
        return kMin;
    }

    public int getkMax() {
        return kMax;
    }

    public void setkMin(int kMin) {
        this.kMin = kMin;
    }

    public void setkMax(int kMax) {
        this.kMax = kMax;
    }

    public Task<Map<Trace, TestOracle.Value>> prepareTask() {
        Task<Map<Trace, TestOracle.Value>> task = new Task<Map<Trace, TestOracle.Value>>() {

            @Override
            protected Map<Trace, TestOracle.Value> call() throws IOException {

                LTL2BA ltl2ba = new LTL2BA();
                ltl2ba.setType(LTL2BA.AutomatonType.NBA);
                BuchiAutomaton automaton = ltl2ba.translate(spec);

                GDFSTestGenerator testGenerator = new GDFSTestGenerator(automaton, spec.getInputVariables());
                testGenerator.setMinLength(kMin);

                TestingEnvironment environment = new TestingEnvironment(testGenerator, sut);
                environment.setMaxTraceLength(kMax);

                HashMap<Trace, TestOracle.Value> result = environment.runTests(res -> updateMessage("Evaluated Tests: " + res.size()));

                return result;
            }

        };

        return task;
    }

}
