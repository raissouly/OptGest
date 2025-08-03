package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DoctorController implements Initializable {

    @FXML private TableView<Doctor> tableDoctor;
    @FXML private TableColumn<Doctor, Integer> colaId;
    @FXML private TableColumn<Doctor, String> colName;
    @FXML private TableColumn<Doctor, String> colPrenom;
    @FXML private TableColumn<Doctor, String> colSpecialite;
    @FXML private TableColumn<Doctor, String> colPhone;
    @FXML private TableColumn<Doctor, String> colemail;
    @FXML private TableColumn<Doctor, Void> colActions;
    @FXML private TextField searchField;  // تأكد أن fx:id في FXML هي نفسها

    private final ObservableList<Doctor> doctorList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connectiondb.Connection(); // الاتصال بقاعدة البيانات
        configureTable();
        loadDoctors();
    }

    private void configureTable() {
        colaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colSpecialite.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colemail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colSpecialite.setCellFactory(column -> new DefaultCellFactory());
        colPhone.setCellFactory(column -> new DefaultCellFactory());

        setupActionsColumn();
    }

    private void loadDoctors() {
        doctorList.clear();
        String query = "SELECT * FROM doctor ORDER BY id";

        try (ResultSet rs = Connectiondb.Dispaly(query)) {
            while (rs.next()) {
                Doctor doctor = new Doctor(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("specialite"),
                        rs.getString("telephone"),
                        rs.getString("email")
                );
                doctorList.add(doctor);
            }
            tableDoctor.setItems(doctorList);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load doctors.", Alert.AlertType.ERROR);
        }
    }

    private static class DefaultCellFactory extends TableCell<Doctor, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.trim().isEmpty()) {
                setText("");
                setStyle("-fx-text-fill: #999;");
            } else {
                setText(item);
                setStyle("-fx-text-fill: black;");
            }
        }
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<Doctor, Void>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");

            {
                btnEdit.setOnAction(e -> editDoctor(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> deleteDoctor(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox actions = new HBox(10, btnEdit, btnDelete);
                    actions.setStyle("-fx-alignment: center;");
                    setGraphic(actions);
                }
            }
        });
    }

    private void editDoctor(Doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddDoctor.fxml"));
            Parent root = loader.load();

            AddDoctorController controller = loader.getController();
            controller.setDoctorData(doctor);

            Stage stage = new Stage();
            stage.setTitle("Modifier Doctor");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            int selectedId = doctor.getId();
            loadDoctors();

            // إعادة اختيار الدكتور المعدل
            for (Doctor doc : doctorList) {
                if (doc.getId() == selectedId) {
                    tableDoctor.getSelectionModel().select(doc);
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d’ouvrir la fenêtre de modification.", Alert.AlertType.ERROR);
        }
    }

    private void deleteDoctor(Doctor doctor) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Confirmation");
        confirm.setHeaderText("Are you sure you want to delete this doctor?");
        confirm.setContentText("Doctor: " + doctor.getNom() + " " + doctor.getPrenom());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM doctor WHERE id = " + doctor.getId();
                try {
                    Connectiondb.ExecuteStatement(sql);
                    loadDoctors();
                    showAlert("Deleted", "Doctor deleted successfully.", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete doctor.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    void btnOnActionAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddDoctor.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Doctor");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d’ouvrir la fenêtre d’ajout du Doctor", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void btnOnActionSearch(ActionEvent event) {
        if (searchField == null) {
            showAlert("Erreur", "Le champ de recherche est vide ou non reconnu.", Alert.AlertType.ERROR);
            return;
        }

        String keyword = searchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            tableDoctor.setItems(doctorList); // عرض الكل
            return;
        }

        keyword = keyword.toLowerCase().trim();

        ObservableList<Doctor> filtered = FXCollections.observableArrayList();

        for (Doctor doc : doctorList) {
            if ((doc.getNom() != null && doc.getNom().toLowerCase().contains(keyword)) ||
                    (doc.getPrenom() != null && doc.getPrenom().toLowerCase().contains(keyword)) ||
                    (doc.getSpecialite() != null && doc.getSpecialite().toLowerCase().contains(keyword)) ||
                    (doc.getTelephone() != null && doc.getTelephone().contains(keyword)) ||
                    (doc.getEmail() != null && doc.getEmail().toLowerCase().contains(keyword))) {
                filtered.add(doc);
            }
        }

        tableDoctor.setItems(filtered);
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshTable() {
        loadDoctors();
    }
}
