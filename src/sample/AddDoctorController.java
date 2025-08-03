package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddDoctorController implements Initializable {

    @FXML
    private Label txtid;

    @FXML
    private TextField Nom;

    @FXML
    private TextField prenom;

    @FXML
    private TextField Specialite;

    @FXML
    private TextField Telephone;

    @FXML
    private TextField Email;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connectiondb.Connection();
        Id_auto();
    }

    @FXML
    void btnOnActionAdd(ActionEvent event) {
        String nom = Nom.getText();
        String prenomText = prenom.getText();
        String specialite = Specialite.getText();
        String telephone = Telephone.getText();
        String email = Email.getText();

        try {
            if (selectedDoctor == null) {
                // 👉 إدخال جديد
                String sql = "INSERT INTO doctor (nom, prenom, specialite, telephone, email) VALUES ('" +
                        nom + "', '" + prenomText + "', '" + specialite + "', '" + telephone + "', '" + email + "')";
                Connectiondb.ExecuteStatement(sql);
                showAlert("Succès", "Médecin ajouté avec succès.", Alert.AlertType.INFORMATION);
            } else {
                // ✏️ تعديل موجود
                String sql = "UPDATE doctor SET nom='" + nom + "', prenom='" + prenomText + "', " +
                        "specialite='" + specialite + "', telephone='" + telephone + "', email='" + email + "' " +
                        "WHERE id=" + selectedDoctor.getId();
                Connectiondb.ExecuteStatement(sql);
                showAlert("Succès", "Médecin modifié avec succès.", Alert.AlertType.INFORMATION);
            }

            ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec lors de l'enregistrement.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clear_input() {
        Nom.clear();
        prenom.clear();
        Specialite.clear();
        Telephone.clear();
        Email.clear();
    }

    private boolean IsEmpty() {
        return Nom.getText().isEmpty() || prenom.getText().isEmpty() || Specialite.getText().isEmpty()
                || Telephone.getText().isEmpty() || Email.getText().isEmpty();
    }

    private void Message_Erorr() {
        Alert msg = new Alert(Alert.AlertType.ERROR);
        msg.setTitle("Erreur");
        msg.setHeaderText(null);
        msg.setContentText("Veuillez remplir tous les champs !");
        msg.show();
    }

    private Doctor selectedDoctor;

    public void setDoctorData(Doctor doctor) {
        this.selectedDoctor = doctor;

        txtid.setText(String.valueOf(doctor.getId())); // إذا كنتي باغية تبيني ID
        Nom.setText(doctor.getNom());
        prenom.setText(doctor.getPrenom());
        Specialite.setText(doctor.getSpecialite());
        Telephone.setText(doctor.getTelephone());
        Email.setText(doctor.getEmail());
    }

    // ✅ الطريقة المصححة - تجيب أكبر ID من قاعدة البيانات
    private int getMaxID() {
        int maxId = 0;
        try {
            // استعمال MAX() للحصول على أكبر ID
            ResultSet result = Connectiondb.Dispaly("SELECT MAX(id) as max_id FROM doctor");
            if (result.next()) {
                maxId = result.getInt("max_id");
            }
        } catch (SQLException ex) {
            System.out.println("Erreur dans getMaxID: " + ex);
        }
        return maxId;
    }

    // ✅ أو طريقة أخرى: ترتيب النتائج وأخذ أول واحد
    private int getLastID() {
        int lastId = 0;
        try {
            // ترتيب حسب ID تنازليا وأخذ أول نتيجة
            ResultSet result = Connectiondb.Dispaly("SELECT id FROM doctor ORDER BY id DESC LIMIT 1");
            if (result.next()) {
                lastId = result.getInt("id");
            }
        } catch (SQLException ex) {
            System.out.println("Erreur dans getLastID: " + ex);
        }
        return lastId;
    }

    private void Id_auto() {
        // استعمال إحدى الطريقتين
        int newId = getMaxID() + 1;
        // أو: int newId = getLastID() + 1;

        txtid.setText(String.valueOf(newId));
    }

    @FXML
    void btnOnActionCancel(ActionEvent event) {
        clear_input();
    }
}