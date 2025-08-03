package sample;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public abstract class Change {
//    public static String info="";

    public static Stage loadFxmlToStage(String fxml) {
        Stage stage = new Stage();
        try {

            Parent root = FXMLLoader.load(Change.class.getResource(fxml));
            Scene sc = new Scene(root);
            stage.setScene(sc);
            return stage;
        } catch (IOException e) {
            throw new IllegalStateException("cannot load FXML screen", e);
        }
    }

    public static void changeScene(Stage stg, String newFXML) throws IOException {
        Parent root = FXMLLoader.load(Change.class.getResource(newFXML));
        stg.setMinHeight(700);
        stg.setMinWidth(1250);
        stg.centerOnScreen();
        stg.resizableProperty().setValue(false);
        stg.getScene().setRoot(root);
    }
//    public static boolean isNumber(String number){
//        //0-9 [\d]+ at least 1 digit
//        // [\d]* at least 0 digit
//        Pattern p = Pattern.compile("[\\d]+");
//        Matcher m= p.matcher(number);
//        return m.matches();
//    }
}
