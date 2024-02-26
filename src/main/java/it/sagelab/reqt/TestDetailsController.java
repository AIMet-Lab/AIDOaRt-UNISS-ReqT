package it.sagelab.reqt;

import it.sagelab.specpro.models.ltl.Atom;
import it.sagelab.specpro.models.ltl.LTLSpec;
import it.sagelab.specpro.models.ltl.assign.Assignment;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

public class TestDetailsController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<TestCase, Integer> column1 = new TableColumn<>("Step #");
        column1.setCellValueFactory(new PropertyValueFactory<>("index"));

        TableColumn<TestCase, String> column2 = new TableColumn<>("Assignment");
        column2.setCellValueFactory(new PropertyValueFactory<>("assignment"));

        column1.prefWidthProperty().setValue(60);

        traceTable.getColumns().add(column1);
    }

    public class Item {
        private int index;
        private Assignment assignment;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Assignment getAssignment() {
            return assignment;
        }

        public void setAssignment(Assignment assignment) {
            this.assignment = assignment;
        }
    }

    @FXML
    Button closeButton;

    @FXML
    TableView traceTable;

    @FXML
    Label statusLabel;

    @FXML
    Label indexLabel;

    private LTLSpec spec;

    public void viewTest(TestCase testCase) {
        statusLabel.setText(testCase.isSuccess() ? "Success" : "Fail");
        statusLabel.setTextFill(testCase.isSuccess() ? Color.GREEN : Color.RED);
        indexLabel.setText(Integer.toString(testCase.getIndex()));

        int index = 0;
        for(Assignment a: testCase.getTrace()) {
            Item i = new Item();
            i.setIndex(++index);
            i.setAssignment(a);
            traceTable.getItems().add(i);
        }

    }

    public void setSpec(LTLSpec spec) {
        this.spec = spec;

        Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>> callBack =
                param -> {
                    String varName = (String) param.getTableColumn().getUserData();
                    Atom a = param.getValue().getAssignment().getAtom(varName);
                    if(a == null) {
                        return new SimpleStringProperty("");
                    }
                    else {
                        return new SimpleStringProperty(param.getValue().getAssignment().getValue(a) ? "T" : "F");
                    }
                };

        ObservableList<TableColumn<Item, String>> assCols = FXCollections.observableArrayList();
        for (Atom input: spec.getInputVariables()){
            TableColumn<Item, String> tmpCol = new TableColumn(input.getLabel());
            tmpCol.setUserData(input.getLabel());
            tmpCol.setCellValueFactory(callBack);
            tmpCol.getStyleClass().add("input-var");
            assCols.add(tmpCol);
        }

        for (Atom outputs: spec.getOutputVariables()){
            TableColumn<Item, String> tmpCol = new TableColumn(outputs.getLabel());
            tmpCol.setUserData(outputs.getLabel());
            tmpCol.setCellValueFactory(callBack);
            tmpCol.getStyleClass().add("output-var");
            assCols.add(tmpCol);
        }

        traceTable.getColumns().addAll(assCols);
    }

    @FXML
    private void closeButtonAction(){
        // get a handle to the stage
        Stage stage = (Stage) closeButton.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

}
