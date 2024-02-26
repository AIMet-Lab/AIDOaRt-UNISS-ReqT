package it.sagelab.reqt;

import it.sagelab.specpro.models.ltl.assign.Trace;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TestCase {

    private int index;
    private Trace trace;
    private boolean success;
    private ImageView icon;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Trace getTrace() {
        return trace;
    }

    public void setTrace(Trace trace) {
        this.trace = trace;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;

        icon = new ImageView(success ?  new Image(getClass().getResourceAsStream("true.png")) : new Image(getClass().getResourceAsStream("false.png")));
        icon.setFitWidth(20);
        icon.setFitHeight(20);
    }

    public ImageView getIcon() {
        return icon;
    }
}
