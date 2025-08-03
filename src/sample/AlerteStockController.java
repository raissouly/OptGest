package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Collections;

import java.sql.*;

public class AlerteStockController {

    @FXML
    private TableView<Produit> tableAlertes;
    @FXML
    private TableColumn<Produit, String> colReference;
    @FXML
    private TableColumn<Produit, String> colDesignation;
    @FXML
    private TableColumn<Produit, Integer> colQuantite;
    @FXML
    private TableColumn<Produit, Integer> colStockMin;
    @FXML
    private TableColumn<Produit, String> colStatus;
    @FXML
    private TableColumn<Produit, Integer> colDeficit;

    @FXML
    public void initialize() {
        // Configure column bindings to match your Produit class
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colDesignation.setCellValueFactory(new PropertyValueFactory<>("designation"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimal"));

        // Optional: Add status and deficit columns if available in FXML
        if (colStatus != null) {
            colStatus.setCellValueFactory(cellData -> {
                Produit p = cellData.getValue();
                String status = p.getQuantite() == 0 ? "OUT OF STOCK" : "LOW STOCK";
                return new javafx.beans.property.SimpleStringProperty(status);
            });
        }

        if (colDeficit != null) {
            colDeficit.setCellValueFactory(cellData -> {
                Produit p = cellData.getValue();
                int deficit = Math.max(0, p.getStockMinimal() - p.getQuantite());
                return new javafx.beans.property.SimpleIntegerProperty(deficit).asObject();
            });
        }

        // Configure column widths
        colReference.setPrefWidth(120);
        colDesignation.setPrefWidth(200);
        colQuantite.setPrefWidth(100);
        colStockMin.setPrefWidth(100);
        if (colStatus != null) colStatus.setPrefWidth(120);
        if (colDeficit != null) colDeficit.setPrefWidth(80);

        // Load low stock products
        chargerProduitsStockBas();
    }

    /**
     * Load products with stock below minimum threshold
     */
    private void chargerProduitsStockBas() {
        ObservableList<Produit> alertes = FXCollections.observableArrayList();

        // Updated query to match your actual database column names
        String query = "SELECT reference, designation, taille, quantite, prix_unitaire, " +
                "couleur, type, marque, stock_minimal " +
                "FROM produits WHERE quantite <= stock_minimal " +
                "ORDER BY (stock_minimal - quantite) DESC";

        try (Connection con = Connectiondb.Connection();
             PreparedStatement pst = con.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Produit produit = new Produit(
                        rs.getString("reference"),
                        rs.getString("designation"),
                        rs.getString("taille"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"),
                        rs.getString("couleur"),
                        rs.getString("type"),
                        rs.getString("marque"),
                        rs.getInt("stock_minimal")
                );
                alertes.add(produit);
            }

            // Display data in table
            tableAlertes.setItems(alertes);

            // Show message based on results
            if (alertes.isEmpty()) {
                afficherMessage("Information", "No products with low stock currently", AlertType.INFORMATION);
            } else {
                // Count out of stock vs low stock
                long outOfStock = alertes.stream().filter(p -> p.getQuantite() == 0).count();
                long lowStock = alertes.size() - outOfStock;

                String message = String.format("Found %d products requiring attention:\n" +
                                "• %d out of stock\n" +
                                "• %d low stock",
                        alertes.size(), outOfStock, lowStock);
                afficherMessage("Stock Alerts", message, AlertType.WARNING);
            }

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            afficherMessage("Error", "Failed to load stock data:\n" + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Refresh table data
     */
    @FXML
    public void actualiserDonnees() {
        chargerProduitsStockBas();
    }

    /**
     * Get count of products with low stock
     * @return number of products
     */
    public int getNombreProduitsStockBas() {
        return tableAlertes.getItems().size();
    }

    /**
     * Get count of products that are completely out of stock
     * @return number of out of stock products
     */
    public int getNombreProduitsRupture() {
        return (int) tableAlertes.getItems().stream()
                .filter(p -> p.getQuantite() == 0)
                .count();
    }

    /**
     * Get the most critical products (highest deficit)
     * @param limit maximum number of products to return
     * @return list of most critical products
     */
    public ObservableList<Produit> getProduitsCritiques(int limit) {
        return tableAlertes.getItems().stream()
                .sorted((p1, p2) -> Integer.compare(
                        (p2.getStockMinimal() - p2.getQuantite()),
                        (p1.getStockMinimal() - p1.getQuantite())
                ))
                .limit(limit)
                .collect(FXCollections::observableArrayList,
                        ObservableList::add,
                        ObservableList::addAll);
    }

    /**
     * Export alerts to CSV
     */
    @FXML
    public void exporterAlertes() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Stock Alerts");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("stock_alerts.csv");

        javafx.stage.Stage stage = (javafx.stage.Stage) tableAlertes.getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                // Write CSV header
                writer.append("Reference,Designation,Current Stock,Min Stock,Deficit,Status\n");

                // Write data
                for (Produit p : tableAlertes.getItems()) {
                    int deficit = Math.max(0, p.getStockMinimal() - p.getQuantite());
                    String status = p.getQuantite() == 0 ? "OUT OF STOCK" : "LOW STOCK";

                    writer.append(p.getReference()).append(",")
                            .append(escapeCsvField(p.getDesignation())).append(",")
                            .append(String.valueOf(p.getQuantite())).append(",")
                            .append(String.valueOf(p.getStockMinimal())).append(",")
                            .append(String.valueOf(deficit)).append(",")
                            .append(status).append("\n");
                }

                afficherMessage("Export Successful",
                        "Stock alerts exported successfully to:\n" + file.getAbsolutePath(),
                        AlertType.INFORMATION);

            } catch (java.io.IOException e) {
                e.printStackTrace();
                afficherMessage("Export Error", "Failed to export data: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    /**
     * Print stock alerts report
     */
    @FXML
    public void imprimerRapport() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("STOCK ALERTS REPORT\n");
        rapport.append("Generated: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");

        long outOfStock = tableAlertes.getItems().stream().filter(p -> p.getQuantite() == 0).count();
        long lowStock = tableAlertes.getItems().size() - outOfStock;

        rapport.append("SUMMARY:\n");
        rapport.append("Total Alerts: ").append(tableAlertes.getItems().size()).append("\n");
        rapport.append("Out of Stock: ").append(outOfStock).append("\n");
        rapport.append("Low Stock: ").append(lowStock).append("\n\n");

        rapport.append("DETAILS:\n");
        rapport.append("Reference\tDesignation\tCurrent\tMin\tDeficit\tStatus\n");
        rapport.append(String.join("", Collections.nCopies(80, "─"))).append("\n");
        for (Produit p : tableAlertes.getItems()) {
            int deficit = Math.max(0, p.getStockMinimal() - p.getQuantite());
            String status = p.getQuantite() == 0 ? "OUT OF STOCK" : "LOW STOCK";

            rapport.append(p.getReference()).append("\t")
                    .append(p.getDesignation()).append("\t")
                    .append(p.getQuantite()).append("\t")
                    .append(p.getStockMinimal()).append("\t")
                    .append(deficit).append("\t")
                    .append(status).append("\n");
        }

        // Display report in a dialog
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Stock Alerts Report");
        alert.setHeaderText("Stock Status Report");
        alert.setContentText(rapport.toString());
        alert.getDialogPane().setPrefSize(600, 500);
        alert.showAndWait();
    }

    /**
     * Check database connection
     * @return true if connection is available
     */
    private boolean verifierConnexionDB() {
        try (Connection con = Connectiondb.Connection()) {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Display message to user
     * @param titre message title
     * @param message message text
     * @param type message type
     */
    private void afficherMessage(String titre, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Escape CSV field for special characters
     * @param field field to escape
     * @return escaped field
     */
    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Clean up resources when closing window
     */
    public void cleanup() {
        if (tableAlertes != null) {
            tableAlertes.getItems().clear();
        }
    }
}