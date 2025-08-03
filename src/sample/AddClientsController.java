package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddClientsController implements Initializable {
    @FXML
    private Label txtid;
    @FXML
    private TextField Nom;
    @FXML
    private TextField prenom;
    @FXML
    private TextField Telephone;
    @FXML
    private TextField Email;

    // إضافة مراجع للأزرار والعناوين لتغيير النصوص
    @FXML
    private Button btnAdd;  // زر الإضافة/التعديل
    @FXML
    private Label titleLabel;  // عنوان النافذة (إذا كان موجود في FXML)

    private static int id = 0;
    private int clientId = -1; // -1 means new client, positive value means edit mode
    private boolean isEditMode = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connectiondb.Connection();
        Id_auto();
        setupAddMode(); // الإعداد الافتراضي للإضافة
    }

    @FXML
    void btnOnActionAdd(ActionEvent event) {
        if (IsEmpty()) {
            Message_Error();
        } else {
            boolean success = false;

            if (isEditMode) {
                // Update existing client
                success = updateClient();
            } else {
                // Add new client
                success = addNewClient();
            }

            if (success) {
                // Close current window
                Stage currentStage = (Stage) Nom.getScene().getWindow();
                currentStage.close();

                // Try to open table window
                openTableWindow();
            }
        }
    }

    private boolean addNewClient() {
        try {
            String query = "INSERT INTO `clients`(`id`, `nom`, `prenom`, `telephone`, `email`) VALUES ('" + txtid.getText() + "',"
                    + "'" + Nom.getText() + "','" + prenom.getText() + "','" + Telephone.getText() + "','" + Email.getText() + "')";
            Connectiondb.Insert(query);
            clear_input();
            showSuccessMessage("Client ajouté avec succès!");
            return true;
        } catch (Exception e) {
            showErrorMessage("Erreur lors de l'ajout du client: " + e.getMessage());
            return false;
        }
    }

    private boolean updateClient() {
        try {
            String query = "UPDATE `clients` SET `nom`='" + Nom.getText() + "', `prenom`='" + prenom.getText() +
                    "', `telephone`='" + Telephone.getText() + "', `email`='" + Email.getText() +
                    "' WHERE `id`=" + clientId;
            Connectiondb.Insert(query);
            showSuccessMessage("Client modifié avec succès!");
            return true;
        } catch (Exception e) {
            showErrorMessage("Erreur lors de la modification du client: " + e.getMessage());
            return false;
        }
    }

    // إعداد واجهة المستخدم لوضع الإضافة
    private void setupAddMode() {
        isEditMode = false;
        if (btnAdd != null) {
            btnAdd.setText("Ajouter");
        }
        if (titleLabel != null) {
            titleLabel.setText("Ajouter un Client");
        }
        // تعيين عنوان النافذة
        Stage stage = getStage();
        if (stage != null) {
            stage.setTitle("Ajouter un Client");
        }
    }

    // إعداد واجهة المستخدم لوضع التعديل
    private void setupEditMode() {
        isEditMode = true;
        if (btnAdd != null) {
            btnAdd.setText("Modifier");
        }
        if (titleLabel != null) {
            titleLabel.setText("Modifier le Client");
        }
        // تعيين عنوان النافذة
        Stage stage = getStage();
        if (stage != null) {
            stage.setTitle("Modifier le Client");
        }
    }

    // للحصول على المرحلة الحالية
    private Stage getStage() {
        try {
            return (Stage) Nom.getScene().getWindow();
        } catch (Exception e) {
            return null;
        }
    }

    private void openTableWindow() {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL fxmlUrl = getClass().getResource("/sample/TableClients.fxml");

            if (fxmlUrl == null) {
                fxmlUrl = getClass().getResource("/TableClients.fxml");
            }

            if (fxmlUrl != null) {
                loader.setLocation(fxmlUrl);
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Liste des Clients");
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                showErrorMessage("Impossible d'ouvrir la fenêtre des clients.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Erreur lors de l'ouverture de la fenêtre: " + e.getMessage());
        }
    }

    private void clear_input() {
        Nom.clear();
        prenom.clear();
        Telephone.clear();
        Email.clear();
    }

    private boolean IsEmpty() {
        return Nom.getText().isEmpty() || prenom.getText().isEmpty()
                || Telephone.getText().isEmpty() || Email.getText().isEmpty();
    }

    private void Message_Error() {
        Alert msg = new Alert(Alert.AlertType.ERROR);
        msg.setTitle("Erreur");
        msg.setContentText("Les informations sont vides");
        msg.show();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.show();
    }

    private int getID_serveur() {
        int id = 0;
        try {
            ResultSet result = Connectiondb.Dispaly("SELECT `id` FROM `clients` ORDER BY `id` DESC LIMIT 1;");
            if (result.next()) {
                id = result.getInt("id");
            }
        } catch (SQLException ex) {
            System.out.println("Error in getserveur: " + ex);
        }
        return id;
    }

    private void Id_auto() {
        if (!isEditMode) {
            int id = getID_serveur();
            txtid.setText(String.valueOf(id + 1));
        }
    }

    @FXML
    void btnOnActionCancel(ActionEvent event) {
        clear_input();
        Stage currentStage = (Stage) Nom.getScene().getWindow();
        currentStage.close();
    }

    // هذه الدالة تستدعى من TableClientsController عندما نريد تعديل عميل
    public void initData(Clients client) {
        this.clientId = client.getId();

        txtid.setText(String.valueOf(client.getId()));
        Nom.setText(client.getNom());
        prenom.setText(client.getPrenom());
        Telephone.setText(client.getTelephone());
        Email.setText(client.getEmail());

        // تفعيل وضع التعديل وتغيير النصوص
        setupEditMode();
    }
}