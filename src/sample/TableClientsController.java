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
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.layout.HBox;


import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class TableClientsController implements Initializable {

    @FXML private TableView<Clients> tableClients;
    @FXML private TableColumn<Clients, Integer> colaId;
    @FXML private TableColumn<Clients, String> colName;
    @FXML private TableColumn<Clients, String> colPrenom;
    @FXML private TableColumn<Clients, String> colPhone;
    @FXML private TableColumn<Clients, String> colemail;
    @FXML private TableColumn<Clients, Void> colAction;
    @FXML private TextField SEARCH;

    private final ObservableList<Clients> list = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connectiondb.Connection();
        configureTable();
        loadData();
        addButtonToTable();
    }

    private void configureTable() {
        colaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colemail.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void loadData() {
        list.clear();
        try {
            ResultSet result = Connectiondb.Dispaly("SELECT * FROM clients");
            while (result.next()) {
                list.add(new Clients(
                        result.getInt("id"),
                        result.getString("nom"),
                        result.getString("prenom"),
                        result.getString("telephone"),
                        result.getString("email")
                ));
            }
            tableClients.setItems(list);
        } catch (SQLException ex) {
            System.out.println("Erreur chargement clients : " + ex.getMessage());
        }
    }

    // حل متقدم أكثر مع أيقونة قلم مخصصة:
    private void addButtonToTable() {
        Callback<TableColumn<Clients, Void>, TableCell<Clients, Void>> cellFactory =
                new Callback<TableColumn<Clients, Void>, TableCell<Clients, Void>>() {
                    @Override
                    public TableCell<Clients, Void> call(final TableColumn<Clients, Void> param) {
                        return new TableCell<Clients, Void>() {

                            private final Button btnEdit = new Button("✏");
                            private final Button btnHistorique = new Button("📜");

                            {
                                // زر التعديل
                                btnEdit.setStyle("-fx-font-size: 16px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 15px; -fx-border-radius: 15px; -fx-padding: 5px 8px;");
                                btnEdit.setTooltip(new Tooltip("Modifier le client"));
                                btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-font-size: 16px; -fx-background-color: #45a049; -fx-text-fill: white; -fx-background-radius: 15px;"));
                                btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-font-size: 16px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 15px;"));
                                btnEdit.setOnAction((ActionEvent event) -> {
                                    Clients client = getTableView().getItems().get(getIndex());
                                    openEditClientWindow(client);
                                });

                                // زر التاريخ
                                btnHistorique.setStyle("-fx-font-size: 16px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 15px; -fx-border-radius: 15px; -fx-padding: 5px 8px;");
                                btnHistorique.setTooltip(new Tooltip("Historique des commandes"));
                                btnHistorique.setOnMouseEntered(e -> btnHistorique.setStyle("-fx-font-size: 16px; -fx-background-color: #1976D2; -fx-text-fill: white; -fx-background-radius: 15px;"));
                                btnHistorique.setOnMouseExited(e -> btnHistorique.setStyle("-fx-font-size: 16px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 15px;"));
                                btnHistorique.setOnAction((ActionEvent event) -> {
                                    Clients client = getTableView().getItems().get(getIndex());
                                    openHistoriqueWindow(client); // خاصك ديري هاد الميثود تحت
                                });
                            }

                            @Override
                            public void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    HBox hbox = new HBox(8, btnEdit, btnHistorique);
                                    setGraphic(hbox);
                                }
                            }
                        };
                    }
                };

        colAction.setCellFactory(cellFactory);
    }
    private void openHistoriqueWindow(Clients client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/HistoriqueClient.fxml"));
            Parent root = loader.load();

            HistoriqueController controller = loader.getController();
            controller.setClientId(client.getId());

            Stage stage = new Stage();
            stage.setTitle("Historique de " + client.getNom());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'historique du client", Alert.AlertType.ERROR);
        }
    }


    private void openEditClientWindow(Clients client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/Clients.fxml"));
            Parent root = loader.load();

            AddClientsController controller = loader.getController();
            controller.initData(client); // هذا سيقوم بتفعيل وضع التعديل

            Stage stage = new Stage();
            stage.setTitle("Modifier le client"); // العنوان الصحيح
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification", Alert.AlertType.ERROR);
        }
    }
    @FXML
    void btnOnActionSearch(ActionEvent event) {
        String keyword = SEARCH.getText().trim();
        if (!keyword.isEmpty()) {
            ObservableList<Clients> searchList = FXCollections.observableArrayList();
            try {
                String sql = "SELECT * FROM clients WHERE nom LIKE '%" + keyword + "%' OR telephone LIKE '%" + keyword + "%'";
                ResultSet result = Connectiondb.Dispaly(sql);
                while (result.next()) {
                    searchList.add(new Clients(
                            result.getInt("id"),
                            result.getString("nom"),
                            result.getString("prenom"),
                            result.getString("telephone"),
                            result.getString("email")
                    ));
                }
                tableClients.setItems(searchList);
            } catch (SQLException ex) {
                System.out.println("Erreur recherche : " + ex.getMessage());
            }
        } else {
            tableClients.setItems(list);
        }
    }

    // في openEditClientWindow method في TableClientsController:


    @FXML
    void btnOnActionAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/Clients.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Client");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d’ouvrir la fenêtre d’ajout du client", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
