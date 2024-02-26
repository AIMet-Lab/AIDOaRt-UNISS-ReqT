
public class ProdMachine {

    enum State {
        IDLE,
        WORKING,
        OVERHEAT
    }

    State currentState;
    int charge;
    int producedPieces;
    int toProduce;
    int prodProgress;
    boolean disableQueue;
    boolean newProduction;

    public ProdMachine() {
        currentState = State.IDLE;
        charge = 0;
        producedPieces = 0;
        toProduce = 0;
        prodProgress = 0;
        disableQueue = false;
    }

    public boolean addPiece() {
        if(!disableQueue) {
            ++toProduce;
            return true;
        } else {
            return false;
        }
    }

    public void prodMiss() {
        if(prodProgress > 0) {
            --prodProgress;
        }
    }

    public void step() {

        newProduction = false;

        if(toProduce >= 2) {
            disableQueue = true;
        }

        if(toProduce == 0) {
            disableQueue = false;
        }

        if(toProduce > 0) {
            currentState = State.WORKING;
        }

        if(toProduce == 0 || charge > 3) {
            currentState = State.IDLE;
        }

        if(currentState == State.IDLE) {
            charge = Math.max(0, charge - 2);
        }

        if(currentState == State.WORKING) {
            ++charge;
            ++prodProgress;
            if(prodProgress >= 2) {
                prodProgress = 0;
                --toProduce;
                ++producedPieces;
                newProduction = true;
            }
        }

        if(charge > 5) {
            currentState = State.OVERHEAT;
        }
    }

}