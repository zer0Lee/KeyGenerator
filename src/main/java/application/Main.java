package application;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;


public class Main extends Application {
  @Override
  public void start(Stage primaryStage) {
    try {      
      BorderPane root = new BorderPane();
      initializeApplicationPane(root);
      Scene scene = new Scene(root,400,400);

      scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
      primaryStage.setTitle("证书生成器");
      primaryStage.setScene(scene);
      primaryStage.show();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) {
    launch(args);
  }
  
  private void initializeApplicationPane(BorderPane pane) {
    AnchorPane center = new AnchorPane();
    pane.setCenter(center);
    
    // Add MAC and Type input
    GridPane gridPane = new GridPane();
    TextField macAddress = new TextField();
    ComboBox<String> licenseType = new ComboBox<>();
    ObservableList<String> selections = FXCollections.observableArrayList();
    selections.addAll("Trail", "Basic");
    licenseType.setItems(selections);
    licenseType.setValue("Trail");

    // Add expire date selection
    ComboBox<Integer> expireMonth = new ComboBox<>();
    expireMonth.getItems().addAll(1, 3, 6, 12, 24, 36);
    expireMonth.setValue(3);
    licenseType.valueProperty().addListener((ov, oldvalue, newvalue) -> {
      if (newvalue.equals("Basic")) {
        expireMonth.setDisable(true);
      } else {
        expireMonth.setDisable(false);
      }
    });
    
    // Add save location
    TextField fileLocation = new TextField();
    Button fileChooser = new Button("Explore");
    fileChooser.setOnAction(event -> {
      FileChooser chooser = new FileChooser();
      chooser.setTitle("Select the save path");
      chooser.getExtensionFilters().add(new ExtensionFilter("License File", "*.lic"));
      File selectedFile = chooser.showSaveDialog(pane.getScene().getWindow());
      
      if (selectedFile != null) {
        fileLocation.setText(selectedFile.getAbsolutePath());
      }
    });
    
    gridPane.add(new Label("MAC: "), 0, 0);
    gridPane.add(macAddress, 1, 0);
    gridPane.add(new Label("License Type: "), 0, 1);
    gridPane.add(licenseType, 1, 1);
    gridPane.add(new Label("Expire: "), 0, 2);
    gridPane.add(expireMonth, 1, 2);
    gridPane.add(new Label("Save Path: "), 0, 3);
    gridPane.add(fileLocation, 1, 3);
    gridPane.add(fileChooser, 2, 3);
    gridPane.setVgap(5.0);
    gridPane.setHgap(5.0);
    AnchorPane.setLeftAnchor(gridPane, 10.0);
    AnchorPane.setRightAnchor(gridPane, 10.0);
    AnchorPane.setTopAnchor(gridPane, 10.0);
    
    // Add OK and Status bar
    Button btOk = new Button("开始生成证书");
    Label statusBar = new Label();
    
    VBox vBox = new VBox();
    vBox.getChildren().addAll(statusBar, btOk);
    vBox.setAlignment(Pos.BOTTOM_CENTER);
    vBox.setSpacing(5.0);
    AnchorPane.setLeftAnchor(vBox, 10.0);
    AnchorPane.setRightAnchor(vBox, 10.0);
    AnchorPane.setBottomAnchor(vBox, 10.0);
    
    // Add actions for ok and cancel button
    btOk.setOnAction(event -> {
      if (macAddress.getText().isEmpty()) {
        alert("MAC address is empty!");
      } else if (fileLocation.getText().isEmpty()) {
        alert("Please set the save path!");
      } else {
        try {
          KeyGenerator keyGenerator = new KeyGenerator();
          keyGenerator.generateLicenseFile(
              fileLocation.getText(), 
              macAddress.getText(), 
              licenseType.getSelectionModel().getSelectedIndex(),
              expireMonth.getValue());
          Alert success = new Alert(AlertType.INFORMATION);
          success.setTitle("证书文件生成成功");
          success.setHeaderText("成功生成证书文件！" + System.lineSeparator() + "文件路径：" + fileLocation.getText());
          success.setContentText(null);
          success.showAndWait();
        } catch (Exception e) {
          StringWriter out = new StringWriter();
          e.printStackTrace(new PrintWriter(out));
          alert("Exception catched while generating license file!" + "\n" + out.toString());
        }
      }
    });

    // Add to center
    center.getChildren().addAll(gridPane, vBox);
  }
  
  private void alert(String message) {
    Alert alertDialog = new Alert(AlertType.WARNING);
    alertDialog.setTitle("Error");
    alertDialog.setHeaderText(message);
    alertDialog.setContentText("Please check the input");
    alertDialog.showAndWait();
  }
}
