package it.sagelab.reqt;

import it.sagelab.specpro.models.ltl.LTLSpec;
import it.sagelab.specpro.models.ltl.assign.Trace;
import it.sagelab.specpro.testing.oracles.TestOracle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class TestReportController implements Initializable {

    @FXML
    TableView testTableView;

    @FXML
    Button closeButton;

    private LTLSpec spec;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TableColumn<TestCase, String> column1 = new TableColumn<>("N");
        column1.setCellValueFactory(new PropertyValueFactory<>("index"));


        TableColumn<TestCase, ImageView> column2 = new TableColumn<>("Result");
        column2.setCellValueFactory(new PropertyValueFactory<>("icon"));

        TableColumn<TestCase, String> column3 = new TableColumn<>("Trace");
        column3.setCellValueFactory(new PropertyValueFactory<>("trace"));

        column1.prefWidthProperty().setValue(60);
        column2.prefWidthProperty().setValue(60);
        column3.prefWidthProperty().bind(testTableView.widthProperty().subtract(120));

        column1.setStyle("-fx-alignment: CENTER;");
        column2.setStyle("-fx-alignment: CENTER;");

        testTableView.getColumns().add(column1);
        testTableView.getColumns().add(column2);
        testTableView.getColumns().add(column3);

        testTableView.setRowFactory( tv -> {
            TableRow<TestCase> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    TestCase rowData = row.getItem();

                    showDetailsWindow(rowData);

                }
            });
            return row ;
        });


    }

    public void showTestResult(Map<Trace, TestOracle.Value> values) {

        int index = 0;
        for(Map.Entry<Trace, TestOracle.Value> entry: values.entrySet()) {
            TestCase t = new TestCase();
            t.setIndex(++index);
            t.setSuccess(entry.getValue() == TestOracle.Value.TRUE);
            t.setTrace(entry.getKey());

            testTableView.getItems().add(t);
        }

    }

    public void showDetailsWindow(TestCase testCase) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("test-details.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();

        stage.setTitle("Test Details");
        Scene scene = new Scene(root, 600, 450);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        TestDetailsController controller = loader.getController();
        controller.setSpec(spec);
        controller.viewTest(testCase);
    }

    @FXML
    private void closeButtonAction(){
        // get a handle to the stage
        Stage stage = (Stage) closeButton.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    public void setSpec(LTLSpec spec) {
        this.spec = spec;
    }

}
