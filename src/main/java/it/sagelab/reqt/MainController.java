package it.sagelab.reqt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.sagelab.specpro.fe.kiss.MealyMachineBuilder;
import it.sagelab.specpro.models.ltl.assign.Trace;
import it.sagelab.specpro.testing.MealyMachineSUT;
import it.sagelab.specpro.testing.SUT;
import it.sagelab.specpro.testing.oracles.TestOracle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.openhft.compiler.CompilerUtils;
import org.apache.commons.io.IOUtils;

public class MainController implements Initializable {

    @FXML
    private Button reqFileButton;

    @FXML
    private TextField reqFileField;

    @FXML
    private ChoiceBox modelSelectionBox;

    @FXML
    private Button modelFileButton;

    @FXML
    private TextField modelFileField;

    @FXML
    private TextField kMinField;

    @FXML
    private TextField kMaxField;

    @FXML
    private Button runButton;

    @FXML
    private ProgressBar bar;

    @FXML
    private Label barLabel;

    private String [] modelChoices = {"Kiss", "NuSMV", "Custom"};

    private FileChooser reqFileChooser, modelFileChooser;

    private TestManager testManager;


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        reqFileChooser = new FileChooser();
        modelFileChooser = new FileChooser();
        FileChooser.ExtensionFilter reqFilter = new FileChooser.ExtensionFilter("LTL or PSP specification (*.ltl, *.req)", "*.ltl", "*.req");
        reqFileChooser.getExtensionFilters().add(reqFilter);

        reqFileButton.setOnAction(event -> this.updateTextField(event,  reqFileField, reqFileChooser));
        modelFileButton.setOnAction(event -> this.updateTextField(event, modelFileField, modelFileChooser));

        modelSelectionBox.setItems(FXCollections.observableArrayList(modelChoices));
        modelSelectionBox.getSelectionModel().select(0);
        modelSelectionChanged();
        modelSelectionBox.getSelectionModel().selectedItemProperty().addListener(e -> modelSelectionChanged());

        // force the field to be numeric only
        kMinField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    kMinField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        // force the field to be numeric only
        kMaxField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    kMaxField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        runButton.setOnAction(e -> runTesting());

        bar.setVisible(false);
        barLabel.setVisible(false);
    }

    private void updateTextField(Event event, TextField field, FileChooser fileChooser) {
        File selectedFile = fileChooser.showOpenDialog(((Node)event.getTarget()).getScene().getWindow());
        if (selectedFile != null) {
            field.setText(selectedFile.getAbsolutePath());
        }
    }

    private void modelSelectionChanged() {
        String model = modelSelectionBox.getSelectionModel().getSelectedItem().toString();

        modelFileChooser.getExtensionFilters().clear();
        switch (model) {
            case "Kiss":
                modelFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Kiss model (*.kiss)", "*.kiss"));
                break;
            case "NuSMV":
                modelFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("NuMSV model (*.smv)", "*.smv"));
                break;
            case "Custom":
                modelFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java SUT class (*.java)", "*.java"));
                break;
        }
    }

    private void runTesting() {
        testManager = new TestManager();

        try {
            testManager.readRequirementSpecification(reqFileField.getText());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Requirement File Error");
            alert.setHeaderText("An error occurred while loading the requirement file!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }


        try {
            testManager.setSut(getSUT());
        } catch (IOException | ClassNotFoundException | RuntimeException | InstantiationException | IllegalAccessException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("SUT File Error");
            alert.setHeaderText("An error occurred while loading the System Under Test model!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
            return;
        }

        testManager.setkMin(Integer.parseInt(kMinField.getText()));
        testManager.setkMax(Integer.parseInt(kMaxField.getText()));


        Task<Map<Trace, TestOracle.Value>> task = testManager.prepareTask();

        task.runningProperty().addListener((obs, wasRunning, isRunning) -> {
            if (isRunning) {
                bar.setVisible(true);
                barLabel.setVisible(true);
                runButton.disableProperty().setValue(true);
            } else {
               bar.setVisible(false);
               barLabel.setVisible(false);
               runButton.disableProperty().setValue(false);
            }
        });

        task.setOnSucceeded(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("test-report.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("Test Report");
                stage.setScene(new Scene(root, 600, 450));
                stage.show();

                TestReportController controller = loader.getController();
                controller.showTestResult(task.getValue());
                controller.setSpec(testManager.getSpec());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        barLabel.textProperty().bind(task.messageProperty());

        task.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Testing Execution Error");
            alert.setHeaderText("An error occurred while running tests!");
            alert.setContentText(event.toString());
            alert.showAndWait();
        });

        ExecutorService executorService =  Executors.newFixedThreadPool(2);;
        executorService.submit(task);



    }

    private SUT getSUT() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String model = modelSelectionBox.getSelectionModel().getSelectedItem().toString();
        String modelFile = modelFileField.getText();

        switch (model) {
            case "Kiss":
                return new MealyMachineSUT(MealyMachineBuilder.parseKISSFile(modelFile));
            case "NuSMV":
                return new NuSMVModelSUT(testManager.getSpec().getInputVariables(), testManager.getSpec().getOutputVariables(), modelFile);
            case "Custom":


                File file = new File(modelFile);
                String javaCode = IOUtils.toString(new FileInputStream(file));
                CompilerUtils.addClassPath(file.getParent());

                Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava("CustomSUT", javaCode);
                return (SUT) aClass.newInstance();
        }

        return null;
    }

}