package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class HistoriqueController implements Initializable {

    @FXML private TableView<CommandeHistorique> tableHistorique;
    @FXML private TableColumn<CommandeHistorique, String> colDate;
    @FXML private TableColumn<CommandeHistorique, String> colProduit;
    @FXML private TableColumn<CommandeHistorique, Integer> colQuantite;
    @FXML private TableColumn<CommandeHistorique, Double> colTotal;
    @FXML private TableColumn<CommandeHistorique, String> colStatut;

    private int clientId;
    private final ObservableList<CommandeHistorique> list = FXCollections.observableArrayList();

    public void setClientId(int id) {
        this.clientId = id;
        loadHistorique(); // تحميل البيانات مباشرة بعد استقبال ID
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colProduit.setCellValueFactory(new PropertyValueFactory<>("produit"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void loadHistorique() {
        list.clear();
        try {
            String sql = "SELECT c.Date_Commande, p.designation AS produit, c.quantite, c.prix_unitaire, c.etat " +
                    "FROM commandes c " +
                    "JOIN produits p ON c.ID_produits = p.reference " +
                    "WHERE c.ID_Client = " + clientId;

            ResultSet rs = Connectiondb.Dispaly(sql);
            while (rs.next()) {
                list.add(new CommandeHistorique(
                        rs.getString("Date_Commande"),
                        rs.getString("produit"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"),
                        rs.getString("etat")
                ));
            }
            tableHistorique.setItems(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
