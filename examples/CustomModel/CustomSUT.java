import it.sagelab.specpro.models.ltl.Atom;
import it.sagelab.specpro.models.ltl.assign.Assignment;
import it.sagelab.specpro.testing.SUT;

import java.util.Set;

public class CustomSUT extends SUT {


    ProdMachine machine;

    public CustomSUT() {
        super(null, null);
    }

    @Override
    public void reset() {
        machine = new ProdMachine();
    }

    @Override
    public Assignment exec(Assignment input) {

        boolean pieceAdded = false;
        for(Atom i: input.getAssignments().keySet()) {
            switch (i.getName()) {
                case "add_piece":
                    if(input.getValue(i)) {
                        pieceAdded = machine.addPiece();
                    }
                    break;
                case "prod_miss":
                    if(input.getValue(i)) {
                        machine.prodMiss();
                    }
            }
        }

        machine.step();

        Assignment output = new Assignment();
        output.add(new Atom("state_idle"), machine.currentState == ProdMachine.State.IDLE);
        output.add(new Atom("state_working"), machine.currentState == ProdMachine.State.WORKING);
        output.add(new Atom("state_overheat"), machine.currentState == ProdMachine.State.OVERHEAT);
        output.add(new Atom("empty_queue"), machine.toProduce == 0);
        output.add(new Atom("disabled_queue"), machine.disableQueue);
        output.add(new Atom("new_production"), machine.newProduction);
        output.add(new Atom("piece_added"), pieceAdded);
        return output;
    }

}